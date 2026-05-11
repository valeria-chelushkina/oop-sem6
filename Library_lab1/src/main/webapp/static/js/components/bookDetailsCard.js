import { showRating, genresForBook, showNumberRated, authorsForBook } from '../utils/utils.js';

export function createBookDetailsCard(book, container){

	if(!book){
		container.innerHTML = '<p class="no-books-found">No book with such id found.</p>';
        document.title = 'Not found'
        return;
	}

	document.title = book.title;
    const mainBlock = document.createElement('div');
    mainBlock.className = 'main-block';
    mainBlock.innerHTML = `
        <div class="left-block">
            <div class="book-cover">
                <img src="${book.coverURL || 'static/img/placeholder.png'}" alt="${book.title}" title="${book.title}">
            </div>
        </div>
        <div class="middle-block">
            <h1 class="book-title">${book.title}</h1>
            <p class="book-code">ISBN: ${book.isbn || 'None'}</p>
            <p class="people-read">Read ${book.timesRead} times</p>
            <div class="rating-div">
                <div class="stars-outer">
                    <div class="stars-inner"></div>
                </div>
                <p class="rating-number"></p>
            </div>
            <p class="people-rated"></p>
            <div class="book-info">
                <div class="authors">
                    <p>Author:</p>
                </div>
                <div class="tags">
                    <p>Tags:</p>
                </div>
                <div class="row-line"></div>
                <div class="book-publisher"><p>Publisher: ${book.publisher || 'None'}.</p></div>
                <div class="book-publication-year"><p>Published in ${book.publicationYear || 'Unknown'}.</p></div>
                <div class="book-language"><p>Language: ${book.language || 'Unknown'}.</p></div>
                <div class="book-pages-count"><p>Number of pages: ${book.pagesCount || 'Unknown'}.</p></div>
            </div>
        </div>
        <div class="right-block">
            <button class="order">Order</button>
            <button class="reading-room-order">Order to reading room</button>
            <p class="available-copies"></p>
        </div>
    `;

    showRating(mainBlock, book.averageRating);
    genresForBook(mainBlock, book.genres);
    authorsForBook(mainBlock, book.authors);
    showNumberRated(mainBlock, book.ratingsCount);

    container.appendChild(mainBlock);
    container.appendChild(document.createElement('hr'));
    const descriptionBlock = document.createElement('div');
    descriptionBlock.className = 'description-block';
    descriptionBlock.innerHTML = `
        <h2>Description</h2>
        <div class="description-text">${book.description || 'No description.'}</div>
    `;
    container.appendChild(descriptionBlock);
}