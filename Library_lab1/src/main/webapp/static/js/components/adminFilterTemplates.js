export const BOOK_ITEM_STATUSES = [
  "AVAILABLE",
  "ORDERED",
  "ISSUED",
  "LOST",
  "DAMAGED",
  "ARCHIVED",
  "READING_ROOM_ONLY",
];

export const LOAN_TYPES = ["SUBSCRIPTION", "READING_ROOM"];
export const LOAN_STATUSES = ["ORDERED", "ISSUED", "RETURNED", "LOST", "DAMAGED", "ARCHIVED", "OVERDUE"];

export function renderCheckboxGroup(name, values, title) {
  const body = values
    .map(
      (value) => `
      <label class="form-control">
        <input type="checkbox" class="admin-filter-control" name="${name}" value="${value}">
        <span>${value}</span>
      </label>`
    )
    .join("");
  return `
    <fieldset class="admin-filter-fieldset">
      <legend>${title}</legend>
      <div class="fieldset-content">
      ${body}
      </div>
    </fieldset>
  `;
}

export function renderLoanDateRange(name, title) {
  return `
    <fieldset class="admin-filter-fieldset">
      <legend>${title}</legend>
      <label>From: <input type="date" class="admin-filter-control" name="${name}From"></label>
      <label>To: <input type="date" class="admin-filter-control" name="${name}To"></label>
    </fieldset>
  `;
}
