import { BookItemApi } from '../api/bookItemApi.js';

export function genresForBook(container, bookGenres){
    const genreContainer = container.querySelector(".tags");
    if(!genreContainer) {
        return;
    }

    if (!Array.isArray(bookGenres)) {
        console.warn("bookGenres is not an array:", bookGenres);
        const span = document.createElement('span');
        span.className = 'tag';
        span.textContent = 'None';
        genreContainer.append(span);
        return;
    }

    const genres = bookGenres.map((g) => g && g.name ? g.name : '').filter(Boolean);
    genres.forEach((genre) => {
        const span = document.createElement('span');
        span.className = 'tag';
        span.textContent = genre;
        genreContainer.append(span);
    })
}

export function authorsForBook(container, bookAuthors){

               const authorContainer = container.querySelector(".authors");
               if(!authorContainer) {
                   return;
               }

               if (!Array.isArray(bookAuthors)) {
                   console.warn("bookAuthors is not an array:", bookAuthors);
                   const span = document.createElement('span');
                   span.className = 'author-name';
                   span.textContent = 'Unknown';
                   authorContainer.append(span);
                   return;
               }

               const authors = bookAuthors.map((a) => a && a.penName ? a.penName : '').filter(Boolean);
               authors.forEach((author) => {
                   const span = document.createElement('span');
                   span.className = 'tag';
                   span.textContent = author;
                   authorContainer.append(span);
               })
           }

export function showRating(container, averageRating) {
    const starPercentage = (averageRating / 5) * 100;
    const starPercentageRounded = `${(Math.round(starPercentage / 10) * 10)}%`;
    const starsInner = container.querySelector('.stars-inner');
    const ratingNum = container.querySelector('.rating-number');
    if (starsInner) starsInner.style.width = starPercentageRounded;
    if (ratingNum) ratingNum.innerHTML = averageRating || 0;
}

export async function loadBookItems(container, bookId) {
    try {
        const bookItems = await BookItemApi.getByBookId(bookId);
        if (!bookItems) return;

        const availableCopies = bookItems.filter(item => (item.status === 'AVAILABLE' || item.status === 'READING_ROOM_ONLY')).length;
        const copiesElement = container.querySelector(".available-copies");

        if (copiesElement) {
            copiesElement.innerHTML = `Available copies: ${availableCopies}`;
        }

        if (availableCopies === 0) {
            const order = container.querySelector('.order');
            const readingRoom = container.querySelector('.reading-room-order');

            if (order) order.disabled = true;
            if (readingRoom) readingRoom.disabled = true;
        }
    } catch (e) {
        console.error("Load book items error:", e);
    }
}

export async function isOnlyReadingRoom(container, bookId) {
	try{
	const bookItem = await BookItemApi.getByBookId(bookId);
            if(!bookItem || !Array.isArray(bookItem)){
                console.warn("bookItem was not found or is not an array:", bookItem);
                return;
            }
            let canOrder = 0;
            let isReadingPresent = 0;
            bookItem.forEach((item) => {
                if(item.status === 'READING_ROOM_ONLY'){
                    isReadingPresent+=1;
                }
                if(item.status === 'AVAILABLE' || item.status ===  'ORDERED' || item.status ===  'ISSUED'){
                    canOrder+=1;
                }
            });
            const orderBtn = container.querySelector('.order');
            if(orderBtn && isReadingPresent > 0 && canOrder === 0){
                orderBtn.disabled = true;
                orderBtn.title = "No available copies.";
            }
	} catch(e){
		 console.error("Load book items error:", e);
	}

}

export function waitLoader(loader){
	    if (loader) {
            loader.classList.add('hidden');
            window.setTimeout(() => {
                loader.style.display = 'none';
            }, 600);
        }
}

export function showNumberRated(container, ratingsCount){
        container.querySelector('.people-rated').innerHTML = `${ratingsCount} people rated`
    }

export function getBookIdFromUrl() {
    const pathSegments = window.location.pathname.split('/');
    return pathSegments[pathSegments.length - 1];
}

export const PaginationHelper = {
    getNextChunk(allData, currentCount, pageSize) {
        return allData.slice(currentCount, currentCount + pageSize);
    },

    hasMore(allData, newTotalCount) {
        return newTotalCount < allData.length;
    }
}