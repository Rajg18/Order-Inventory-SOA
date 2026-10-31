export function formatCurrency(amount) {
  if (amount === undefined || amount === null) return '₹0.00';
  return `₹${Number(amount).toFixed(2)}`;
}

export function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  const date = new Date(dateTimeStr);
  return date.toLocaleString('en-IN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}
