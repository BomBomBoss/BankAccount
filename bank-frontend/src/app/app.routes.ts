import { Routes } from '@angular/router';
import {HomeComponent} from './modules/home/home.component';
import {AccountOverviewComponent} from './modules/account-overview/account-overview.component';
import {TransactionOverviewComponent} from './modules/transaction-overview/transaction-overview.component';


export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'accounts/:id', component: AccountOverviewComponent },
  { path: 'accounts/:accountId/transactions/:transactionId', component: TransactionOverviewComponent },
];
