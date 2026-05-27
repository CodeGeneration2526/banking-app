import { createRouter, createWebHistory } from "vue-router";
import HomePage from "@/pages/HomePage.vue";
import LoginRegisterPage from "@/pages/LoginRegisterPage.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        { path: "/", name: "Home", component: HomePage },
        { path: "/authenticate", name: "Login/Register", component: LoginRegisterPage },
    ],
});

export default router;
