import {Component, OnInit} from '@angular/core';
import {CommonModule, DecimalPipe} from '@angular/common';
import {Account} from '../../models/account.model';
import {AccountService} from '../../services/account.service';
import {Router} from '@angular/router';


@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  accounts: Account[] = [];
  loading = true;
  error: string | null = null;

  constructor(private accountService: AccountService, private router: Router) {}

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.loading = true;
    this.error = null;

    this.accountService.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.loading = false;
      },
      error: (err) => {
        this.error = this.accountService.getErrorMessage(err);
        this.loading = false;
        console.error(err);
      }
    });
  }

  openAccount(accountId: number): void {
    this.router.navigate(['/accounts', accountId]);
  }
}
