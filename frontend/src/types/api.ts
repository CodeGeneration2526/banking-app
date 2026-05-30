// Ergonomic aliases over the generated OpenAPI types.
// Only alias DTOs that are used often or whose generated names are long;
// reach into `components["schemas"][...]` directly for one-off types.
import type { components } from "./schema";

type Schemas = components["schemas"];

// --- Auth ---
export type LoginRequest = Schemas["LoginRequest"];
export type LoginResponse = Schemas["LoginResponse"];
export type RegisterRequest = Schemas["RegisterRequest"];

// --- Accounts ---
export type AccountSummary = Schemas["AccountSummaryResponse"];
export type AccountDetail = Schemas["AccountDetailResponse"];
export type NewAccountRequest = Schemas["NewAccountRequest"];
export type UpdateAccountRequest = Schemas["UpdateAccountRequest"];
export type AccountsPage = Schemas["PagedModelAccountSummaryResponse"];

// --- Transactions ---
export type TransactionRequest = Schemas["TransactionRequest"];
export type Transaction = Schemas["TransactionResponse"];
export type TransactionsPage = Schemas["PagedModelTransactionResponse"];

// --- Customers / users ---
export type User = Schemas["UserResponse"];
export type UsersPage = Schemas["PagedModelUserResponse"];
export type UserPatchRequest = Schemas["UserPatchRequest"];

// --- Common ---
export type ApiMessage = Schemas["ApiResponse"];
export type PageInfo = Schemas["PageMetadata"];
