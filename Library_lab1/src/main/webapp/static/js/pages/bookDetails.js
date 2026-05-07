import  { getId } from '../utils/utils.js';
import { BookApi } from '../api/bookApi.js';
import { createBookDetailsCard } from '../components/bookDetailsCard.js';

document.addEventListener('DOMContentLoaded', async => {
	const searchBar = document.getElementById('search-bar');
    const searchForm = document.getElementById('search-form');
    const bookContainer = document.getElementById('container')

	async function init() {
		bookContainer.innerHTML = '';
		const id = getId();
		if (!id) {
            createBookDetailsCard(null, bookContainer);
            return;
        }

		const bookData = await BookApi.getById(id);
		if (!bookData || bookData.error || !bookData.title) {
            createBookDetailsCard(null, bookContainer);
        } else {
            createBookDetailsCard(bookData, bookContainer);
        }

	}

	// event listener
	searchForm.addEventListener('submit', (e) => {
	e.preventDefault();
        const params = UrlService.getParamsFromForm(searchBar);
        UrlService.navigateWithParams(params);
    });

	init();
})