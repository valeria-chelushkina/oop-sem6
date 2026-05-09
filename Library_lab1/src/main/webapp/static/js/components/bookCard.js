import { showRating, loadBookItems, genresForBook, isOnlyReadingRoom } from '../utils/utils.js';

export function createBookCard(book){
	const authorLine = Array.isArray(book.authors)
            ? book.authors.map(a => a?.penName || '').filter(Boolean).join(', ')
            : 'Unknown';

        const titleLink = `/book/${book.id}`;
        const li = document.createElement('li');
        const div = document.createElement('div');
        div.className = 'book-card';
        div.innerHTML = `
                <a href="${titleLink}">
                    <div class="book-cover">
                        <img src="${book.coverURL || 'static/img/placeholder.png'}" alt="${book.title}" title="${book.title}">
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
                    <button class="order">Order</button>
                    <button class="reading-room-order">Order to reading room</button>
                </div>
        `;
        li.appendChild(div);

        showRating(li, book.averageRating);
        loadBookItems(li, book.id);
        genresForBook(li, book.genres);
        isOnlyReadingRoom(li, book.id);
        return li;
}