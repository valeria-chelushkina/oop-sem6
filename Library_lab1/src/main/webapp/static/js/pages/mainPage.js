import { BookApi } from '../api/bookApi.js';
import { FilterApi } from '../api/filterApi.js';
import { createBookCard } from '../components/bookCard.js';
import  { renderFilterGroups } from '../components/filterMenu.js';
import { UrlService } from '../services/urlService.js';
import { waitLoader } from '../utils/utils.js';
import { OrderService } from '../services/orderService.js';

document.addEventListener('DOMContentLoaded', async () => {
    const bookList = document.getElementById('book-list');
    const searchBar = document.getElementById('search-bar');
    const filterForm = document.getElementById('filter-form');
    const loader = document.getElementById('loader');

    async function init() {
        try {
            const authResponse = await fetch('/api/auth/status');
            const auth = await authResponse.json();
            const readerId = auth.authenticated ? auth.id : null;

            // load filters
            const filters = await FilterApi.getFilters();
            renderFilterGroups(filters);

            // synchronize with URL
            UrlService.applyUrlToForm(searchBar, filterForm);

            // load books
            const params = new URLSearchParams(window.location.search);
            const data = await BookApi.getAll(params.toString());
            const books = Array.isArray(data) ? data : [];
            
            // render result
            bookList.innerHTML = '';
            if (books.length === 0) {
                bookList.innerHTML = '<p class="no-books-found">No books found</p>';
                waitLoader(loader);
                return;
            } else {
                for (const book of books) {
                    const card = createBookCard(book);
                    bookList.appendChild(card);
                    await OrderService.updateButtonStates(card, book.id, readerId);
                }
            }

            // bind order events
            bookList.addEventListener('click', async (e) => {
                const orderBtn = e.target.closest('.order');
                const rrBtn = e.target.closest('.reading-room-order');
                if (!orderBtn && !rrBtn) return;

                const card = e.target.closest('.book-card');
                const bookId = card.querySelector('a').href.split('/').pop();
                
                if (orderBtn) {
                    await OrderService.handleOrder(bookId, 'SUBSCRIPTION');
                } else if (rrBtn) {
                    await OrderService.handleOrder(bookId, 'READING_ROOM');
                }
            });

            waitLoader(loader);
        } catch (error) {
            console.error("Initialization failed:", error);
            waitLoader(loader);
        }
    }

    init();
});