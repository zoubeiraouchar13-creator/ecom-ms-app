import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { EcomApi } from '../../services/ecom-api';
import { Bill } from '../../models';
import { billTotal } from '../bill-total';

@Component({
  selector: 'app-bills',
  imports: [RouterLink, DatePipe, CurrencyPipe],
  template: `
    <div class="page-head">
      <h1>Factures</h1>
      @if (customerId()) {
        <a class="btn" routerLink="/bills">Voir toutes les factures</a>
      }
    </div>

    @if (error()) {
      <p class="alert">{{ error() }}</p>
    }
    @if (loading()) {
      <p class="muted">Chargement…</p>
    }

    @if (!loading() && !error()) {
      <table class="table">
        <thead>
          <tr>
            <th>N°</th>
            <th>Date</th>
            <th>Client</th>
            <th class="right">Lignes</th>
            <th class="right">Total (MAD)</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          @for (b of filtered(); track b.id) {
            <tr>
              <td>{{ b.id }}</td>
              <td>{{ b.billingDate | date: 'dd/MM/yyyy HH:mm' }}</td>
              <td>{{ b.customer?.name ?? b.customerId }}</td>
              <td class="right">{{ b.productItems.length }}</td>
              <td class="right">{{ total(b) | currency: 'MAD' : 'code' }}</td>
              <td class="right"><a class="btn" [routerLink]="['/bills', b.id]">Détails</a></td>
            </tr>
          } @empty {
            <tr>
              <td colspan="6" class="muted">Aucune facture.</td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class Bills {
  private api = inject(EcomApi);
  private route = inject(ActivatedRoute);

  bills = signal<Bill[]>([]);
  loading = signal(true);
  error = signal('');
  customerId = toSignal(this.route.queryParamMap.pipe(map((p) => p.get('customerId'))));
  total = billTotal;

  filtered = computed(() => {
    const id = this.customerId();
    return id ? this.bills().filter((b) => String(b.customerId) === id) : this.bills();
  });

  constructor() {
    this.api.getBills().subscribe({
      next: (data) => {
        this.bills.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(
          `Impossible de charger les factures (${err.status || 'gateway injoignable'}).`,
        );
        this.loading.set(false);
      },
    });
  }
}
