<script setup lang="ts">
import { ref, onMounted } from "vue";
import { api } from "@/api";
import { formatCents } from "@/utils/money";
import type { Transaction, AmountFilter } from "@/types/api";

const transactions = ref<Transaction[]>([]);
const loading = ref(true);
const error = ref("");

const dateFormat = new Intl.DateTimeFormat("en-NL", { dateStyle: "medium", timeStyle: "long" });

const PAGE_SIZE = 10;
const pageIndex = ref(0);
const totalPages = ref(0);

const userId = ref("");
const account = ref("");
const dateFrom = ref("");
const dateTo = ref("");
const amountEuros = ref("");
const amountFilter = ref<AmountFilter>("EqualTo");

async function loadTransactions() {
  loading.value = true;
  error.value = "";
  try {
    const result = await api.transactions.list({
      page: pageIndex.value,
      size: PAGE_SIZE,
      userId: userId.value ? Number(userId.value) : undefined,
      account: account.value,
      dateFrom: dateFrom.value,
      dateTo: dateTo.value,
      amountInCents: amountEuros.value ? Math.round(Number(amountEuros.value) * 100) : undefined,
      amountFilter: amountFilter.value,
    });
    transactions.value = result.content ?? [];
    totalPages.value = result.page?.totalPages ?? 0;
  } catch {
    error.value = "Failed to load transactions.";
  } finally {
    loading.value = false;
  }
}

onMounted(loadTransactions);

function goToPage(index: number) {
  pageIndex.value = index;
  loadTransactions();
}

function search() {
  pageIndex.value = 0;
  loadTransactions();
}

function resetFilters() {
  userId.value = "";
  account.value = "";
  dateFrom.value = "";
  dateTo.value = "";
  amountEuros.value = "";
  amountFilter.value = "EqualTo";
  search();
}

const hasFilters = () =>
  !!(userId.value || account.value || dateFrom.value || dateTo.value || amountEuros.value);

// New transfer modal
const showTransfer = ref(false);
const transferFrom = ref("");
const transferTo = ref("");
const transferAmountEuros = ref("");
const submitting = ref(false);
const modalError = ref("");
const successMessage = ref("");

function openTransfer() {
  transferFrom.value = "";
  transferTo.value = "";
  transferAmountEuros.value = "";
  modalError.value = "";
  showTransfer.value = true;
}

function closeTransfer() {
  showTransfer.value = false;
}

async function submitTransfer() {
  submitting.value = true;
  modalError.value = "";
  try {
    await api.transactions.create({
      from: transferFrom.value,
      to: transferTo.value,
      amountInCents: Math.round(Number(transferAmountEuros.value) * 100),
    });
    showTransfer.value = false;
    successMessage.value = "Transfer completed successfully.";
    pageIndex.value = 0;
    await loadTransactions();
  } catch (e) {
    modalError.value = e instanceof Error ? e.message : "Failed to execute transfer. Please try again.";
  } finally {
    submitting.value = false;
  }
}

</script>

<template>
  <div class="header">
    <hgroup>
      <h2>Transactions</h2>
      <p>View all transactions across the system</p>
    </hgroup>
    <button type="button" @click="openTransfer">New transfer</button>
  </div>

  <form class="search" @submit.prevent="search">
    <input v-model.trim="userId" type="number" min="1" placeholder="User ID" />
    <input v-model.trim="account" type="text" placeholder="IBAN or Account Number" />
    <label>
      From
      <input v-model="dateFrom" type="date" />
    </label>
    <label>
      To
      <input v-model="dateTo" type="date" />
    </label>
    <label>
      Amount (€)
      <div class="amount-filter">
        <select v-model="amountFilter" aria-label="Amount comparison">
          <option value="LessThan">&lt;</option>
          <option value="EqualTo">=</option>
          <option value="GreaterThan">&gt;</option>
        </select>
        <input v-model.trim="amountEuros" type="number" min="0" step="0.01" placeholder="" />
      </div>
    </label>
    <button type="submit">Search</button>
    <button type="button" class="secondary" :disabled="!hasFilters()" @click="resetFilters">
      Reset
    </button>
  </form>

  <article>
    <p v-if="loading" aria-busy="true">Loading transactions…</p>
    <p v-else-if="error">{{ error }}</p>
    <p v-else-if="transactions.length === 0">No transactions found.</p>

    <table v-else>
      <thead>
        <tr>
          <th scope="col">From</th>
          <th scope="col">To</th>
          <th scope="col">Amount</th>
          <th scope="col">Timestamp</th>
          <th scope="col">Initiated by</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="tx in transactions" :key="tx.transactionId">
          <td>
            {{ tx.from.identifier }}
            <small>{{ tx.from.ownerFirstName }} {{ tx.from.ownerLastName }} ({{ tx.from.ownerId }})</small>
          </td>
          <td>
            {{ tx.to.identifier }}
            <small>{{ tx.to.ownerFirstName }} {{ tx.to.ownerLastName }} ({{ tx.to.ownerId }})</small>
          </td>
          <td>{{ formatCents(tx.amountInCents) }}</td>
          <td>{{ dateFormat.format(new Date(tx.timestamp)) }}</td>
          <td>{{ tx.initiatedBy }}</td>
        </tr>
      </tbody>
    </table>

    <nav v-if="!loading && totalPages > 1" class="pagination">
      <button class="secondary" :disabled="pageIndex === 0" @click="goToPage(pageIndex - 1)">
        &lt; Prev
      </button>
      <span>Page {{ pageIndex + 1 }} of {{ totalPages }}</span>
      <button class="secondary" :disabled="pageIndex >= totalPages - 1" @click="goToPage(pageIndex + 1)">
        Next &gt;
      </button>
    </nav>
  </article>

  <dialog :open="showTransfer">
    <article>
      <header>
        <button aria-label="Close" rel="prev" @click="closeTransfer"></button>
        <p><strong>New transfer</strong></p>
      </header>

      <form @submit.prevent="submitTransfer">
        <label>
          From IBAN
          <input v-model.trim="transferFrom" type="text" placeholder="IBAN" required />
        </label>
        <label>
          To IBAN
          <input v-model.trim="transferTo" type="text" placeholder="IBAN" required />
        </label>
        <label>
          Amount (€)
          <input v-model.trim="transferAmountEuros" type="number" min="0.01" step="0.01" required />
        </label>

        <p v-if="modalError" class="error">{{ modalError }}</p>

        <footer>
          <button type="button" class="secondary" :disabled="submitting" @click="closeTransfer">
            Cancel
          </button>
          <button type="submit" :aria-busy="submitting" :disabled="submitting">
            Transfer
          </button>
        </footer>
      </form>
    </article>
  </dialog>

  <dialog :open="successMessage !== ''">
    <article>
      <header>
        <button aria-label="Close" rel="prev" @click="successMessage = ''"></button>
        <p><strong>Success</strong></p>
      </header>
      <p>{{ successMessage }}</p>
      <footer>
        <button @click="successMessage = ''">OK</button>
      </footer>
    </article>
  </dialog>
</template>

<style scoped>
.header {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 1rem;
}

.header button {
  width: auto;
  flex: none;
}

.search {
  display: flex;
  gap: 1rem;
  align-items: end;
  padding-bottom: 1rem;
}

.search input,
.search select {
  margin: 0;
}

.search label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.875rem;
  margin: 0;
}

td small {
  display: block;
  color: var(--pico-muted-color);
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

.search button {
  margin: 0;
  width: auto;
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
</style>
