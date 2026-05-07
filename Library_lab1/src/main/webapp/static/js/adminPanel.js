// only the starting point

const navItems = document.querySelectorAll(".nav-item");
let currentRenderer = null;

// config table for easier use
const tableConfigs = {
  "books-section": {
    title: "Books management",

    headers: ["ID", "Title", "Author(s)", "ISBN", "Management"],

    action: "add-book",

    renderer: renderBookRow,
  },

  "authors-section": {
    title: "Authors management",

    headers: ["ID", "Pen name", "Management"],

    action: "add-author",

    renderer: renderAuthorRow,
  },

  "book-items-section": {
    title: "Book items management",

    headers: ["ID", "Book ID", "Inventory code", "Status", "Management"],

    action: "add-book-item",

    renderer: renderBookItemRow,
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
  },

  "genres-section": {
    title: "Genres management",

    headers: ["ID", "Name", "Management"],

    action: "add-genre",

    renderer: renderGenreRow,
  },
};

function renderTable(config) {
  const tableHead = document.querySelector(".table-head");

  tableHead.innerHTML = config.headers
    .map((header) => `<th>${header}</th>`)
    .join("");

  document.getElementById("tab-title").textContent = config.title;

  document.getElementById("open-add-form").dataset.action = config.action;
}

function switchSection(targetId) {
  navItems.forEach((nav) =>
    nav.classList.remove("active")
  );

  document
    .querySelector(`[data-target="${targetId}"]`)
    .classList.add("active");

  const config = tableConfigs[targetId];

  currentRenderer = config.renderer;

  renderTable(config);

  document.querySelector(".table-body").innerHTML = "";

  loadData(targetId);
}

navItems.forEach((item) => {
  item.addEventListener("click", () => {
    switchSection(item.dataset.target);
  });
});

// will change logic to fetch from API later
function loadData(targetId) {
  const fakeData = {
    "books-section": [
      {
        id: 1,
        title: "The Hobbit",
        ISBN: "123456",
        authors: [{ penName: "Tolkien" }],
      },
    ],

    "authors-section": [
      {
        id: 1,
        penName: "Tolkien",
      },
    ],
  };

  const data = fakeData[targetId] || [];

  data.forEach(buildRow);
}

function buildRow(data) {
  const tableBody = document.querySelector(".table-body");

  tableBody.insertAdjacentHTML("beforeend", currentRenderer(data));
}

function renderBookRow(data) {
  const authorLine =
    Array.isArray(data.authors) && data.authors.length
      ? data.authors
          .map((a) => (a && a.penName ? a.penName : ""))
          .filter(Boolean)
          .join(", ")
      : "";
  return `
      <tr>
        <td scope="row">${data.id}</td>
              <td>${data.title}</td>
              <td>${authorLine}</td>
              <td>${data.ISBN}</td>
              <td>${renderActionButtons()}</td>
      </tr>
        `;
}

function renderAuthorRow(data) {
  return `
        <tr>
            <td scope="row">${data.id}</td>
            <td>${data.penName}</td>
            <td>${renderActionButtons()}</td>
        </tr>
        `;
}

function renderBookItemRow(data) {
  return `
      <tr>
        <td scope="row">${data.id}</td>
              <td>${data.bookId}</td>
              <td>${data.inventoryCode}</td>
              <td>${data.status}</td>
              <td>${renderActionButtons()}</td>
      </tr>
        `;
}

function renderLoanRow(data) {
  return `
      <tr>
        <td scope="row">${data.id}</td>
              <td>${data.bookId}</td>
              <td>${data.readerId}</td>
              <td>${data.librarianId}</td>
              <td>${data.loanDate}</td>
              <td>${data.dueDate}</td>
              <td>${data.returnDate}</td>
              <td>${data.loanType}</td>
              <td>${data.status}</td>
              <td>${renderActionButtons()}</td>
      </tr>
        `;
}

function renderGenreRow(data) {
  return `
      <tr>
        <td scope="row">${data.id}</td>
              <td>${data.name}</td>
              <td>${renderActionButtons()}</td>
      </tr>
        `;
}

function renderActionButtons() {
  return `
    <div class="row-entry-buttons">
      <button class="view-update-btn">View/Update</button>
      <button class="delete-btn">Delete</button>
    </div>
  `;
}

document.addEventListener("DOMContentLoaded", () => {
  switchSection("books-section");
});