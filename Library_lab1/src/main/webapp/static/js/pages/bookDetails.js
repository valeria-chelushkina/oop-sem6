import  { getBookIdFromUrl } from '../utils/utils.js';
import { BookApi } from '../api/bookApi.js';
import { createBookDetailsCard } from '../components/bookDetailsCard.js';

document.addEventListener('DOMContentLoaded', async => {
    const bookContainer = document.getElementById('container')

	async function init() {
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
        }

	}

	init();
})