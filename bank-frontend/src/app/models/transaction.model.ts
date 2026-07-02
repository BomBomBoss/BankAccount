export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL' | 'EXCHANGE_IN' | 'EXCHANGE_OUT';

export interface Transaction {
  id: number;
  type: TransactionType;
  amount: number;
  currency: string;
  balanceAfter: number;
  description: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
}
