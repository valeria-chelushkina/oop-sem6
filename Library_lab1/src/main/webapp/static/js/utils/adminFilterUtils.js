export function includesText(value, q) {
  if (!q) return true;
  return String(value ?? "").toLowerCase().includes(q);
}

export function parseDateValue(value) {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date.getTime();
}

export function inDateRange(value, from, to) {
  const ts = parseDateValue(value);
  if (from && (!ts || ts < from)) return false;
  if (to && (!ts || ts > to)) return false;
  return true;
}
