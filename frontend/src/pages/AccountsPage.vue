<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const router = useRouter();
const auth = useAuthStore();

if (!auth.currentUser) router.push({ name: "login" });
const currentUser = auth.currentUser!;

</script>

<template>
    <h1>Welcome back, {{ currentUser.firstName }}!</h1>
    <div>
        Account
    </div>
</template>

<style scoped>
.dashboard {
    display: grid;
    grid-template-columns: 14rem 1fr;
    gap: 2rem;
}

aside nav ul {
    flex-direction: column;
}

aside nav a[aria-current="page"] {
    background: var(--pico-primary-background);
    color: var(--pico-primary-inverse);
    border-radius: var(--pico-border-radius);
}
</style>
