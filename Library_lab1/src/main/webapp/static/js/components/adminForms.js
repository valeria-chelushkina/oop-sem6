export function renderBookForm() {
  return `
  <input type="hidden" name="id">
  <div class="form-group">
                  <label> Title:</label>
                  <input type="text" name="title" id="book-title" required />
                </div>

                <div class="form-group">
                  <label>Author(s)</label>
                  <div class="input-with-action">
                    <select name="author-ids" id="author-select" multiple>
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
                    <select name="genre-ids" id="genre-select" multiple></select>
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
                  <input type="text" name="isbn" id="book-isbn" />
                </div>

                <div class="form-group">
                  <label> Publisher:</label>
                  <input type="text" name="publisher" id="book-publisher" />
                </div>

                <div class="form-group">
                  <label> Publication year:</label>
                  <input type="number" name="publication-year" id="book-publication-year" />
                </div>

                <div class="form-group">
                  <label style="display: inline-block;"> Cover:</label>
                  <img src="" class="cover-preview" style="width: 30px; display: inline-block; height: auto !important; margin-left: 10px;" />
                  <input
                    type="file"
                    name="coverURL"
                    id="book-cover"
                    accept="image/png, image/jpeg"
                    style="display: inline-block;"
                  />
                </div>

                <div class="form-group">
                  <label> Language:</label>
                  <input type="text" name="language" id="book-language" />
                </div>

                <div class="form-group">
                  <label> Number of pages:</label>
                  <input type="number" name="pages-count" id="book-page-number" />
                </div>

                <div class="form-group">
                  <label> Description:</label>
                  <textarea name="description" id="description"></textarea>
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
    <input type="text" name="pen-name" id="author-pen-name" required />
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
                    <input type="text" name="book-id" id="book-item-book-id" required />
                  </div>

                  <div class="form-group">
                    <label> Inventory code:</label>
                    <input type="text" name="inventory-code" id="book-item-inventory-code" />
                  </div>

                  <div class="form-group">
                    <label> Status:</label>
                    <select name="status" id="book-item-status">
                      <option></option>
                      <option value="AVAILABLE">AVAILABLE</option>
                      <option value="ORDERED">ORDERED</option>
                      <option value="ISSUED">ISSUED</option>
                      <option value="LOST">LOST</option>
                      <option value="DAMAGED">DAMAGED</option>
                      <option value="ARCHIVED">ARCHIVED</option>
                      <option value="READING_ROOM_ONLY">READING ROOM ONLY</option>
                    </select>
                  </div>
  `;
}

export function renderLoanForm() {
  return `
  <input type="hidden" name="id">
        <div class="form-group">
          <label>Book item ID</label>
          <input type="number" name="book-item-id" id="book-item-id" required min="0" />
        </div>
        <div class="form-group">
          <label>Reader ID</label>
          <input type="number" name="reader-id" id="reader-id" required min="0" />
        </div>
        <div class="form-group">
          <label>Librarian ID</label>
          <input type="number" name="librarian-id" id="librarian-id" min="0" />
        </div>
        <div class="form-group">
                          <label>Status</label>
                          <select name="status" id="loan-status">
                          <option></option>
                                      <option value="ORDERED">ORDERED</option>
                                      <option value="ISSUED">ISSUED</option>
                                      <option value="LOST">LOST</option>
                                      <option value="DAMAGED">DAMAGED</option>
                                      <option value="ARCHIVED">ARCHIVED</option>
                                      <option value="RETURNED">
                                        RETURNED
                                      </option>
                          </select>
                        </div>
        <div class="form-group">
          <label>Loan type</label>
          <select name="loan-type" id="loan-type">
          <option></option>
                      <option value="SUBSCRIPTION">SUBSCRIPTION</option>
                      <option value="READING_ROOM">READING ROOM</option>
          </select>
        </div>
        <div class="form-group">
          <label>Loan date</label>
          <input name="loan-date" id="loan-date" type="datetime-local" />
        </div>
        <div class="form-group">
          <label>Due date</label>
          <input name="due-date" id="due-date" type="date">
        </div>
        <div class="form-group">
          <label>Return date</label>
          <input name="return-date" id="return-date" type="datetime-local">
        </div>
  `;
}

export function renderGenreForm(){
  return  `
  <div class="form-group">
    <label> Name:</label>
    <input type="text" name="name" id="genre-name" required />
  </div>
  `
}