import { Routes } from '@angular/router';
import { Customers } from './pages/customers/customers';
import { Products } from './pages/products/products';
import { Bills } from './pages/bills/bills';
import { BillDetails } from './pages/bill-details/bill-details';
import { Chat } from './pages/chat/chat';

export const routes: Routes = [
  { path: '', redirectTo: 'chat', pathMatch: 'full' },
  { path: 'customers', component: Customers, title: 'Clients' },
  { path: 'products', component: Products, title: 'Produits' },
  { path: 'bills', component: Bills, title: 'Factures' },
  { path: 'bills/:id', component: BillDetails, title: 'Détails facture' },
  { path: 'chat', component: Chat, title: 'Assistant IA' },
  { path: '**', redirectTo: 'chat' },
];
