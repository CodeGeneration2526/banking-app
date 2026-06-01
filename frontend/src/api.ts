const API_BASE_URL = import.meta.env.VITE_API_URL;

import type { LoginResponse, User, UsersPage, NewAccountRequest, ApiMessage } from "@/types/api";
import { useAuthStore } from "@/stores/auth";
import router from "@/router";

async function request<T>(path: string, options: RequestInit): Promise<T> {
    const auth = useAuthStore();
    
    const headers = new Headers(options.headers);
    headers.append("Content-Type", "application/json");

    if (auth.token) headers.append("Authorization", "Bearer " + auth.token);

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers,
    });

    if (!response.ok) {
        // Most errors return { message }, @Valid failures return a field->message map, account for both states
        const body = await response.json();
        const message = body.message ?? Object.values(body).join(", ");

        // Check for token expiration (Auth check is because backend sends 401 for auth policy rejections too)
        if (response.status === 401 && !auth.currentUser) {
            auth.clearToken();
            router.push({ name: "login" });
        }

        throw new Error(message);
    }

    return response.json();
}

export const api = {
    auth: {
        login(email: string, password: string) {
            return request<LoginResponse>("/auth/login", {
                method: "POST",
                body: JSON.stringify({ email, password }),
            });
        },
    },
    users: {
        me: () => request<User>("/users/me", { method: "GET" }),
        list: (params: { page?: number; size?: number; isApproved?: boolean; role?: User["role"] } = {}) => {
            const query = new URLSearchParams();
            if (params.page !== undefined) query.set("page", String(params.page));
            if (params.size !== undefined) query.set("size", String(params.size));
            if (params.isApproved !== undefined) query.set("isApproved", String(params.isApproved));
            if (params.role !== undefined) query.set("role", params.role);
            const qs = query.toString();
            return request<UsersPage>(`/users${qs ? `?${qs}` : ""}`, { method: "GET" });
        },
    },
    accounts: {
        // Approve a pending customer and create their checking + savings accounts.
        approve: (body: NewAccountRequest) =>
            request<ApiMessage>("/accounts", {
                method: "POST",
                body: JSON.stringify(body),
            }),
    },
};
