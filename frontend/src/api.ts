const API_BASE_URL = import.meta.env.VITE_API_URL;

import type {
    LoginResponse,
    User,
    UsersPage,
    NewAccountRequest,
    ApiMessage,
    AccountsPage,
    AccountDetail,
    UpdateAccountRequest,
} from "@/types/api";
import { useAuthStore } from "@/stores/auth";
import router from "@/router";

export class ApiError extends Error {
    status: number;
    constructor(status: number, message: string) {
        super(message);
        this.status = status;
    }
}

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
        // Fetch currently logged in user
        me: () => request<User>("/users/me", { method: "GET" }),
        // Fetch all users with params to check role and approval status
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
        // Fetch all accounts with params to search
        list: (params: { page?: number; size?: number; firstName?: string; lastName?: string; iban?: string } = {}) => {
            const query = new URLSearchParams();
            if (params.page !== undefined) query.set("page", String(params.page));
            if (params.size !== undefined) query.set("size", String(params.size));
            if (params.firstName) query.set("firstName", params.firstName);
            if (params.lastName) query.set("lastName", params.lastName);
            if (params.iban) query.set("iban", params.iban);
            const qs = query.toString();
            return request<AccountsPage>(`/accounts${qs ? `?${qs}` : ""}`, { method: "GET" });
        },
        // Fetch a single account's details (balance + limits)
        get: (accountId: number) => request<AccountDetail>(`/accounts/${accountId}`, { method: "GET" }),
        // Update an account's limits and/or closed status
        update: (accountId: number, body: UpdateAccountRequest) =>
            request<AccountDetail>(`/accounts/${accountId}`, {
                method: "PATCH",
                body: JSON.stringify(body),
            }),
        // Approve a pending customer and create their checking + savings accounts
        approve: (body: NewAccountRequest) =>
            request<ApiMessage>("/accounts", {
                method: "POST",
                body: JSON.stringify(body),
            }),
    },
};
