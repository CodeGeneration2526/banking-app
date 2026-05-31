import { createApp } from "vue";
import { createPinia } from "pinia";

import App from "./App.vue";
import router from "./router";
import { api } from "./api";
import { useAuthStore } from "./stores/auth";

import "./styles/main.scss";

const app = createApp(App);

app.use(createPinia());

// Hydrates the current user on refresh since only the token persists
// so we fetch the profile to keep UI up to date with auth user
const auth = useAuthStore();
if (auth.isAuthenticated && !auth.currentUser) {
    try {
        auth.setCurrentUser(await api.users.me());
    } catch {
        auth.clearToken();
    }
}

app.use(router);

app.mount("#app");
