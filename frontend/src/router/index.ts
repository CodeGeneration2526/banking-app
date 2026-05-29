import { createRouter, createWebHistory } from "vue-router";
import HomePage from "@/pages/HomePage.vue";
import LoginPage from "@/pages/LoginPage.vue";
import NotFoundPage from "@/pages/NotFoundPage.vue";
import EmployeeDashboard from "@/pages/EmployeeDashboard.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        { path: "/", name: "home", component: HomePage },
        { path: "/login", name: "login", component: LoginPage },
        { path: "/employee", name: "employee", component: EmployeeDashboard },

        // 404 catch all route
        { path: "/:pathMatch(.*)*", name: "not-found", component: NotFoundPage },
    ],
});

export default router;
