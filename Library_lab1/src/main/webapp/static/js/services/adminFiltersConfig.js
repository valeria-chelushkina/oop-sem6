import { includesText, inDateRange, parseDateValue } from "../utils/adminFilterUtils.js";
import {
  BOOK_ITEM_STATUSES,
  LOAN_STATUSES,
  LOAN_TYPES,
  renderCheckboxGroup,
  renderLoanDateRange,
} from "../components/adminFilterTemplates.js";

export const sectionFilterConfigs = {
  "books-section": {
    filterTemplate: () => "",
    filterLogic: (item, query) => {
      const q = query.toLowerCase();
      return (
        includesText(item.id, q) ||
        includesText(item.title, q) ||
        includesText(item.author, q) ||
        includesText(item.isbn, q)
      );
    },
  },
  "authors-section": {
    filterTemplate: () => "",
    filterLogic: (item, query) => {
      const q = query.toLowerCase();
      return includesText(item.id, q) || includesText(item.penName, q);
    },
  },
  "genres-section": {
    filterTemplate: () => "",
    filterLogic: (item, query) => {
      const q = query.toLowerCase();
      return includesText(item.id, q) || includesText(item.name, q);
    },
  },
  "book-items-section": {
    filterTemplate: () => renderCheckboxGroup("status", BOOK_ITEM_STATUSES, "Statuses"),
    filterLogic: (item, query, filters) => {
      const q = query.toLowerCase();
      const statuses = filters.status || [];
      const matchesQuery = includesText(item.id, q) || includesText(item.bookId, q);
      const matchesStatus = statuses.length === 0 || statuses.includes(String(item.status || "").toUpperCase());
      return matchesQuery && matchesStatus;
    },
  },
  "loans-section": {
    filterTemplate: () => `
      ${renderCheckboxGroup("loanType", LOAN_TYPES, "Loan type")}
      ${renderCheckboxGroup("loanStatus", LOAN_STATUSES, "Status")}
      ${renderLoanDateRange("loanDate", "Loan date")}
      ${renderLoanDateRange("dueDate", "Due date")}
      ${renderLoanDateRange("returnDate", "Return date")}
    `,
    filterLogic: (item, query, filters) => {
      const q = query.toLowerCase();
      const types = filters.loanType || [];
      const statuses = filters.loanStatus || [];
      const matchesQuery =
        includesText(item.id, q) || includesText(item.readerId, q) || includesText(item.bookItemId, q);
      const matchesType = types.length === 0 || types.includes(String(item.loanType || "").toUpperCase());
      const matchesStatus = statuses.length === 0 || statuses.includes(String(item.status || "").toUpperCase());
      const loanDateFrom = parseDateValue(filters.loanDateFrom);
      const loanDateTo = parseDateValue(filters.loanDateTo);
      const dueDateFrom = parseDateValue(filters.dueDateFrom);
      const dueDateTo = parseDateValue(filters.dueDateTo);
      const returnDateFrom = parseDateValue(filters.returnDateFrom);
      const returnDateTo = parseDateValue(filters.returnDateTo);
      const matchesLoanDate = inDateRange(item.loanDate, loanDateFrom, loanDateTo);
      const matchesDueDate = inDateRange(item.dueDate, dueDateFrom, dueDateTo);
      const matchesReturnDate = inDateRange(item.returnDate, returnDateFrom, returnDateTo);
      return (
        matchesQuery &&
        matchesType &&
        matchesStatus &&
        matchesLoanDate &&
        matchesDueDate &&
        matchesReturnDate
      );
    },
  },
};
