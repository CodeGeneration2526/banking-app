import { defineStore } from "pinia";
import type { LoginResponse } from "@/types/api";

const TOKEN_KEY = "bankingapp_token";

export const useAuthStore = defineStore("auth", {
    state: () => ({
        token: localStorage.getItem(TOKEN_KEY) ?? "",
        currentUser: null as LoginResponse | null,
    }),
    getters: {
        isAuthenticated: state => Boolean(state.token),
    },
    actions: {
        setToken(token: string) {
            this.token = token;
            localStorage.setItem(TOKEN_KEY, token);
        },
        clearToken() {
            this.token = "";
            localStorage.removeItem(TOKEN_KEY);
        },
    },
})
