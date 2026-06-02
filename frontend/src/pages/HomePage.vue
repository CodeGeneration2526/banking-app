<script setup lang="ts">
import { computed } from "vue";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();

const isPendingApproval = computed(
    () => auth.currentUser?.role === "Customer" && !auth.currentUser?.approvedBy,
);
</script>

<template>
    <section class="welcome">
        <h1>Welcome to the bank</h1>

        <p v-if="isPendingApproval">
            Thanks for registering! Your account is pending employee approval. You can log in again
            once your account is approved.
        </p>

        <p v-else-if="auth.isAuthenticated">
            You're all set. Head to your dashboard to manage your work.
        </p>

        <p v-else>
            Please sign in to access your accounts, or create a new customer profile if you are
            registering for the first time.
        </p>

        <div class="actions">
            <RouterLink v-if="auth.isEmployee" to="/employee" role="button">
                Employee dashboard
            </RouterLink>
            <RouterLink v-else-if="auth.isAuthenticated && !isPendingApproval" to="/accounts" role="button">
                Go to accounts
            </RouterLink>
            <RouterLink v-else-if="!auth.isAuthenticated" to="/login" role="button">
                Login
            </RouterLink>
            <RouterLink v-if="!auth.isAuthenticated" to="/register" class="secondary" role="button">
                Register
            </RouterLink>
        </div>
    </section>
</template>

<style scoped>
.welcome {
    max-width: 40rem;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.actions {
    display: flex;
    gap: 0.75rem;
    flex-wrap: wrap;
}

.actions a {
    width: auto;
}
</style>
