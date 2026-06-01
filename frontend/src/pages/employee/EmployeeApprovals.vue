<script setup lang="ts">
import { ref, onMounted } from "vue";
import { api } from "@/api";
import type { User } from "@/types/api";

const users = ref<User[]>([]);
const loading = ref(true);
const error = ref("");

const dateFormat = new Intl.DateTimeFormat("en-NL", { dateStyle: "medium", timeStyle: "long" });

const PAGE_SIZE = 10;
const pageIndex = ref(0);
const totalPages = ref(0);

async function loadUsers() {
  loading.value = true;
  error.value = "";
  try {
    const result = await api.users.list({ page: pageIndex.value, size: PAGE_SIZE, isApproved: false });
    users.value = result.content ?? [];
    totalPages.value = result.page?.totalPages ?? 0;

    // If the last user on a page was just approved, step back to the previous page.
    if (users.value.length === 0 && pageIndex.value > 0) {
      pageIndex.value--;
      await loadUsers();
    }
  } catch {
    error.value = "Failed to load users.";
  } finally {
    loading.value = false;
  }
}

function goToPage(index: number) {
  pageIndex.value = index;
  loadUsers();
}

onMounted(loadUsers);

// The user whose approval modal is open (null = closed)
const selected = ref<User | null>(null);
const submitting = ref(false);
const modalError = ref("");
const successMessage = ref("");
const dailyLimitEuros = ref(500);
const absoluteLimitEuros = ref(0);

function openApprove(user: User) {
  selected.value = user;
  dailyLimitEuros.value = 500;
  absoluteLimitEuros.value = 0;
  modalError.value = "";
}

function closeApprove() {
  selected.value = null;
}

async function confirmApprove() {
  if (!selected.value) return;
  const user = selected.value;

  submitting.value = true;
  modalError.value = "";
  try {
    const resp = await api.accounts.approve({
      userId: user.userId,
      dailyLimitInCents: Math.round(dailyLimitEuros.value * 100),
      absoluteLimitInCents: Math.round(absoluteLimitEuros.value * 100),
    });
    // Close the approval modal and show a success message, re-fetch the unapproved users
    selected.value = null;
    successMessage.value = resp.message;
    await loadUsers();
  } catch (e) {
    modalError.value = e instanceof Error ? e.message : "Failed to approve user. Please try again.";
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <hgroup>
    <h2>Approve Users</h2>
    <p>Review users requesting approval</p>
  </hgroup>

  <article>
    <p v-if="loading" aria-busy="true">Loading users…</p>
    <p v-else-if="error">{{ error }}</p>
    <p v-else-if="users.length === 0">No users awaiting approval.</p>

    <table v-else>
      <thead>
        <tr>
          <th scope="col">Name</th>
          <th scope="col">Email</th>
          <th scope="col">Phone</th>
          <th scope="col">BSN</th>
          <th scope="col">Registration Date</th>
          <th scope="col">Manage</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.userId">
          <td>{{ user.firstName }} {{ user.lastName }}</td>
          <td>{{ user.email }}</td>
          <td>{{ user.phoneNumber }}</td>
          <td>{{ user.bsn }}</td>
          <td>{{ dateFormat.format(new Date(user.registrationDate)) }}</td>
          <td>
            <button class="approve" @click="openApprove(user)">Approve</button>
          </td>
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

  <dialog :open="selected !== null">
    <article v-if="selected">
      <header>
        <button aria-label="Close" rel="prev" @click="closeApprove"></button>
        <p>
          <strong>Approve {{ selected.firstName }} {{ selected.lastName }}</strong>
        </p>
      </header>

      <form @submit.prevent="confirmApprove">
        <label>
          Daily transfer limit (€)
          <input v-model.number="dailyLimitEuros" type="number" min="0" required />
        </label>
        <label>
          Absolute limit (€)
          <input v-model.number="absoluteLimitEuros" type="number" required />
        </label>

        <p v-if="modalError" class="error">{{ modalError }}</p>

        <footer>
          <button type="button" class="secondary" :disabled="submitting" @click="closeApprove">
            Cancel
          </button>
          <button type="submit" class="approve" :aria-busy="submitting" :disabled="submitting">
            Approve &amp; create accounts
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
.approve {
  --pico-background-color: var(--pico-ins-color);
  --pico-border-color: var(--pico-ins-color);
  margin: 0;
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
