<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { api } from "@/api";

const firstName = ref("");
const lastName = ref("");
const email = ref("");
const password = ref("");
const bsn = ref("");
const phoneNumber = ref("");

const loading = ref(false);
const error = ref("");
const success = ref("");

const router = useRouter();

async function handleRegister() {
    error.value = "";
    success.value = "";
    loading.value = true;

    try {
        await api.auth.register({
            firstName: firstName.value.trim(),
            lastName: lastName.value.trim(),
            email: email.value.trim(),
            password: password.value,
            bsn: bsn.value.trim(),
            phoneNumber: phoneNumber.value.trim(),
        });

        success.value = "Registration submitted. You can log in after an employee approves your account.";
    } catch (e) {
        error.value = e instanceof Error ? e.message : "Failed to register.";
    } finally {
        loading.value = false;
    }
}

function goToLogin() {
    router.push({ name: "login" });
}
</script>

<template>
    <form @submit.prevent="handleRegister">
        <fieldset :disabled="loading">
            <label>
                First name
                <input v-model.trim="firstName" type="text" name="first_name" placeholder="First name" required />
            </label>
            <label>
                Last name
                <input v-model.trim="lastName" type="text" name="last_name" placeholder="Last name" required />
            </label>
            <label>
                Email
                <input v-model.trim="email" type="email" name="email" placeholder="Email address" required />
            </label>
            <label>
                Password
                <input v-model="password" type="password" name="password" placeholder="Create a password" required />
            </label>
            <label>
                BSN
                <input v-model.trim="bsn" type="text" name="bsn" placeholder="BSN number" required />
            </label>
            <label>
                Phone number
                <input v-model.trim="phoneNumber" type="tel" name="phone" placeholder="Phone number" required />
            </label>
        </fieldset>

        <p v-if="error" class="error">{{ error }}</p>
        <p v-else-if="success" class="success">{{ success }}</p>

        <div class="actions">
            <input type="submit" value="Create account" :disabled="loading" />
            <button type="button" class="secondary" @click="goToLogin">Back to login</button>
        </div>
    </form>
</template>

<style scoped>
form {
    max-width: 36rem;
    margin: 0 auto;
}

.actions {
    display: flex;
    gap: 0.75rem;
    align-items: center;
    flex-wrap: wrap;
}

.actions input,
.actions button {
    width: auto;
}

.error {
    color: var(--pico-del-color);
}

.success {
    color: var(--pico-ins-color);
}
</style>
