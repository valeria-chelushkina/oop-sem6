export function renderBookRow(data) {
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

export function renderAuthorRow(data) {
  return `
        <tr>
            <td scope="row">${data.id}</td>
            <td>${data.penName}</td>
            <td>${renderActionButtons()}</td>
        </tr>
        `;
}

export function renderBookItemRow(data) {
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

export function renderLoanRow(data) {
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

export function renderGenreRow(data) {
  return `
      <tr>
        <td scope="row">${data.id}</td>
              <td>${data.name}</td>
              <td>${renderActionButtons()}</td>
      </tr>
        `;
}

export function renderActionButtons() {
  return `
    <div class="row-entry-buttons">
      <button class="view-update-btn">View/Update</button>
      <button class="delete-btn">Delete</button>
    </div>
  `;
}