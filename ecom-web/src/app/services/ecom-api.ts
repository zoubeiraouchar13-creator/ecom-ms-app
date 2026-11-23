import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { GATEWAY_URL } from '../config';
import { Bill, Customer, Product } from '../models';

/**
 * Appels aux micro-services à travers la Gateway.
 * customer-service et inventory-service exposent leurs données avec Spring Data REST (format HAL) :
 *   { "_embedded": { "customers": [ { name, email, _links: { self: { href: ".../customers/1" } } } ] } }
 * Spring Data REST n'expose pas l'id par défaut : on le récupère depuis le lien "self".
 */
@Injectable({ providedIn: 'root' })
export class EcomApi {
  private http = inject(HttpClient);

  getCustomers(): Observable<Customer[]> {
    return this.http
      .get<any>(`${GATEWAY_URL}/CUSTOMER-SERVICE/api/customers?size=100`)
      .pipe(map((res) => this.fromHal<Customer>(res, 'customers')));
  }

  getProducts(): Observable<Product[]> {
    return this.http
      .get<any>(`${GATEWAY_URL}/INVENTORY-SERVICE/api/products?size=100`)
      .pipe(map((res) => this.fromHal<Product>(res, 'products')));
  }

  getBills(): Observable<Bill[]> {
    return this.http.get<Bill[]>(`${GATEWAY_URL}/BILLING-SERVICE/api/fullBills`);
  }

  getBill(id: number): Observable<Bill> {
    return this.http.get<Bill>(`${GATEWAY_URL}/BILLING-SERVICE/api/fullBills/${id}`);
  }

  private fromHal<T>(res: any, key: string): T[] {
    const items: any[] = res?._embedded?.[key] ?? [];
    return items.map((it) => ({
      ...it,
      id: it.id ?? Number(String(it._links?.self?.href ?? '').split('/').pop()),
    }));
  }
}
