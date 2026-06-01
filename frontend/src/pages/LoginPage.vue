<script setup lang="ts">
import { api, ApiError } from '@/api';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { ref } from 'vue';

const email = ref("");
const password = ref("");
const error = ref("");
const loading = ref(false);

const auth = useAuthStore();
const router = useRouter();

function redirectUser() {
    if (auth.isEmployee) {
        router.push({ name: "employee" });
    } else if (auth.currentUser?.role === "Customer" && !auth.currentUser?.approvedBy) {
        router.push({ name: "home" });
    } else {
        router.push({ name: "accounts" });
    }
}

if (auth.isAuthenticated) redirectUser();

async function handleLogin() {
    error.value = "";
    loading.value = true;

    try {
        const response = await api.auth.login(
            email.value,
            password.value,
        );

        auth.setToken(response.token);
        // If a token is set but the api calls fail it's set to null atm, could be worth fixing or checking later
        auth.setCurrentUser(await api.users.me());

        redirectUser();
    } catch (e) {
        if (e instanceof ApiError) {
            error.value = e.message;
            console.error(e.message);
        } else {
            error.value = "Something went wrong...";
            throw e; // hailmary? the error likely originated from vue router then
        }
    } finally {
        loading.value = false;
    }
}

</script>

<template>
    <form @submit.prevent="handleLogin">
        <fieldset :disabled="loading">
            <label>
                Email
                <input v-model="email" type="email" name="first_name" placeholder="Email Address" autocomplete="off" required />
            </label>
            <label>
                Password
                <input v-model="password" type="password" name="password" placeholder="Your super secret password" autocomplete="off" required />
            </label>
        </fieldset>

        <p v-if="error">{{error}}</p>

        <input type="submit" value="Login" :disabled="loading" />
        <p class="register-link">
            New here? <RouterLink to="/register">Create an account</RouterLink>
        </p>
    </form>
</template>

<style scoped>
form {
    max-width: 32rem;
    margin: 0 auto;
}

.register-link {
    margin-top: 0.75rem;
}
</style>
