import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import { Observable } from 'rxjs';
import { Account } from '../models/account.model';
import { PageResponse, Transaction } from '../models/transaction.model';
import {BalancePoint} from '../models/balance-point.model';
import {ErrorResponse} from '../models/error-response.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly baseUrl = 'http://localhost:8090/api/v1/accounts';

  constructor(private http: HttpClient) {}

  getAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(this.baseUrl);
  }

  getAccount(accountId: number): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/${accountId}/balance`);
  }

  getTransactions(accountId: number, page: number, size: number): Observable<PageResponse<Transaction>> {
    return this.http.get<PageResponse<Transaction>>(
      `${this.baseUrl}/${accountId}/transactions`,
      { params: { page, size } }
    );
  }

  getTransaction(accountId: number, transactionId: number): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.baseUrl}/${accountId}/transactions/${transactionId}`);
  }

  getBalanceHistory(accountId: number): Observable<BalancePoint[]> {
    return this.http.get<BalancePoint[]>(`${this.baseUrl}/${accountId}/balance-history`);
  }

  getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const backendError = error.error as Partial<ErrorResponse> | null;

      if (backendError?.message) {
        return backendError.message;
      }

      if (error.status === 0) {
        return 'Backend is not available. Please try again later.';
      }

      return `Request failed with status ${error.status}`;
    }

    return 'Something went wrong. Please try again later.';
  }
}
