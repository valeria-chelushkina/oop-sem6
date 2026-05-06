// only the starting point

const navItems = document.querySelectorAll(".nav-item");
const sections = document.querySelectorAll(".card");

// sidebar logic
navItems.forEach((item) => {
  item.addEventListener("click", () => {
    navItems.forEach((nav) => nav.classList.remove("active"));
    item.classList.add("active");
    sections.forEach((sec) => sec.classList.add("hidden"));
    const targetId = item.getAttribute("data-target");
    document.getElementById("tab-title").innerHTML =
      item.getAttribute("data-name") + " management";
    document.getElementById(targetId).classList.remove("hidden");
    console.log(`Switched to ${targetId}`);
    document.querySelectorAll(".table-body").forEach(table => table.innerHTML='');
    buildSectionRow(1, targetId); // temporary - will be changed
  });
});

// build one section row in a corresponding table
// will fetch from API later
function buildSectionRow(data, targetId) {
  const section = document.getElementById(targetId);
  const tableBody = section.querySelector(".table-body");
  switch (targetId) {
    case "books-section":
      const authorLine =
        Array.isArray(data.authors) && data.authors.length
          ? data.authors
              .map((a) => (a && a.penName ? a.penName : ""))
              .filter(Boolean)
              .join(", ")
          : "";
      tableBody.innerHTML += `
      <tr>
        <td scope="row">${data.id}</td>
              <td>${data.title}</td>
              <td>${authorLine}</td>
              <td>${data.ISBN}</td>
              <td>
                <div class="row-entry-buttons">
                  <button class="view-update-btn">View/Update</button>
                  <button class="delete-btn">Delete</button>
                </div>
              </td>
      </tr>
        `;
      break;
    case "authors-section":
      tableBody.innerHTML += `
        <tr>
            <td scope="row">${data.id}</td>
            <td>${data.penName}</td>
            <td>
                <div class="row-entry-buttons">
                    <button class="view-update-btn">View/Update</button>
                    <button class="delete-btn">Delete</button>
                </div>
            </td>
        </tr>
        `;
      break;
    case "book-items-section":
      tableBody.innerHTML += `
      <tr>
        <td scope="row">${data.id}</td>
              <td>${data.bookId}</td>
              <td>${data.inventoryCode}</td>
              <td>${data.status}</td>
              <td>
                <div class="row-entry-buttons">
                  <button class="view-update-btn">View/Update</button>
                  <button class="delete-btn">Delete</button>
                </div>
              </td>
      </tr>
        `;
      break;
    case "loans-section":
      tableBody.innerHTML += `
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
              <td>
                <div class="row-entry-buttons">
                  <button class="view-update-btn">View/Update</button>
                  <button class="delete-btn">Delete</button>
                </div>
              </td>
      </tr>
        `;
      break;
    case "genres-section":
      tableBody.innerHTML += `
      <tr>
        <td scope="row">${data.id}</td>
              <td>${data.name}</td>
              <td>
                <div class="row-entry-buttons">
                  <button class="view-update-btn">View/Update</button>
                  <button class="delete-btn">Delete</button>
                </div>
              </td>
      </tr>
        `;
      break;
    default:
      alert("Something went wrong: no ID found!");
  }
}

document.addEventListener("DOMContentLoaded", () => {
  buildSectionRow(1, "books-section"); // temporary - will be changed
});