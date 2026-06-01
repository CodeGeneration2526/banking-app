<script setup lang="ts">
import { api } from '@/api';
import { useAuthStore } from '@/stores/auth';
import type { AccountDetail, AccountSummary, AmountFilter, Transaction } from '@/types/api';
import { formatCents } from '@/utils/money';
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const auth = useAuthStore();

const transferState = ref<AccountDetail|null>(null);
const transferReceiver = ref("");
const transferAmountEuros = ref("");
const transferMode = ref<"ownSavings" | "other">("ownSavings");
const submitting = ref(false);
const transferError = ref("");
const successMessage = ref("");

const accounts = ref<AccountDetail[]>([]);

const showUserSearch = ref(false);
const searchFirstName = ref("");
const searchLastName = ref("");
const searchIban = ref("");
const searchResults = ref<AccountSummary[]>([]);
const searchLoading = ref(false);
const searchError = ref("");
const searchPage = ref(0);
const searchTotalPages = ref(0);
const SEARCH_PAGE_SIZE = 8;

const transactions = ref<Transaction[]>([]);
const txLoading = ref(false);
const txError = ref("");
const txPage = ref(0);
const txTotalPages = ref(0);
const TX_PAGE_SIZE = 10;

const txAccount = ref("");
const txDateFrom = ref("");
const txDateTo = ref("");
const txAmountEuros = ref("");
const txAmountFilter = ref<AmountFilter>("EqualTo");
const dateFormat = new Intl.DateTimeFormat("en-NL", { dateStyle: "medium", timeStyle: "short" });

const currentUserId = computed(() => auth.currentUser?.userId ?? null);

const senderIdentifier = computed(() => {
    if (!transferState.value) return "";
    return String(transferState.value.iban ?? transferState.value.accountNumber ?? "");
});

const ownAccounts = computed(() =>
    accounts.value.filter(account => account.userId === currentUserId.value),
);

const ownCheckingAccounts = computed(() =>
    ownAccounts.value.filter(account => account.accountType === "Checking"),
);

const ownAccountIdentifiers = computed(() =>
    new Set(ownAccounts.value.map(account => String(account.iban ?? account.accountNumber))),
);

const eligibleOwnAccounts = computed(() => {
    if (!transferState.value) return [];

    const sender = transferState.value;
    const others = ownAccounts.value.filter(account => account.accountId !== sender.accountId);

    if (sender.accountType === "Checking") {
        return others.filter(account => account.accountType === "Savings");
    }

    if (sender.accountType === "Savings") {
        return others;
    }

    return others;
});

const canSubmitTransfer = computed(() => {
    if (!transferState.value) return false;
    if (submitting.value) return false;

    if (transferState.value.accountType === "Savings") {
        return eligibleOwnAccounts.value.length > 0;
    }

    if (transferState.value.accountType === "Checking" && transferMode.value === "ownSavings") {
        return eligibleOwnAccounts.value.length > 0;
    }

    return true;
});

function looksLikeIban(value: string) {
    const trimmed = value.trim();
    if (trimmed.length < 8) return false;
    return /^[A-Za-z]{2}[0-9A-Za-z]+$/.test(trimmed);
}

async function loadAccounts() {
    if (!auth.currentUser) {
        router.push({ name: "login" });
        return;
    }

    if (auth.currentUser.role === "Customer" && !auth.currentUser.approvedBy) {
        router.push({ name: "home" });
        return;
    }

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
}

async function loadUserSearchResults() {
    searchLoading.value = true;
    searchError.value = "";
    try {
        const result = await api.accounts.list({
            page: searchPage.value,
            size: SEARCH_PAGE_SIZE,
            firstName: searchFirstName.value.trim() || undefined,
            lastName: searchLastName.value.trim() || undefined,
            iban: searchIban.value.trim() || undefined,
        });
        const content = result.content ?? [];
        const filtered = content.filter(account => account.ownerUserId !== currentUserId.value);
        searchResults.value = filtered;
        searchTotalPages.value = result.page?.totalPages ?? 0;

        if (searchResults.value.length === 0 && searchPage.value > 0) {
            searchPage.value--;
            await loadUserSearchResults();
        }
    } catch (e) {
        searchError.value = e instanceof Error ? e.message : "Failed to search users.";
    } finally {
        searchLoading.value = false;
    }
}

async function loadTransactions() {
    txLoading.value = true;
    txError.value = "";
    try {
        const amountValue = String(txAmountEuros.value ?? "").trim();
        const amountInCents = amountValue ? Math.round(Number(amountValue) * 100) : undefined;

        const result = await api.transactions.list({
            page: txPage.value,
            size: TX_PAGE_SIZE,
            account: txAccount.value.trim() || undefined,
            dateFrom: txDateFrom.value || undefined,
            dateTo: txDateTo.value || undefined,
            amountInCents,
            amountFilter: amountInCents !== undefined ? txAmountFilter.value : undefined,
        });

        transactions.value = result.content ?? [];
        txTotalPages.value = result.page?.totalPages ?? 0;

        if (transactions.value.length === 0 && txPage.value > 0) {
            txPage.value--;
            await loadTransactions();
        }
    } catch (e) {
        txError.value = e instanceof Error ? e.message : "Failed to load transactions.";
    } finally {
        txLoading.value = false;
    }
}

function openUserSearch() {
    showUserSearch.value = true;
    searchError.value = "";
    if (searchResults.value.length === 0) {
        searchPage.value = 0;
        loadUserSearchResults();
    }
}

function startTransferToAccount(account: AccountSummary) {
    const sender = ownCheckingAccounts.value[0];
    if (!sender) {
        searchError.value = "You need a checking account to send money.";
        return;
    }

    showTransfer(sender);
    transferMode.value = "other";
    transferReceiver.value = String(account.iban ?? account.accountNumber ?? "");
    showUserSearch.value = false;
}

function closeUserSearch() {
    showUserSearch.value = false;
}

function submitUserSearch() {
    searchPage.value = 0;
    loadUserSearchResults();
}

function goToSearchPage(index: number) {
    searchPage.value = index;
    loadUserSearchResults();
}

function goToTxPage(index: number) {
    txPage.value = index;
    loadTransactions();
}

function searchTransactions() {
    txPage.value = 0;
    loadTransactions();
}

function resetTransactions() {
    txAccount.value = "";
    txDateFrom.value = "";
    txDateTo.value = "";
    txAmountEuros.value = "";
    txAmountFilter.value = "EqualTo";
    searchTransactions();
}

const hasTxFilters = computed(() =>
    !!(txAccount.value || txDateFrom.value || txDateTo.value || txAmountEuros.value),
);


onMounted(() => {
    loadAccounts();
    loadTransactions();
});

function showTransfer(account: AccountDetail) {
    transferState.value = account;
    transferReceiver.value = "";
    transferAmountEuros.value = "";
    transferError.value = "";
    successMessage.value = "";

    if (account.accountType === "Checking") {
        if (eligibleOwnAccounts.value.length) {
            transferMode.value = "ownSavings";
            transferReceiver.value = String(
                eligibleOwnAccounts.value[0]?.iban ?? eligibleOwnAccounts.value[0]?.accountNumber,
            );
        } else {
            transferMode.value = "other";
        }
    } else {
        transferMode.value = "ownSavings";
        if (eligibleOwnAccounts.value.length) {
            transferReceiver.value = String(
                eligibleOwnAccounts.value[0]?.iban ?? eligibleOwnAccounts.value[0]?.accountNumber,
            );
        }
    }
}

function closeTransfer() {
    transferState.value = null;
    transferReceiver.value = "";
    transferAmountEuros.value = "";
    transferError.value = "";
}

watch(transferMode, mode => {
    if (!transferState.value) return;
    if (mode === "ownSavings" && eligibleOwnAccounts.value.length) {
        const first = eligibleOwnAccounts.value[0];
        const firstIdentifier = String(first?.iban ?? first?.accountNumber);
        if (!eligibleOwnAccounts.value.some(account => String(account.iban ?? account.accountNumber) === transferReceiver.value)) {
            transferReceiver.value = firstIdentifier;
        }
    }

    if (mode === "other") {
        const current = transferReceiver.value.trim();
        if (!current) return;
        if (ownAccountIdentifiers.value.has(current)) {
            transferReceiver.value = "";
        }
    }
});

async function submitTransfer() {
    if (!transferState.value) return;

    transferError.value = "";
    submitting.value = true;

    const amountInCents = Math.round(Number(transferAmountEuros.value) * 100);
    const to = transferReceiver.value.trim();

    if (!to) {
        transferError.value = "Please select a receiver.";
        submitting.value = false;
        return;
    }

    if (!amountInCents || amountInCents <= 0) {
        transferError.value = "Enter a valid transfer amount.";
        submitting.value = false;
        return;
    }

    if (senderIdentifier.value === to) {
        transferError.value = "Sender and receiver must be different accounts.";
        submitting.value = false;
        return;
    }

    if (transferState.value.accountType === "Savings") {
        const allowed = eligibleOwnAccounts.value.some(
            account => String(account.iban ?? account.accountNumber) === to,
        );
        if (!allowed) {
            transferError.value = "Savings accounts can only transfer to your own account.";
            submitting.value = false;
            return;
        }
    }

    if (transferState.value.accountType === "Checking") {
        if (transferMode.value === "ownSavings") {
            const allowed = eligibleOwnAccounts.value.some(
                account => String(account.iban ?? account.accountNumber) === to,
            );
            if (!allowed) {
                transferError.value = "Checking accounts can only transfer to your savings or other users.";
                submitting.value = false;
                return;
            }
        }

        if (transferMode.value === "other" && ownAccountIdentifiers.value.has(to)) {
            transferError.value = "Checking accounts can only transfer to your savings or other users.";
            submitting.value = false;
            return;
        }

        if (transferMode.value === "other" && !looksLikeIban(to)) {
            transferError.value = "Please enter a valid IBAN for other users.";
            submitting.value = false;
            return;
        }
    }

    try {
        await api.transactions.create({
            from: senderIdentifier.value,
            to,
            amountInCents,
        });
        closeTransfer();
        successMessage.value = "Transfer completed successfully.";
        await loadAccounts();
        await loadTransactions();
    } catch (e) {
        transferError.value = e instanceof Error ? e.message : "Failed to execute transfer.";
    } finally {
        submitting.value = false;
    }
}

</script>

<template>
    <h1>Welcome back, {{ auth.currentUser?.firstName }}!</h1>
    <div v-if="!accounts.length">No Accounts</div>
    <div v-if="accounts.length" class="accounts-grid">
        <article v-for="account in accounts" :key="account.accountId" class="account-card">
            <span class="account-type">{{ account.accountType }}</span>
            <span class="account-balance">{{ formatCents(account.storedAmountInCents) }}</span>
            <span class="account-iban">{{ account.iban ?? account.accountNumber }}</span>
            <div class="spacer"></div>
            <a @click="showTransfer(account)" class="transfer-btn">Transfer</a>
        </article>
    </div>
    <button class="secondary search-users-btn" @click="openUserSearch">
        Search other users
    </button>

    <section class="transactions-section">
        <div class="transactions-header">
            <h2>Transactions</h2>
            <p>Filter your activity by date, amount, or account.</p>
        </div>

        <form class="tx-search" @submit.prevent="searchTransactions">
            <label>
                Account
                <input
                    v-model.trim="txAccount"
                    type="text"
                    list="account-options"
                    placeholder="IBAN or Account Number"
                />
            </label>
            <label>
                From
                <input v-model="txDateFrom" type="date" />
            </label>
            <label>
                To
                <input v-model="txDateTo" type="date" />
            </label>
            <label>
                Amount (€)
                <div class="amount-filter">
                    <select v-model="txAmountFilter" aria-label="Amount comparison">
                        <option value="LessThan">&lt;</option>
                        <option value="EqualTo">=</option>
                        <option value="GreaterThan">&gt;</option>
                    </select>
                    <input v-model.trim="txAmountEuros" type="number" min="0" step="0.01" />
                </div>
            </label>
            <button type="submit">Search</button>
            <button type="button" class="secondary" :disabled="!hasTxFilters" @click="resetTransactions">
                Reset
            </button>
        </form>

        <datalist id="account-options">
            <option
                v-for="account in ownAccounts"
                :key="account.accountId"
                :value="String(account.iban ?? account.accountNumber)"
            />
        </datalist>

        <article>
            <p v-if="txLoading" aria-busy="true">Loading transactions…</p>
            <p v-else-if="txError" class="error">{{ txError }}</p>
            <p v-else-if="transactions.length === 0">No transactions found.</p>

            <table v-else>
                <thead>
                    <tr>
                        <th scope="col">From</th>
                        <th scope="col">To</th>
                        <th scope="col">Amount</th>
                        <th scope="col">Date</th>
                        <th scope="col">Initiated by</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="tx in transactions" :key="tx.transactionId">
                        <td>
                            {{ tx.from.identifier }}
                            <small>{{ tx.from.ownerFirstName }} {{ tx.from.ownerLastName }}</small>
                        </td>
                        <td>
                            {{ tx.to.identifier }}
                            <small>{{ tx.to.ownerFirstName }} {{ tx.to.ownerLastName }}</small>
                        </td>
                        <td>{{ formatCents(tx.amountInCents) }}</td>
                        <td>{{ dateFormat.format(new Date(tx.timestamp)) }}</td>
                        <td>{{ tx.initiatedBy }}</td>
                    </tr>
                </tbody>
            </table>

            <nav v-if="!txLoading" class="pagination">
                <button class="secondary" :disabled="txPage === 0" @click="goToTxPage(txPage - 1)">
                    &lt; Prev
                </button>
                <span>Page {{ txPage + 1 }} of {{ txTotalPages }}</span>
                <button
                    class="secondary"
                    :disabled="txPage >= txTotalPages - 1"
                    @click="goToTxPage(txPage + 1)"
                >
                    Next &gt;
                </button>
            </nav>
        </article>
    </section>
    <dialog :open="transferState !== null">
        <article v-if="transferState">
            <header>
                <button aria-label="Close" rel="prev" @click="closeTransfer"></button>
                <p><strong>New transfer</strong></p>
            </header>

            <form @submit.prevent="submitTransfer">
                <label>
                    Sender
                    <input disabled="true" name="sender" :value="transferState.iban ?? transferState.accountNumber">
                </label>

                <template v-if="transferState.accountType === 'Checking'">
                    <fieldset class="transfer-destination">
                        <label>
                            <input
                                v-model="transferMode"
                                type="radio"
                                value="ownSavings"
                                :disabled="eligibleOwnAccounts.length === 0"
                            />
                            My accounts
                        </label>
                        <label>
                            <input v-model="transferMode" type="radio" value="other" />
                            Other account
                        </label>
                    </fieldset>

                    <label v-if="transferMode === 'ownSavings'">
                        Receiver
                        <select v-model="transferReceiver" :disabled="eligibleOwnAccounts.length === 0" required>
                            <option v-if="eligibleOwnAccounts.length === 0" value="">No savings accounts available</option>
                            <option
                                v-for="account in eligibleOwnAccounts"
                                :key="account.accountId"
                                :value="String(account.iban ?? account.accountNumber)"
                            >
                                {{ account.iban ?? account.accountNumber }}
                            </option>
                        </select>
                    </label>

                    <label v-else>
                        Receiver (IBAN)
                        <input
                            v-model.trim="transferReceiver"
                            name="receiver"
                            placeholder="IBAN"
                            required
                        />
                    </label>
                </template>

                <template v-else>
                    <label>
                        Receiver
                        <select v-model="transferReceiver" :disabled="eligibleOwnAccounts.length === 0" required>
                            <option v-if="eligibleOwnAccounts.length === 0" value="">No eligible accounts available</option>
                            <option
                                v-for="account in eligibleOwnAccounts"
                                :key="account.accountId"
                                :value="String(account.iban ?? account.accountNumber)"
                            >
                                {{ account.iban ?? account.accountNumber }}
                            </option>
                        </select>
                    </label>
                </template>

                <label>
                    Amount (€)
                    <input v-model.trim="transferAmountEuros" type="number" min="0.01" step="0.01" required />
                </label>

                <p v-if="transferError" class="error">{{ transferError }}</p>

                <footer>
                    <button type="submit" :aria-busy="submitting" :disabled="!canSubmitTransfer">
                        Transfer
                    </button>
                </footer>
            </form>
        </article>
    </dialog>

    <dialog :open="showUserSearch">
        <article>
            <header>
                <button aria-label="Close" rel="prev" @click="closeUserSearch"></button>
                <p><strong>Search users</strong></p>
            </header>

            <form class="search-users" @submit.prevent="submitUserSearch">
                <div class="name-row">
                    <label>
                        First name
                        <input v-model.trim="searchFirstName" type="text" placeholder="First name" />
                    </label>
                    <label>
                        Last name
                        <input v-model.trim="searchLastName" type="text" placeholder="Last name" />
                    </label>
                </div>
                <label>
                    IBAN
                    <input v-model.trim="searchIban" type="text" placeholder="IBAN" />
                </label>
                <div class="search-actions">
                    <button type="submit" :aria-busy="searchLoading">Search</button>
                </div>
            </form>

            <p v-if="searchLoading" aria-busy="true">Searching users…</p>
            <p v-else-if="searchError" class="error">{{ searchError }}</p>
            <p v-else-if="searchResults.length === 0">No users found.</p>

            <table v-else class="search-results">
                <thead>
                    <tr>
                        <th scope="col">Owner</th>
                        <th scope="col">IBAN</th>
                        <th scope="col">Action</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="account in searchResults" :key="account.accountId">
                        <td>{{ account.ownerFirstName }} {{ account.ownerLastName }}</td>
                        <td>{{ account.iban ?? account.accountNumber }}</td>
                        <td>
                            <a @click="startTransferToAccount(account)" class="send-money-btn">
                                Send money
                            </a>
                        </td>
                    </tr>
                </tbody>
            </table>

            <nav v-if="!searchLoading && searchTotalPages > 1" class="pagination">
                <button
                    class="secondary"
                    :disabled="searchPage === 0"
                    @click="goToSearchPage(searchPage - 1)"
                >
                    &lt; Prev
                </button>
                <span>Page {{ searchPage + 1 }} of {{ searchTotalPages }}</span>
                <button
                    class="secondary"
                    :disabled="searchPage >= searchTotalPages - 1"
                    @click="goToSearchPage(searchPage + 1)"
                >
                    Next &gt;
                </button>
            </nav>
        </article>
    </dialog>

    <dialog :open="successMessage !== ''" class="chonky">
        <article>
            <header>
                <button aria-label="Close" rel="prev" @click="successMessage = ''"></button>
                <p><strong>Success</strong></p>
            </header>
            <p>{{ successMessage }}</p>
            <button @click="successMessage = ''">OK</button>
        </article>
    </dialog>
</template>

<style scoped>
fieldset {
    display: flex;
    flex-direction: row;
    gap: 1rem;
}

.transfer-destination {
    margin: 0;
}

.spacer {
    height: 0.5rem;
}

.transfer-btn,
.send-money-btn {
    cursor: pointer;
}

.search-users-btn {
    margin-top: 0.75rem;
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

.search-users {
    display: grid;
    gap: 0.75rem;
    margin-bottom: 1rem;
}

.name-row {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0.75rem;
}

.search-actions {
    display: flex;
    gap: 0.75rem;
    align-items: center;
}

.search-results {
    margin-top: 0.5rem;
}

.transactions-section {
    margin-top: 2rem;
}

.transactions-header {
    display: flex;
    flex-direction: column;
    gap: 0.35rem;
    margin-bottom: 1rem;
}

.transactions-header h2 {
    margin-bottom: 0;
}

.transactions-header p {
    margin: 0;
    color: var(--pico-muted-color);
}

.tx-search {
    display: flex;
    flex-wrap: wrap;
    gap: 1rem;
    align-items: end;
    margin-bottom: 1rem;
}

.tx-search label {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    font-size: 0.875rem;
    margin: 0;
}

.tx-search input,
.tx-search select {
    margin: 0;
}

.tx-search button {
    margin: 0;
    width: auto;
}

.amount-filter {
    display: flex;
    gap: 0.5rem;
}

.amount-filter select {
    width: auto;
    flex: none;
}

.amount-filter input {
    min-width: 4rem;
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

td small {
    display: block;
    color: var(--pico-muted-color);
}

.error {
    color: var(--pico-del-color);
}
</style>
