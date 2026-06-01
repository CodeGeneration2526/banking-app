<script setup lang="ts">
import { api } from '@/api';
import { useAuthStore } from '@/stores/auth';
import type { AccountDetail } from '@/types/api';
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const auth = useAuthStore();

const transferState = ref<AccountDetail|null>(null);

const accounts = ref<AccountDetail[]>([]);

onMounted(async () => {
    if (!auth.currentUser) {
        router.push({ name: "login" });
        return;
    };

    let page = 0;
    let totalPages = 1;

    accounts.value = [];

    while (page < totalPages) {
        const res = await api.accounts.list({
            ownerUserId: auth.currentUser.userId,
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

function showTransfer(account: AccountDetail) {
    transferState.value = account;
}

</script>

<template>
    <h1>Welcome back, {{ auth.currentUser?.firstName }}!</h1>
    <div v-if="!accounts.length">No Accounts</div>
    <div v-if="accounts.length" class="accounts-grid">
        <article v-for="account in accounts" :key="account.accountId" class="account-card">
            <span class="account-type">{{ account.accountType }}</span>
            <span class="account-balance">&euro;{{ (account.storedAmountInCents / 100).toFixed(2) }}</span>
            <span class="account-iban">{{ account.iban ?? account.accountNumber }}</span>
            <div class="spacer"></div>
            <a @click="showTransfer(account)" class="transfer-btn">Transfer</a>
        </article>
    </div>
    <div v-if="transferState">
        <h2>Transfers</h2>
        <fieldset>
            <label>
                Sender
                <input disabled="true" name="sender" :value="transferState.iban ?? transferState.accountNumber">
            </label>
            <label>
                Receiver
                <input name="receiver" placeholder="Search for the Receiver">
            </label>
        </fieldset>
    </div>
</template>

<style scoped>
fieldset {
    display: flex;
    flex-direction: row;
    gap: 1rem;
}

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
