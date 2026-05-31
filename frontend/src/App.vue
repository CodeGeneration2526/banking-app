<script setup lang="ts">
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const router = useRouter();

function handleLogout() {
    auth.clearToken();
    router.push({ name: "login" });
}

</script>

<template>
    <header>
        <nav>
            <ul>
                <li><strong>Bank</strong></li>
            </ul>
            <ul>
                <li><RouterLink to="/" class="contrast">Home</RouterLink></li>
                <li v-if="auth.isEmployee"><RouterLink to="/employee" class="contrast">Employee Dashboard</RouterLink></li>
                <li v-if="auth.isAuthenticated">
                    <details class="dropdown">
                        <summary class="contrast">Account</summary>
                        <ul>
                            <li><a href="#" @click.prevent="handleLogout">Logout</a></li>
                        </ul>
                    </details>
                </li>
                <li v-else><RouterLink to="/login" class="contrast">Login</RouterLink></li>
            </ul>
        </nav>
    </header>
    <main>
        <RouterView />
    </main>
    <footer>
        <small>&#169; Jonathan Mauricio<br></small>
        <small>&#169; Kunal Dandekar<br></small>
        <small>Licensed under the GNU <a class="secondary" target="_blank" href="https://www.gnu.org/licenses/agpl-3.0.en.html">AGPL-3.0-or-later</a> license</small>
    </footer>
</template>

<style scoped></style>
