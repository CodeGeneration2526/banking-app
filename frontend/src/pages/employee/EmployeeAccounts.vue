<script setup lang="ts">
import { ref, onMounted } from "vue";
import { api } from "@/api";
import { formatCents } from "@/utils/money";
import type { AccountSummary, AccountDetail } from "@/types/api";

const accounts = ref<AccountSummary[]>([]);
const loading = ref(true);
const error = ref("");

const PAGE_SIZE = 10;
const pageIndex = ref(0);
const totalPages = ref(0);

const firstName = ref("");
const lastName = ref("");
const iban = ref("");

async function loadAccounts() {
    loading.value = true;
    error.value = "";
    try {
        const result = await api.accounts.list({
            page: pageIndex.value,
            size: PAGE_SIZE,
            firstName: firstName.value,
            lastName: lastName.value,
            iban: iban.value,
        });
        accounts.value = result.content ?? [];
        totalPages.value = result.page?.totalPages ?? 0;

      // If the accounts page is empty, step back to the previous page.
        if (accounts.value.length === 0 && pageIndex.value > 0) {
            pageIndex.value--;
            await loadAccounts();
        }
    } catch {
        error.value = "Failed to load accounts.";
    } finally {
        loading.value = false;
    }
}

function goToPage(index: number) {
    pageIndex.value = index;
    loadAccounts();
}

function search() {
    pageIndex.value = 0;
    loadAccounts();
}

function resetFilters() {
    firstName.value = "";
    lastName.value = "";
    iban.value = "";
    search();
}

onMounted(loadAccounts);

const selected = ref<AccountSummary | null>(null);
const detail = ref<AccountDetail | null>(null);
const detailLoading = ref(false);
const submitting = ref(false);
const modalError = ref("");
const dailyLimitEuros = ref(0);
const absoluteLimitEuros = ref(0);
const closed = ref(false);

async function openEdit(account: AccountSummary) {
    selected.value = account;
    detail.value = null;
    modalError.value = "";
    detailLoading.value = true;
    try {
        const result = await api.accounts.get(account.accountId);
        detail.value = result;
        dailyLimitEuros.value = result.dailyLimitInCents / 100;
        absoluteLimitEuros.value = result.absoluteLimitInCents / 100;
        closed.value = result.closed;
    } catch (e) {
        modalError.value = e instanceof Error ? e.message : "Failed to load account details.";
    } finally {
        detailLoading.value = false;
    }
}

function closeEdit() {
    selected.value = null;
}

async function saveEdit() {
    if (!selected.value) return;

    submitting.value = true;
    modalError.value = "";
    try {
        await api.accounts.update(selected.value.accountId, {
            dailyLimitInCents: Math.round(dailyLimitEuros.value * 100),
            absoluteLimitInCents: Math.round(absoluteLimitEuros.value * 100),
        });
        selected.value = null;
        await loadAccounts();
    } catch (e) {
        modalError.value = e instanceof Error ? e.message : "Failed to update account. Please try again.";
    } finally {
        submitting.value = false;
    }
}

async function toggleClosed() {
    if (!selected.value) return;

    const reopen = closed.value;
    const action = reopen ? "reopen" : "close";
    if (!window.confirm(`Are you sure you want to ${action} this account?`)) return;

    submitting.value = true;
    modalError.value = "";
    try {
        await api.accounts.update(selected.value.accountId, { closed: !reopen });
        selected.value = null;
        await loadAccounts();
    } catch (e) {
        modalError.value = e instanceof Error ? e.message : `Failed to ${action} account. Please try again.`;
    } finally {
        submitting.value = false;
    }
}
</script>

<template>
    <hgroup>
        <h2>Accounts</h2>
        <p>View all customer accounts and manage their transfer limits</p>
    </hgroup>

    <form class="search" @submit.prevent="search">
        <input v-model.trim="firstName" type="text" placeholder="First name" />
        <input v-model.trim="lastName" type="text" placeholder="Last name" />
        <input v-model.trim="iban" type="text" placeholder="IBAN" />
        <button type="submit">Search</button>
        <button type="button" class="secondary" :disabled="!firstName && !lastName && !iban" @click="resetFilters">
          Reset
        </button>
    </form>

    <article>
        <p v-if="loading" aria-busy="true">Loading accounts…</p>
        <p v-else-if="error">{{ error }}</p>
        <p v-else-if="accounts.length === 0">No accounts found.</p>

        <table v-else>
            <thead>
                <tr>
                    <th scope="col">Owner</th>
                    <th scope="col">IBAN</th>
                    <th scope="col">Type</th>
                    <th scope="col"></th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="account in accounts" :key="account.accountId">
                    <td>{{ account.ownerFirstName }} {{ account.ownerLastName }}</td>
                    <td>{{ account.iban ?? "- " }}</td>
                    <td>{{ account.accountType }}</td>
                    <td>
                        <button class="secondary" @click="openEdit(account)">Edit</button>
                    </td>
                </tr>
            </tbody>
        </table>

        <nav v-if="!loading && totalPages > 1" class="pagination">
            <button class="secondary" :disabled="pageIndex === 0" @click="goToPage(pageIndex - 1)">
                < Prev
            </button>
            <span>Page {{ pageIndex + 1 }} of {{ totalPages }}</span>
            <button class="secondary" :disabled="pageIndex >= totalPages - 1" @click="goToPage(pageIndex + 1)">
                Next >
            </button>
        </nav>
    </article>

    <dialog :open="selected !== null">
        <article v-if="selected">
            <header>
                <button aria-label="Close" rel="prev" @click="closeEdit"></button>
                <p>
                    <strong>Edit {{ selected.ownerFirstName }} {{ selected.ownerLastName }} — {{ selected.accountType }}</strong>
                </p>
            </header>

            <p v-if="detailLoading" aria-busy="true">Loading account details…</p>

            <form v-else-if="detail" @submit.prevent="saveEdit">
                <p>Current balance: <strong>{{ formatCents(detail.storedAmountInCents) }}</strong></p>
                <p v-if="closed" class="error"><strong>This account is closed.</strong> Reopen it to edit its limits.</p>

                <label>
                    Daily transfer limit (€)
                    <input v-model.number="dailyLimitEuros" type="number" min="0" :disabled="closed" required />
                </label>
                <label>
                    Absolute limit (€)
                    <input v-model.number="absoluteLimitEuros" type="number" :disabled="closed" required />
                </label>

                <p v-if="modalError" class="error">{{ modalError }}</p>

                <footer>
                    <button type="button" class="secondary" :disabled="submitting" @click="closeEdit">
                        Cancel
                    </button>
                    <button type="submit" :aria-busy="submitting" :disabled="submitting || closed">
                        Save changes
                    </button>
                    <button type="button" :class="closed ? 'reopen' : 'close-account'" :disabled="submitting" @click="toggleClosed">
                      {{ closed ? "Reopen account" : "Close account" }}
                    </button>
                </footer>
            </form>

            <template v-else>
                <p class="error">{{ modalError }}</p>
                <footer>
                    <button class="secondary" @click="closeEdit">
                      Close
                    </button>
                </footer>
            </template>
        </article>
    </dialog>
</template>

<style scoped>
.search {
    display: flex;
    gap: 1rem;
    align-items: end;
    padding-bottom: 1rem;
}

.search input {
    margin: 0;
}

.search button {
    margin: 0;
    width: auto;
}

.error {
    color: var(--pico-del-color);
}

dialog footer {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
}

dialog footer button {
    width: auto;
    margin: 0;
    white-space: nowrap;
}

dialog footer .close-account {
    --pico-background-color: var(--pico-del-color);
    --pico-border-color: var(--pico-del-color);
      margin-left: auto;
}

dialog footer .reopen {
      margin-left: auto;
}

.pagination {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
}

.pagination button {
    margin: 0;
    width: auto;
}
</style>
