const API_BASE_URL = import.meta.env.VITE_API_URL;

import type { LoginResponse, User } from "@/types/api";
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
        // JWT Token expiration check
        if (response.status === 401) {
            auth.clearToken();
            router.push({ name: "login" });
        }
        throw new Error(JSON.stringify(response.json()));
        // TODO: do error handling with various Error types
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
