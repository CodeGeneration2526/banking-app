<script setup lang="ts">
import { ref, onMounted } from "vue";
import { api } from "@/api";
import type { User } from "@/types/api";

const users = ref<User[]>([]);
const loading = ref(true);
const error = ref("");

const dateFormat = new Intl.DateTimeFormat("en-nl", { dateStyle: "medium", timeStyle: "long" });

const PAGE_SIZE = 10;
const pageIndex = ref(0);
const totalPages = ref(0);

async function loadUsers() {
  loading.value = true;
  error.value = "";
  try {
    const result = await api.users.list({ page: pageIndex.value, size: PAGE_SIZE, isApproved: true });
    users.value = result.content ?? [];
    totalPages.value = result.page?.totalPages ?? 0;

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

// Edit modal
const selected = ref<User | null>(null);
const submitting = ref(false);
const modalError = ref("");
const editFirstName = ref("");
const editLastName = ref("");
const editEmail = ref("");
const editClosed = ref(false);
const successMessage = ref("");

function openEdit(user: User) {
  selected.value = user;
  editFirstName.value = user.firstName;
  editLastName.value = user.lastName;
  editEmail.value = user.email;
  editClosed.value = user.closed;
  modalError.value = "";
}

function closeEdit() {
  selected.value = null;
}

async function saveEdit() {
  if (!selected.value) return;

  submitting.value = true;
  modalError.value = "";
  try {
    await api.users.update(selected.value.userId, {
      firstName: editFirstName.value,
      lastName: editLastName.value,
      email: editEmail.value,
    });
    selected.value = null;
    successMessage.value = "User details updated successfully.";
    await loadUsers();
  } catch (e) {
    modalError.value = e instanceof Error ? e.message : "Failed to update user. Please try again.";
  } finally {
    submitting.value = false;
  }
}

async function toggleClosed() {
  if (!selected.value) return;

  const reopen = editClosed.value;
  const action = reopen ? "reopen" : "close";
  if (!window.confirm(`Are you sure you want to ${action} this user?`)) return;

  submitting.value = true;
  modalError.value = "";
  try {
    await api.users.update(selected.value.userId, { closed: !reopen });
    selected.value = null;
    successMessage.value = reopen ? "User reopened successfully." : "User closed successfully.";
    await loadUsers();
  } catch (e) {
    modalError.value = e instanceof Error ? e.message : `Failed to ${action} user. Please try again.`;
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <hgroup>
    <h2>View all Users</h2>
    <p>View all users and manage them</p>
  </hgroup>

  <article>
    <p v-if="loading" aria-busy="true">Loading users…</p>
    <p v-else-if="error">{{ error }}</p>
    <p v-else-if="users.length === 0">No users found.</p>

    <table v-else>
      <thead>
      <tr>
        <th scope="col">Name</th>
        <th scope="col">Email</th>
        <th scope="col">Phone</th>
        <th scope="col">BSN</th>
        <th scope="col">Registration Date</th>
        <th scope="col">Role</th>
        <th scope="col">Approved By (ID)</th>
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
        <td>{{ user.role }}</td>
        <td>{{ user.approvedBy }}</td>
        <td>
          <button class="secondary" @click="openEdit(user)">Edit</button>
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
          <strong>Edit {{ selected.firstName }} {{ selected.lastName }}</strong>
        </p>
      </header>

      <form @submit.prevent="saveEdit">
        <p v-if="editClosed" class="error"><strong>This user is closed.</strong> Reopen them to edit their details.</p>

        <label>
          First name
          <input v-model.trim="editFirstName" type="text" minlength="2" maxlength="50" :disabled="editClosed" required />
        </label>
        <label>
          Last name
          <input v-model.trim="editLastName" type="text" minlength="2" maxlength="50" :disabled="editClosed" required />
        </label>
        <label>
          Email
          <input v-model.trim="editEmail" type="email" :disabled="editClosed" required />
        </label>

        <p v-if="modalError" class="error">{{ modalError }}</p>

        <footer>
          <button type="button" class="secondary" :disabled="submitting" @click="closeEdit">
            Cancel
          </button>
          <button type="submit" :aria-busy="submitting" :disabled="submitting || editClosed">
            Save changes
          </button>
          <button type="button" :class="editClosed ? 'reopen' : 'close-user'" :disabled="submitting" @click="toggleClosed">
            {{ editClosed ? "Reopen user" : "Close user" }}
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
.error {
  color: var(--pico-del-color);
}

.closed-badge {
  color: var(--pico-del-color);
  font-weight: bold;
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

dialog footer .close-user {
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
