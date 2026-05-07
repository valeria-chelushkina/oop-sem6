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

// open-close buttons for the modal window
const closeBtn = document.querySelectorAll(".close-btn");
const openBtn = document.getElementById("open-add-form");
const overlay = document.getElementById("modal-overlay");

const closeModal = () => {
  overlay.classList.remove("active");
};

// close with a background click (? maybe will remove it)
overlay.addEventListener("click", (e) => {
  if (e.target === overlay) closeModal();
});

const modalConfigs = {
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

function openModal(action) {
  const config = modalConfigs[action];
  if (!config) return;
  document.getElementById("modal-title").textContent = config.title;
  document.getElementById("admin-form").innerHTML = config.renderer();

  overlay.classList.add("active");
  initializeSelects();
}

openBtn.addEventListener("click", (e) => {
  openModal(e.target.dataset.action);
});

closeBtn.forEach((btn) => btn.addEventListener("click", closeModal));

function renderBookForm() {
  return `
  <div class="form-group">
                  <label> Title:</label>
                  <input type="text" id="book-title" required />
                </div>

                <div class="form-group">
                  <label>Author(s)</label>
                  <div class="input-with-action">
                    <select name="authorIds" id="author-select" multiple>
                    </select>
                    <button
                      type="button"
                      class="btn-quick-add"
                      onclick="openAuthorModal()"
                      title="Create new author"
                    >
                      +
                    </button>
                  </div>
                </div>

                <div class="form-group">
                  <label>Genre(s)/Tag(s)</label>
                  <div class="input-with-action">
                    <select name="genreIds" id="genre-select" multiple></select>
                    <button
                      type="button"
                      class="btn-quick-add"
                      onclick="openAuthorModal()"
                      title="Create new genre"
                    >
                      +
                    </button>
                  </div>
                </div>

                <div class="form-group">
                  <label> ISBN:</label>
                  <input type="text" id="book-isbn" />
                </div>

                <div class="form-group">
                  <label> Publisher:</label>
                  <input type="text" id="book-publisher" />
                </div>

                <div class="form-group">
                  <label> Publication year:</label>
                  <input type="number" id="book-publication-year" />
                </div>

                <div class="form-group">
                  <label> Cover:</label>
                  <input
                    type="file"
                    id="book-cover"
                    accept="image/png, image/jpeg"
                  />
                </div>

                <div class="form-group">
                  <label> Language:</label>
                  <input type="text" id="book-language" />
                </div>

                <div class="form-group">
                  <label> Number of pages:</label>
                  <input type="number" id="book-page-number" />
                </div>

                <div class="form-group">
                  <label> Description:</label>
                  <textarea name="description" id="book-description"></textarea>
                </div>

                <div class="form-group">
                  <label>Book items:</label>
                  <div id="items-container"></div>
                  <button
                    type="button"
                    id="add-copy-btn"
                    onclick="addItemRow()"
                  >
                    + Add book copy
                  </button>
                </div>
  `;
}

function renderAuthorForm() {
  return `
  <div class="form-group">
    <label> Pen name:</label>
    <input type="text" id="author-pen-name" required />
  </div>
  <div class="form-group">
    <label> Biography:</label>
    <textarea
    name="biography"
    id="author-biography"
    ></textarea>
  </div>
  `;
}

function renderBookItemForm() {
  return `
                  <div class="form-group">
                    <label> Book ID:</label>
                    <input type="text" id="bookItem-book-id" required />
                  </div>

                  <div class="form-group">
                    <label> Inventory code:</label>
                    <input type="text" id="bookItem-inventory-code" />
                  </div>

                  <div class="form-group">
                    <label> Status:</label>
                    <select name="bookItem-status" id="bookItem-status">
                      <option></option>
                      <option value="available">AVAILABLE</option>
                      <option value="ordered">ORDERED</option>
                      <option value="issued">ISSUED</option>
                      <option value="lost">LOST</option>
                      <option value="damaged">DAMAGED</option>
                      <option value="archived">ARCHIVED</option>
                      <option value="reading-room-only">
                        READING ROOM ONLY
                      </option>
                    </select>
                  </div>
  `;
}

function renderLoanForm() {
  return `
  <div class="form-group">
          <label>Book item ID</label>
          <input type="number" required min="0" />
        </div>
        <div class="form-group">
          <label>Reader ID</label>
          <input type="number" required min="0" />
        </div>
        <div class="form-group">
          <label>Librarian ID</label>
          <input type="number" min="0" />
        </div>
        <div class="form-group">
                          <label>Status</label>
                          <select name="loan-status" id="loan-status">
                          <option></option>
                                      <option value="ordered-loan">ORDERED</option>
                                      <option value="issued-loan">ISSUED</option>
                                      <option value="lost-loan">LOST</option>
                                      <option value="damaged-loan">DAMAGED</option>
                                      <option value="archived-loan">ARCHIVED</option>
                                      <option value="returned-loan">
                                        RETURNED
                                      </option>
                          </select>
                        </div>
        <div class="form-group">
          <label>Loan type</label>
          <select name="loan-type" id="loan-type">
          <option></option>
                      <option value="subscription">SUBSCRIPTION</option>
                      <option value="reading-room">READING_ROOM</option>
          </select>
        </div>
        <div class="form-group">
          <label>Loan date</label>
          <input type="datetime-local" />
        </div>
        <div class="form-group">
          <label>Due date</label>
          <input type="date">
        </div>
        <div class="form-group">
          <label>Return date</label>
          <input type="datetime-local">
        </div>
  `;
}

function renderGenreForm(){
  return  `
  <div class="form-group">
    <label> Name:</label>
    <input type="text" id="genre-name" required />
  </div>
  `
}

document.addEventListener("DOMContentLoaded", () => {
  switchSection("books-section");
});


function initializeSelects() {
  setupSelect2("#author-select", {
    placeholder: "Choose authors",
    allowClear: true,
    width: "100%",
    dropdownParent: $(".modal-overlay"),
  });

  setupSelect2("#genre-select", {
    placeholder: "Choose genres",
    allowClear: true,
    width: "100%",
    dropdownParent: $(".modal-overlay"),
  });

  setupSelect2("#bookItem-status", {
    placeholder: "Select a status",
    allowClear: true,
    width: "100%",
    dropdownParent: $(".modal-overlay"),
  });

  setupSelect2("#loan-type", {
    placeholder: "Select a type",
    allowClear: true,
    width: "100%",
    dropdownParent: $(".modal-overlay"),
  });

  setupSelect2("#loan-status", {
    placeholder: "Select a status",
    allowClear: true,
    width: "100%",
    dropdownParent: $(".modal-overlay"),
  });
}

function setupSelect2(selector, options) {
  const element = $(selector);

  if (!element.length) return;

  if (element.hasClass("select2-hidden-accessible")) {
    element.select2("destroy");
  }

  element.select2(options);
}

document.addEventListener("DOMContentLoaded", () => {
  switchSection("books-section");
});