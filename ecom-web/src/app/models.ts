export interface Customer {
  id: number;
  name: string;
  email: string;
}

export interface Product {
  id: number;
  name: string;
  price: number;
  quantity: number;
}

export interface ProductItem {
  id: number;
  productId: number;
  quantity: number;
  price: number;
  product?: Product;
}

export interface Bill {
  id: number;
  billingDate: string;
  customerId: number;
  customer?: Customer;
  productItems: ProductItem[];
}

export interface ChatMessage {
  from: 'user' | 'agent' | 'error';
  text: string;
}
