<script setup lang="ts"></script>

<template>
    <header>
        <nav>
            <ul>
                <li><strong>Bank</strong></li>
            </ul>
            <ul>
                <li><RouterLink to="/" class="contrast">Home</RouterLink></li>
                <li><RouterLink to="/login" class="contrast">Login</RouterLink></li>
<!--              Change this to require auth (for users  too) later-->
                <li><RouterLink to="/employee" class="contrast">Employee Dashboard</RouterLink></li>
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
