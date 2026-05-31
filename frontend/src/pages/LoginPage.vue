<script setup lang="ts">
import { api } from '@/api';
import { useAuthStore } from '@/stores/auth';
import { ref } from 'vue';

const email = ref("");
const password = ref("");
const error = ref("");
const loading = ref(false);

const auth = useAuthStore();

if (auth.isAuthenticated) {
    console.log("User is already authenticated");
    // TODO: redirect to the Users page when that is implemented
}

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
    } catch (e) {
        console.error(e);
        error.value = JSON.stringify(e);
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
    </form>
</template>

<style scoped>
form {
    max-width: 32rem;
    margin: 0 auto;
}
</style>
