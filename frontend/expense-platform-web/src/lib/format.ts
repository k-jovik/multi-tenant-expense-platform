export function formatCurrency(amountMinor: number): string {
  return `$${(amountMinor / 100).toFixed(2)}`;
}