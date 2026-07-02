import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import {Transaction} from '../../models/transaction.model';
import {AccountService} from '../../services/account.service';

@Component({
  selector: 'app-transaction-overview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './transaction-overview.component.html',
  styleUrl: './transaction-overview.component.scss'
})
export class TransactionOverviewComponent implements OnInit {
  accountId!: number;
  transactionId!: number;
  transaction: Transaction | null = null;

  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    this.accountId = Number(this.route.snapshot.paramMap.get('accountId'));
    this.transactionId = Number(this.route.snapshot.paramMap.get('transactionId'));
    this.loadTransaction();
  }

  loadTransaction(): void {
    this.loading = true;
    this.accountService.getTransaction(this.accountId, this.transactionId).subscribe({
      next: (tx) => {
        this.transaction = tx;
        this.loading = false;
      },
      error: (err) => {
        this.error = this.accountService.getErrorMessage(err);
        this.loading = false;
        console.error(err);
      }
    });
  }

  isDebit(): boolean {
    return this.transaction?.type === 'WITHDRAWAL' || this.transaction?.type === 'EXCHANGE_OUT';
  }

  goBack(): void {
    this.router.navigate(['/accounts', this.accountId]);
  }

  exportToPdf(): void {
    if (!this.transaction) {
      return;
    }

    const doc = new jsPDF();
    const tx = this.transaction;

    doc.setFontSize(18);
    doc.text('Transaction Summary', 14, 20);

    doc.setFontSize(11);
    doc.setTextColor(100);
    doc.text(`Generated on ${new Date().toLocaleString()}`, 14, 27);

    autoTable(doc, {
      startY: 35,
      head: [['Field', 'Value']],
      body: [
        ['Transaction ID', String(tx.id)],
        ['Type', tx.type],
        ['Amount', `${this.isDebit() ? '-' : '+'}${tx.amount.toFixed(2)} ${tx.currency}`],
        ['Balance After', `${tx.balanceAfter.toFixed(2)} ${tx.currency}`],
        ['Description', tx.description || '-'],
        ['Date', new Date(tx.createdAt).toLocaleString()],
        ['Account ID', String(this.accountId)],
      ],
      styles: { fontSize: 11 },
      headStyles: { fillColor: [44, 62, 80] },
    });

    doc.save(`transaction-${tx.id}.pdf`);
  }
}
