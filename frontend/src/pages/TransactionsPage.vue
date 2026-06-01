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
</template>

<style scoped>
</style>
