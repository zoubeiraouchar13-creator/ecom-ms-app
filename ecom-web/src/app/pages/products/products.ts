import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { EcomApi } from '../../services/ecom-api';
import { Product } from '../../models';

@Component({
  selector: 'app-products',
  imports: [CurrencyPipe],
  template: `
    <div class="page-head">
      <h1>Produits</h1>
      <input
        class="search"
        placeholder="Rechercher un produit…"
        [value]="keyword()"
        (input)="keyword.set($any($event.target).value)"
      />
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
            <th>ID</th>
            <th>Nom</th>
            <th class="right">Prix (MAD)</th>
            <th class="right">Stock</th>
          </tr>
        </thead>
        <tbody>
          @for (p of filtered(); track p.id) {
            <tr>
              <td>{{ p.id }}</td>
              <td>{{ p.name }}</td>
              <td class="right">{{ p.price | currency: 'MAD' : 'code' }}</td>
              <td class="right">
                <span class="badge" [class.low]="p.quantity < 10">{{ p.quantity }}</span>
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="4" class="muted">Aucun produit.</td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class Products {
  private api = inject(EcomApi);
  products = signal<Product[]>([]);
  keyword = signal('');
  loading = signal(true);
  error = signal('');

  filtered = computed(() => {
    const k = this.keyword().toLowerCase();
    return this.products().filter((p) => p.name.toLowerCase().includes(k));
  });

  constructor() {
    this.api.getProducts().subscribe({
      next: (data) => {
        this.products.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(
          `Impossible de charger les produits (${err.status || 'gateway injoignable'}).`,
        );
        this.loading.set(false);
      },
    });
  }
}
