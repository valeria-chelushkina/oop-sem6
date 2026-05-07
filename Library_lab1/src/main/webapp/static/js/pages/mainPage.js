import { BookApi } from '../api/bookApi.js';
import { FilterApi } from '../api/filterApi.js';
import { createBookCard } from '../components/bookCard.js';
import  { renderFilterGroups } from '../components/filterMenu.js';
import { UrlService } from '../services/urlService.js';
import { waitLoader } from '../utils/utils.js';

document.addEventListener('DOMContentLoaded', async () => {
    const bookList = document.getElementById('book-list');
    const searchBar = document.getElementById('search-bar');
    const filterForm = document.getElementById('filter-form');
    const searchForm = document.getElementById('search-form');
    const loader = document.getElementById('loader');

    async function init() {
        try {
            // load filters
            const filters = await FilterApi.getFilters();
            renderFilterGroups(filters);

            // synchronize with URL
            UrlService.applyUrlToForm(searchBar, filterForm);

            // load books
            const params = new URLSearchParams(window.location.search);
            const data = await BookApi.getAll(params.toString());
            const books = Array.isArray(data) ? data : [];
            if (!Array.isArray(data) && data && data.error) {
                console.warn('API:', data.error);
            }

            // render result
            bookList.innerHTML = '';
            if (books.length === 0) {
                bookList.innerHTML = '<p class="no-books-found">No books found</p>';
                return;
            } else {
                books.forEach(book => bookList.appendChild(createBookCard(book)));
            }

            waitLoader(loader);
        } catch (error) {
            console.error("Initialization failed:", error);
        }
    }

    // event listeners
    [searchForm, filterForm].forEach(form => {
        form?.addEventListener('submit', (e) => {
            e.preventDefault();
            const params = UrlService.getParamsFromForm(searchBar, filterForm);
            UrlService.navigateWithParams(params);
        });
    });

    init();
});