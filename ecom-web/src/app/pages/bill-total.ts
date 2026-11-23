import { Bill } from '../models';

export function billTotal(bill: Bill): number {
  return bill.productItems.reduce((sum, pi) => sum + pi.price * pi.quantity, 0);
}
