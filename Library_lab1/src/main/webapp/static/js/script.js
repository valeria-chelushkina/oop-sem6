import { genresForBook, showRating, loadBookItems} from './utils.js'

document.addEventListener('DOMContentLoaded', () => {
    const bookList = document.getElementById('book-list');
    const searchBar = document.getElementById('search-bar');
    const searchForm = document.getElementById('search-form');
    const filterForm = document.getElementById('filter-form');
    if (!bookList || !searchBar) {
        console.error('The expected #book-list or #search-bar elements are missing from the markup.');
        return;
    }

    /** Current directory page without query string. */
    function catalogPathname() {
        return window.location.pathname || '/';
    }

    /** Collect form state into the same query string as for /api/books. */
    function catalogParamsFromForms() {
        const params = new URLSearchParams();
        const q = searchBar.value.trim();
        if (q) {
            params.set('query', q);
        }
        if (filterForm) {
            filterForm.querySelectorAll('input[name="genre"]:checked').forEach((elem) => {
                params.append('genre', elem.value);
            });
            filterForm.querySelectorAll('input[name="language"]:checked').forEach((elem) => {
                params.append('language', elem.value);
            });
        }
        return params;
    }

    /** Full reboot with full URL. */
    function navigateWithCatalogState() {
        const params = catalogParamsFromForms();
        const qs = params.toString();
        const path = catalogPathname();
        window.location.assign(qs ? `${path}?${qs}` : path);
    }

    /** Restore fields from the address bar after reload. */
    function applyCatalogParamsFromUrl(params) {
        const q = params.get('query');
        searchBar.value = q != null ? q : '';

        if (!filterForm) {
            return;
        }
        filterForm.querySelectorAll('input[name="genre"]').forEach((elem) => {
            elem.checked = false;
        });
        filterForm.querySelectorAll('input[name="language"]').forEach((elem) => {
            elem.checked = false;
        });
        params.getAll('genre').forEach((want) => {
            filterForm.querySelectorAll('input[name="genre"]').forEach((elem) => {
                if (elem.value === want) {
                    elem.checked = true;
                }
            });
        });
        params.getAll('language').forEach((want) => {
            filterForm.querySelectorAll('input[name="language"]').forEach((elem) => {
                if (elem.value === want) {
                    elem.checked = true;
                }
            });
        });
    }

    function buildBooksApiUrl() {
        const params = catalogParamsFromForms();
        const qs = params.toString();
        return qs ? `/api/books?${qs}` : '/api/books';
    }

    async function loadBooks() {
        const loader = document.getElementById('loader');
        try {
            const url = buildBooksApiUrl();
            const response = await fetch(url);
            if (!response.ok) {
                console.error('API error:', response.status);
                renderBooks([]);
                if (loader) {
                    loader.classList.add('hidden');
                    window.setTimeout(() => {
                        loader.style.display = 'none';
                    }, 600);
                }
                return;
            }
            const data = await response.json();
            const books = Array.isArray(data) ? data : [];
            if (!Array.isArray(data) && data && data.error) {
                console.warn('API:', data.error);
            }
            renderBooks(books);
            if (loader) {
                loader.classList.add('hidden');
                window.setTimeout(() => {
                    loader.style.display = 'none';
                }, 600);
            }
        } catch (error) {
            console.error('Book download error:', error);
            if (loader) {
                loader.classList.add('hidden');
                loader.style.display = 'none';
            }
        }
    }

    function renderBooks(books) {
        bookList.innerHTML = '';

        if (books.length === 0) {
            bookList.innerHTML = '<p class="no-books-found">No books found</p>';
            return;
        }

        books.forEach((book) => {
            const authorLine =
                Array.isArray(book.authors) && book.authors.length
                    ? book.authors.map((a) => (a && a.penName ? a.penName : '')).filter(Boolean).join(', ')
                    : '';
            const coverSrc = book.coverURL || book.coverUrl || '';
            const li = document.createElement('li');
            const titleLink = "/book?id=" + book.id;
            li.innerHTML = `
                <div class="book-card">
                    <a href="${titleLink}">
                        <div class="book-cover">
                            <img src="${coverSrc || 'placeholder.jpg'}" alt="${book.title}" title="${book.title}">
                        </div>
                    </a>
                    <div class="book-info">
                        <a href="${titleLink}"><p class="title">${book.title}</p></a>
                        <p class="author">by <span>${authorLine || 'Unknown'}</span></p>
                        <div class="status-row">
                            <div class="rating">
                                <div class="stars-outer">
                                    <div class="stars-inner"></div>
                                </div>
                                <p class="rating-number"></p>
                            </div>
                            <p class="available-copies"></p>
                        </div>
                        <div class="additional-info">
                            <div class="tags">
                                <p>Tags:</p>
                            </div>
                            <div class="meta-data">
                                <p class="publisher">Publisher: ${book.publisher || 'None'}.</p>
                                <p class="language">Language: ${book.language || 'Unknown'}.</p>
                                <p class="release-year">Published in ${book.publicationYear || 'Unknown'}.</p>
                            </div>
                        </div>
                    </div>
                    <div class="add-order">
                        <button id="order">Order</button>
                        <button id="reading-room-order">Order to reading room</button>
                    </div>
                </div>
            `;

            showRating(li, book.averageRating);
            loadBookItems(li, book.id);
            genresForBook(li, book.genres);
            bookList.appendChild(li);
        });
    }

    async function loadFilters() {
        try {
            const response = await fetch('/api/filters');
            if (!response.ok) {
                console.warn("Couldn't load /api/filters:", response.status);
                return;
            }
            const filters = await response.json();
            const genreContainer = document.querySelector('#genre-filters');
            const languageContainer = document.querySelector('#language-filters');
            if (!genreContainer || !languageContainer) {
                return;
            }
            genreContainer.innerHTML = '<legend>Genres & tags</legend>';
            (filters.genres || []).forEach((genre) => {
                genreContainer.innerHTML += `
                <label class="form-control"><input type="checkbox" name="genre" value="${genre}" />
                                        <span>${genre}</span></label>`;
            });
            languageContainer.innerHTML = '<legend>Languages</legend>';
            (filters.languages || []).forEach((language) => {
                languageContainer.innerHTML += `
                        <label class="form-control"><input type="checkbox" name="language" value="${language}" />
                                                <span>${language}</span></label>`;
            });
        } catch (e) {
            console.warn('Error loadFilters:', e);
        }
    }

    if (searchForm) {
        searchForm.addEventListener('submit', (e) => {
            e.preventDefault();
            navigateWithCatalogState();
        });
    }

    if (filterForm) {
        filterForm.addEventListener('submit', (e) => {
            e.preventDefault();
            navigateWithCatalogState();
        });

    }

    async function init() {
        await loadFilters();
        applyCatalogParamsFromUrl(new URLSearchParams(window.location.search));
        await loadBooks();
    }

    void init();
});