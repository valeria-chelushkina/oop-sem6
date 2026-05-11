import  { getBookIdFromUrl } from '../utils/utils.js';
import { BookApi } from '../api/bookApi.js';
import { createBookDetailsCard } from '../components/bookDetailsCard.js';
import { OrderService } from '../services/orderService.js';

document.addEventListener('DOMContentLoaded', async () => {
    const bookContainer = document.getElementById('container')

	async function init() {
        const authResponse = await fetch('/api/auth/status');
        const auth = await authResponse.json();
        const readerId = auth.authenticated ? auth.id : null;

		bookContainer.innerHTML = '';
		const bookId = getBookIdFromUrl();
		if (!bookId) {
            createBookDetailsCard(null, bookContainer);
            return;
        }

		const bookData = await BookApi.getById(bookId);
		if (!bookData || bookData.error || !bookData.title) {
            createBookDetailsCard(null, bookContainer);
        } else {
            createBookDetailsCard(bookData, bookContainer);
            const cardElement = bookContainer.querySelector('.main-block');
            await OrderService.updateButtonStates(cardElement, bookId, readerId);

            // bind events
            cardElement.querySelector('.order')?.addEventListener('click', () => OrderService.handleOrder(bookId, 'SUBSCRIPTION'));
            cardElement.querySelector('.reading-room-order')?.addEventListener('click', () => OrderService.handleOrder(bookId, 'READING_ROOM'));
        }

	}

	init();
})