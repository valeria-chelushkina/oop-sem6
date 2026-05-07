export function renderBookForm() {
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
                      data-action="quick-add-author"
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
                      data-action="quick-add-genre"
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
                    data-action="quick-add-book-item"
                    class="btn-quick-add"
                  >
                    + Add book copy
                  </button>
                </div>
  `;
}

export function renderAuthorForm() {
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

export function renderBookItemForm() {
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

export function renderLoanForm() {
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

export function renderGenreForm(){
  return  `
  <div class="form-group">
    <label> Name:</label>
    <input type="text" id="genre-name" required />
  </div>
  `
}