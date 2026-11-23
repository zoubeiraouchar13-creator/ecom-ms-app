import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EcomApi } from '../../services/ecom-api';
import { Customer } from '../../models';

@Component({
  selector: 'app-customers',
  imports: [RouterLink],
  template: `
    <div class="page-head">
      <h1>Clients</h1>
      <input class="search" placeholder="Rechercher un client…" [value]="keyword()"
             (input)="keyword.set($any($event.target).value)" />
    </div>

    @if (error()) { <p class="alert">{{ error() }}</p> }
    @if (loading()) { <p class="muted">Chargement…</p> }

    @if (!loading() && !error()) {
      <table class="table">
        <thead><tr><th>ID</th><th>Nom</th><th>Email</th><th></th></tr></thead>
        <tbody>
          @for (c of filtered(); track c.id) {
            <tr>
              <td>{{ c.id }}</td>
              <td>{{ c.name }}</td>
              <td>{{ c.email }}</td>
              <td class="right"><a class="btn" [routerLink]="['/bills']" [queryParams]="{ customerId: c.id }">Factures</a></td>
            </tr>
          } @empty {
            <tr><td colspan="4" class="muted">Aucun client.</td></tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class Customers {
  private api = inject(EcomApi);
  customers = signal<Customer[]>([]);
  keyword = signal('');
  loading = signal(true);
  error = signal('');

  filtered = computed(() => {
    const k = this.keyword().toLowerCase();
    return this.customers().filter((c) => c.name.toLowerCase().includes(k) || c.email.toLowerCase().includes(k));
  });

  constructor() {
    this.api.getCustomers().subscribe({
      next: (data) => { this.customers.set(data); this.loading.set(false); },
      error: (err) => { this.error.set(`Impossible de charger les clients (${err.status || 'gateway injoignable'}).`); this.loading.set(false); },
    });
  }
}
