export type ExpenseStatus = 'DRAFT' | 'APPROVED' | 'REJECTED' | 'SUBMITTED';
export type Role = 'ADMIN' | 'MANAGER' | 'EMPLOYEE';
export type AccountType = 'CASH' | 'PAYABLE' | 'EXPENSE';

export interface RegisterRequest {
  email: string;
  password: string;
  tenantName: string;
  fullName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
  tenantName: string;
}

export interface AuthResponse {
  token: string;
  id: string;
  role: Role;
  tenantId: string;
}

export interface ExpenseRequest {
  amountMinor: number;
  currency: string;
  category: string;
  description: string;
}

export interface Expense {
  id: string;
  amountMinor: number;
  currency: string;
  category: string;
  description: string | null;
  status: ExpenseStatus;
  rejectionReason: string | null;
}

export interface RejectRequest {
  reason: string;
}

// Accounts
export interface Account {
  id: string;
  name: string;
  type: AccountType;
  balanceMinor: number;
}

export interface ApiError {
  error: string;
}
