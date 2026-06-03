const API_BASE_URL = import.meta.env.VITE_API_URL;

import type {
    LoginResponse,
    RegisterRequest,
    User,
    UsersPage,
    NewAccountRequest,
    ApiMessage,
    AccountsPage,
    AccountDetail,
    UpdateAccountRequest,
    UserPatchRequest,
    TransactionsPage,
    TransactionRequest,
    Transaction,
    AmountFilter,
} from "@/types/api";
import { useAuthStore } from "@/stores/auth";
import router from "@/router";

export class ApiError extends Error {
    status: number;
    constructor(status: number, message: string) {
        super(message);
        this.status = status;
    }
}

async function request<T>(path: string, options: RequestInit): Promise<T> {
    const auth = useAuthStore();
    
    const headers = new Headers(options.headers);
    headers.append("Content-Type", "application/json");

    if (auth.token) headers.append("Authorization", "Bearer " + auth.token);

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers,
    });

    if (!response.ok) {
        // Most errors return { message }, @Valid failures return a field->message map, account for both states
        const body = await response.json();
        const message = body.message ?? Object.values(body).join(", ");

        // Check for token expiration (Auth check is because backend sends 401 for auth policy rejections too)
        if (response.status === 401 && !auth.currentUser) {
            auth.clearToken();
            router.push({ name: "login" });
        }

        throw new Error(message);
    }

    return response.json();
}

interface Pagable {
    page?: number,
    size?: number,
    sort?: string,
}

export const api = {
    auth: {
        login(email: string, password: string) {
            return request<LoginResponse>("/auth/login", {
                method: "POST",
                body: JSON.stringify({ email, password }),
            });
        },
        register: (body: RegisterRequest) =>
            request<User>("/auth/register", {
                method: "POST",
                body: JSON.stringify(body),
            }),
    },
    users: {
        // Fetch currently logged in user
        me: () => request<User>("/users/me", { method: "GET" }),
        // Fetch all users with params to check role and approval status
        list: (params: { page?: number; size?: number; isApproved?: boolean; role?: User["role"] } = {}) => {
            const query = new URLSearchParams();
            if (params.page !== undefined) query.set("page", String(params.page));
            if (params.size !== undefined) query.set("size", String(params.size));
            if (params.isApproved !== undefined) query.set("isApproved", String(params.isApproved));
            if (params.role !== undefined) query.set("role", params.role);
            const qs = query.toString();
            return request<UsersPage>(`/users${qs ? `?${qs}` : ""}`, { method: "GET" });
        },
        // Update a user's first name, last name, email, or closed status
        update: (userId: number, body: UserPatchRequest) =>
            request<User>(`/users/${userId}`, {
                method: "PATCH",
                body: JSON.stringify(body),
            }),
    },
    accounts: {
        // Fetch all accounts with params to search
        list: (params: { firstName?: string, lastName?: string, iban?: string, ownerUserId?: number } & Pagable = {}) => {
            const query = new URLSearchParams();

            if (params.page !== undefined) query.set("page", String(params.page));
            if (params.size !== undefined) query.set("size", String(params.size));
            if (params.sort !== undefined) query.set("sort", params.sort);

            if (params.firstName) query.set("firstName", params.firstName);
            if (params.lastName) query.set("lastName", params.lastName);
            if (params.iban) query.set("iban", params.iban);
            if (params.ownerUserId) query.set("ownerUserId", String(params.ownerUserId));

            const qs = query.toString();
            return request<AccountsPage>(`/accounts${qs ? `?${qs}` : ""}`, { method: "GET" });
        },
        // Fetch a single account's details (balance + limits)
        get: (accountId: number) => request<AccountDetail>(`/accounts/${accountId}`, { method: "GET" }),
        // Update an account's limits and/or closed status
        update: (accountId: number, body: UpdateAccountRequest) =>
            request<AccountDetail>(`/accounts/${accountId}`, {
                method: "PATCH",
                body: JSON.stringify(body),
            }),
        // Approve a pending customer and create their checking + savings accounts
        approve: (body: NewAccountRequest) =>
            request<ApiMessage>("/accounts", {
                method: "POST",
                body: JSON.stringify(body),
            }),
    },
    transactions: {
        list: (
            params: {
                page?: number;
                size?: number;
                userId?: number;
                account?: string;
                dateFrom?: string;
                dateTo?: string;
                amountInCents?: number;
                amountFilter?: AmountFilter;
            } = {},
        ) => {
            const query = new URLSearchParams();
            if (params.page !== undefined) query.set("page", String(params.page));
            if (params.size !== undefined) query.set("size", String(params.size));
            if (params.userId !== undefined) query.set("userId", String(params.userId));
            if (params.account) query.set("account", params.account);
            if (params.dateFrom) query.set("dateFrom", params.dateFrom);
            if (params.dateTo) query.set("dateTo", params.dateTo);
            if (params.amountInCents !== undefined) {
                query.set("amountInCents", String(params.amountInCents));
                if (params.amountFilter) query.set("amountFilter", params.amountFilter);
            }
            const qs = query.toString();
            return request<TransactionsPage>(`/transactions${qs ? `?${qs}` : ""}`, { method: "GET" });
        },
        // Execute an employee-initiated transfer between two accounts (IBAN or account number)
        create: (body: TransactionRequest) =>
            request<Transaction>("/transactions", {
                method: "POST",
                body: JSON.stringify(body),
            }),
    },
};
