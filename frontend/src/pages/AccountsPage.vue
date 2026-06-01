<script setup lang="ts">
import { api } from '@/api';
import { useAuthStore } from '@/stores/auth';
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const auth = useAuthStore();

if (!auth.currentUser) router.push({ name: "login" });
const currentUser = auth.currentUser!;

const accounts = ref<{
    accountId: number;
    iban?: string | undefined;
    accountType: string;
    storedAmountInCents: number;
    userId: number;
    absoluteLimitInCents: number;
    dailyLimitInCents: number;
}[]>([]);

onMounted(async () => {
    let page = 0;
    let totalPages = 1;

    accounts.value = [];

    while (page < totalPages) {
        const res = await api.accounts.list({
            ownerUserId: currentUser.userId,
            sort: "accountType,ASC",
            page,
        });

        const content = res.content ?? [];

        const detailed = await Promise.all(content.map(a => api.accounts.get(a.accountId)));
        accounts.value.push(...detailed);

        let resTotalPages = res.page?.totalPages;
        if (resTotalPages) {
            totalPages = resTotalPages;
        } else {
            break;
        }

        page++;
    }
});

</script>

<template>
    <h1>Welcome back, {{ currentUser.firstName }}!</h1>
    <div v-if="!accounts.length">No Accounts</div>
    <div v-if="accounts.length" class="accounts-grid">
        <article v-for="account in accounts" :key="account.accountId" class="account-card">
            <span class="account-type">{{ account.accountType }}</span>
            <span class="account-balance">&euro;{{ (account.storedAmountInCents / 100).toFixed(2) }}</span>
            <span class="account-iban">{{ account.iban ?? "NO IBAN" }}</span>
            <div class="spacer"></div>
            <a class="transfer-btn">Transfer</a>
        </article>
    </div>
</template>

<style scoped>
.spacer {
    height: 0.5rem;
}

.transfer-btn {
    cursor: pointer;
}

.accounts-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 1rem;
    margin-top: 1.5rem;
}

.account-card {
    display: flex;
    flex-direction: column;
    padding: 1.25rem 3rem 1.25rem 2rem;
}

.account-type {
    font-size: 0.7rem;
    font-weight: 600;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    opacity: 0.6;
}

.account-balance {
    font-size: 1.5rem;
    font-weight: 700;
}

.account-iban {
    font-size: 0.65rem;
    opacity: 0.45;
}
</style>
