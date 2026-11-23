import { Component, inject, input, OnInit, signal } from '@angular/core';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EcomApi } from '../../services/ecom-api';
import { Bill } from '../../models';
import { billTotal } from '../bill-total';

@Component({
  selector: 'app-bill-details',
  imports: [RouterLink, DatePipe, CurrencyPipe],
  template: `
    <div class="page-head">
      <h1>Facture n° {{ id() }}</h1>
      <a class="btn" routerLink="/bills">← Retour</a>
    </div>

    @if (error()) {
      <p class="alert">{{ error() }}</p>
    }

    @if (bill(); as b) {
      <div class="card">
        <p><strong>Date :</strong> {{ b.billingDate | date: 'dd/MM/yyyy HH:mm' }}</p>
        <p><strong>Client :</strong> {{ b.customer?.name }} ({{ b.customer?.email }})</p>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>Produit</th>
            <th class="right">Prix unitaire</th>
            <th class="right">Quantité</th>
            <th class="right">Montant</th>
          </tr>
        </thead>
        <tbody>
          @for (pi of b.productItems; track pi.id) {
            <tr>
              <td>{{ pi.product?.name ?? 'Produit ' + pi.productId }}</td>
              <td class="right">{{ pi.price | currency: 'MAD' : 'code' }}</td>
              <td class="right">{{ pi.quantity }}</td>
              <td class="right">{{ pi.price * pi.quantity | currency: 'MAD' : 'code' }}</td>
            </tr>
          }
        </tbody>
        <tfoot>
          <tr>
            <td colspan="3" class="right"><strong>Total</strong></td>
            <td class="right">
              <strong>{{ total(b) | currency: 'MAD' : 'code' }} MAD</strong>
            </td>
          </tr>
        </tfoot>
      </table>
    }
  `,
})
export class BillDetails implements OnInit {
  private api = inject(EcomApi);
  id = input.required<string>(); // paramètre de route :id (withComponentInputBinding)
  bill = signal<Bill | null>(null);
  error = signal('');
  total = billTotal;

  ngOnInit(): void {
    this.api.getBill(Number(this.id())).subscribe({
      next: (b) => this.bill.set(b),
      error: (err) =>
        this.error.set(
          `Impossible de charger la facture (${err.status || 'gateway injoignable'}).`,
        ),
    });
  }
}
