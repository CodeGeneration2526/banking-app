<script setup lang="ts">
import { ref } from "vue";
import EmployeeAccounts from "@/pages/employee/EmployeeAccounts.vue";
import EmployeeApprovals from "@/pages/employee/EmployeeApprovals.vue";
import EmployeeTransactions from "@/pages/employee/EmployeeTransactions.vue";

// All the sidebar entries and corresponding components
const sections = [
    { key: "approvals", label: "Approve Users", component: EmployeeApprovals },
    { key: "accounts", label: "Accounts", component: EmployeeAccounts },
    { key: "transactions", label: "Transactions", component: EmployeeTransactions },
] as const;

type Section = (typeof sections)[number];
const active = ref<Section>(sections[0]);

</script>

<template>
    <div class="dashboard">
        <aside>
            <nav>
                <ul>
                    <li v-for="section in sections" :key="section.key">
                        <a
                            href="#"
                            :aria-current="section.key === active.key ? 'page' : undefined"
                            @click.prevent="active = section"
                        >{{ section.label }}</a>
                    </li>
                </ul>
            </nav>
        </aside>

        <section>
            <component :is="active.component" />
        </section>
    </div>
</template>

<style scoped>
.dashboard {
    display: grid;
    grid-template-columns: 14rem 1fr;
    gap: 2rem;
}

aside nav ul {
    flex-direction: column;
}

aside nav a[aria-current="page"] {
    background: var(--pico-primary-background);
    color: var(--pico-primary-inverse);
    border-radius: var(--pico-border-radius);
}
</style>
