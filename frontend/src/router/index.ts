import { createRouter, createWebHistory } from "vue-router";
import HomePage from "@/pages/HomePage.vue";
import LoginPage from "@/pages/LoginPage.vue";
import NotFoundPage from "@/pages/NotFoundPage.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        { path: "/", name: "home", component: HomePage },
        { path: "/login", name: "login", component: LoginPage },

        // 404 catch all route
        { path: "/:pathMatch(.*)*", name: "not-found", component: NotFoundPage },
    ],
});

export default router;
