const API_BASE_URL = import.meta.env.VITE_API_URL;

import type { LoginResponse, User } from "@/types/api";
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
        if (response.status === 401) {
            auth.clearToken();
            router.push({ name: "login" });
        }

        let message = response.statusText;

        try {
            const data = await response.json();

            if (typeof data?.message === "string" && data.message.length > 0) {
                message = data.message;
            }
        } catch {/* response was not valid json */}

        throw new ApiError(response.status, message);
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
    }
};
