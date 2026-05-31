import { defineStore } from "pinia";
import type { User } from "@/types/api";

const TOKEN_KEY = "bankingapp_token";

export const useAuthStore = defineStore("auth", {
    state: () => ({
        token: localStorage.getItem(TOKEN_KEY) ?? "",
        currentUser: null as User | null,
    }),
    getters: {
        isAuthenticated: state => Boolean(state.token),
        isEmployee: state => state.currentUser?.role === "Employee",
    },
    actions: {
        setToken(token: string) {
            this.token = token;
            localStorage.setItem(TOKEN_KEY, token);
        },
        clearToken() {
            this.token = "";
            this.currentUser = null;
            localStorage.removeItem(TOKEN_KEY);
        },
        setCurrentUser(currentUser: User) {
            this.currentUser = currentUser;
        },
        clearCurrentUser() {
            this.currentUser = null;
        },
    },
})
