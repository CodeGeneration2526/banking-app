import { createRouter, createWebHistory } from "vue-router";
import HomePage from "@/pages/HomePage.vue";
import LoginPage from "@/pages/LoginPage.vue";
import NotFoundPage from "@/pages/NotFoundPage.vue";
import EmployeeDashboard from "@/pages/EmployeeDashboard.vue";
import AccountsPage from "@/pages/AccountsPage.vue";
import TransactionsPage from "@/pages/TransactionsPage.vue";
import { useAuthStore } from "@/stores/auth";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        { path: "/", name: "home", component: HomePage },
        { path: "/login", name: "login", component: LoginPage },
        { path: "/employee", name: "employee", component: EmployeeDashboard, meta: { requiresEmployee: true } },
        { path: "/accounts", name: "accounts", component: AccountsPage },
        { path: "/transactions", name: "transactions", component: TransactionsPage },

        // 404 catch all route
        { path: "/:pathMatch(.*)*", name: "not-found", component: NotFoundPage },
    ],
});

// Guard for employee only routes, profile is refreshed at startup (see: main.ts)
router.beforeEach((to) => {
    if (!to.meta.requiresEmployee) return true;
    if (!useAuthStore().isAuthenticated) return { name: "login" };
    return useAuthStore().isEmployee ? true : { name: "home" };
});

export default router;
