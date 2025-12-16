export interface StockPrice {
    symbol: string;
    price: number;
    timestamp: string;
}

export interface StockTransaction {
    id?: number;
    userId: string;
    symbol: string;
    quantity: number;
    price: number;
    createdAt?: string;
}

export interface Portfolio {
    transactions: StockTransaction[];
    totalValue: number;
}
