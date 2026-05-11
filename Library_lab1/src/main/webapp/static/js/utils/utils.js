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