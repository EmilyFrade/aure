export function digitsOnly(value: string): string {
  return value.replace(/\D/g, '');
}

export function maskPhoneBR(value: string): string {
  const d = digitsOnly(value).slice(0, 11);
  if (d.length === 0) return '';
  if (d.length <= 2) return `(${d}`;
  if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`;
  if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
  return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
}

export function maskCurrencyBR(value: string): string {
  const d = digitsOnly(value).replace(/^0+/, '').slice(0, 8);
  if (d.length === 0) return '';
  const cents = d.padStart(3, '0');
  const reais = cents.slice(0, -2).replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${reais},${cents.slice(-2)}`;
}

export function parseCurrencyBR(value: string): number {
  const d = digitsOnly(value);
  return d ? Number(d) / 100 : 0;
}

export function stripSpaces(value: string): string {
  return value.replace(/\s/g, '');
}
