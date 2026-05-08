import { renderBookRow, renderAuthorRow, renderBookItemRow, renderLoanRow, renderGenreRow } from '../components/adminTableRows.js';
import { renderBookForm, renderAuthorForm, renderBookItemForm, renderLoanForm, renderGenreForm } from '../components/adminForms.js';
import { BookApi } from '../api/bookApi.js';
import { AuthorApi } from '../api/authorApi.js';
import { BookItemApi } from '../api/bookItemApi.js';
import { LoanApi } from '../api/loanApi.js';
import { GenreApi } from '../api/genreApi.js';
import { sectionFilterConfigs } from "./adminFiltersConfig.js";

export const tableConfigs = {
  "books-section": {
    title: "Books management",
    headers: ["ID", "Title", "Author(s)", "ISBN", "Management"],
    action: "add-book",
    renderer: renderBookRow,
    apiCall: () => BookApi.getAll(),
    ...sectionFilterConfigs["books-section"],
  },
  "authors-section": {
    title: "Authors management",
    headers: ["ID", "Pen name", "Management"],
    action: "add-author",
    renderer: renderAuthorRow,
    apiCall: () => AuthorApi.getAll(),
    ...sectionFilterConfigs["authors-section"],
  },
  "book-items-section": {
    title: "Book items management",
    headers: ["ID", "Book ID", "Inventory code", "Status", "Management"],
    action: "add-book-item",
    renderer: renderBookItemRow,
    apiCall: () => BookItemApi.getAll(),
    ...sectionFilterConfigs["book-items-section"],
  },
  "loans-section": {
    title: "Loans management",
    headers: [
      "ID",
      "Book item ID",
      "Reader ID",
      "Librarian ID",
      "Loan date",
      "Due date",
      "Return date",
      "Loan type",
      "Status",
      "Management",
    ],
    action: "add-loan",
    renderer: renderLoanRow,
    apiCall: () => LoanApi.getAll(),
    ...sectionFilterConfigs["loans-section"],
  },
  "genres-section": {
    title: "Genres management",
    headers: ["ID", "Name", "Management"],
    action: "add-genre",
    renderer: renderGenreRow,
    apiCall: () => GenreApi.getAll(),
    ...sectionFilterConfigs["genres-section"],
  },
};

export const modalConfigs = {
  "add-book": {
    title: "Add new book",
    renderer: renderBookForm,
  },
  "add-author": {
    title: "Add new author",
    renderer: renderAuthorForm,
  },
  "add-book-item": {
    title: "Add new book item",
    renderer: renderBookItemForm,
  },
  "add-loan": {
    title: "Add new loan",
    renderer: renderLoanForm,
  },
  "add-genre": {
    title: "Add new genre",
    renderer: renderGenreForm,
  },
};

export const quickModalConfig = {
    "quick-add-author": {
      title: "Add new author",
      renderer: renderAuthorForm,
    },
    "quick-add-genre": {
      title: "Add new genre",
      renderer: renderGenreForm,
    },
    "quick-add-book-item": {
      title: "Add new book item",
      renderer: renderBookItemForm,
    },
  };