import { Component, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ChartConfiguration, ChartData } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import {Account} from '../../models/account.model';
import {Transaction} from '../../models/transaction.model';
import {AccountService} from '../../services/account.service';

@Component({
  selector: 'app-account-overview',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './account-overview.component.html',
  styleUrl: './account-overview.component.scss'
})
export class AccountOverviewComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('scrollAnchor') scrollAnchor!: ElementRef<HTMLDivElement>;

  accountId!: number;
  account: Account | null = null;
  transactions: Transaction[] = [];

  loadingAccount = true;
  loadingTransactions = false;
  loadingChart = true;
  error: string | null = null;

  readonly pageSize = 20;
  currentPage = 0;
  isLastPage = false;

  private observer?: IntersectionObserver;


  chartData: ChartData<'line'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Balance',
        borderColor: '#2c3e50',
        backgroundColor: 'rgba(44, 62, 80, 0.08)',
        fill: true,
        tension: 0.2,
        pointRadius: 2,
      }
    ]
  };

  chartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    },
    scales: {
      x: {
        ticks: { maxTicksLimit: 6 }
      },
      y: {
        beginAtZero: false
      }
    }
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    this.accountId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadAccount();
    this.loadTransactions();
    this.loadBalanceHistory();
  }

  ngAfterViewInit(): void {
    this.setupInfiniteScroll();
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  loadAccount(): void {
    this.loadingAccount = true;
    this.accountService.getAccount(this.accountId).subscribe({
      next: (account) => {
        this.account = account;
        this.loadingAccount = false;
      },
      error: (err) => {
        this.error = this.accountService.getErrorMessage(err);
        this.loadingAccount = false;
        console.error(err);
      }
    });
  }

  loadBalanceHistory(): void {
    this.loadingChart = true;
    this.accountService.getBalanceHistory(this.accountId).subscribe({
      next: (points) => {
        this.chartData = {
          labels: points.map(p => new Date(p.date).toLocaleDateString()),
          datasets: [
            {
              ...this.chartData.datasets[0],
              data: points.map(p => p.balance)
            }
          ]
        };
        this.loadingChart = false;
      },
      error: (err) => {
        this.error = this.accountService.getErrorMessage(err);
        this.loadingChart = false;
        console.error(err);
      }
    });
  }

  loadTransactions(): void {
    if (this.loadingTransactions || this.isLastPage) {
      return;
    }

    this.loadingTransactions = true;

    this.accountService.getTransactions(this.accountId, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.transactions = [...this.transactions, ...response.content];
        this.isLastPage = response.last;
        this.currentPage++;
        this.loadingTransactions = false;
      },
      error: (err) => {
        this.error = this.accountService.getErrorMessage(err);
        this.loadingTransactions = false;
        console.error(err);
      }
    });
  }

  private setupInfiniteScroll(): void {
    this.observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        if (entry.isIntersecting && !this.loadingTransactions && !this.isLastPage) {
          this.loadTransactions();
        }
      },
      { root: null, rootMargin: '100px', threshold: 0 }
    );

    this.observer.observe(this.scrollAnchor.nativeElement);
  }

  openTransaction(transactionId: number): void {
    this.router.navigate(['/accounts', this.accountId, 'transactions', transactionId]);
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
