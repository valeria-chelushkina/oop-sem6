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

export function showRating(container, averageRating) {
    const starPercentage = (averageRating / 5) * 100;
    const starPercentageRounded = `${(Math.round(starPercentage / 10) * 10)}%`;
    const starsInner = container.querySelector('.stars-inner');
    const ratingNum = container.querySelector('.rating-number');
    if (starsInner) starsInner.style.width = starPercentageRounded;
    if (ratingNum) ratingNum.innerHTML = averageRating || 0;
}

export async function loadBookItems(container, id) {
    try{
        const url = '/api/book-items?availableCount=true&bookId='+id;
        const response = await fetch(url);
        if(!response.ok) {
            console.warn("Couldn't load /api/book-items:", response.status);
            return;
        }
        const bookItem = await response.json();
        const availableCopies = bookItem.availableCount ?? 0;

        const copiesElement = container.querySelector(".available-copies")
        if(copiesElement) {
            copiesElement.innerHTML = `Available copies: ${availableCopies}`;
        }
        if(availableCopies === 0){
            const order = document.getElementById('order');
            const readingRoom = document.getElementById('reading-room-order');
            order.disabled = true;
            readingRoom.disabled = true;
            order.title = "No available copies.";
            readingRoom.title = "No available copies.";
        }
    }
    catch(e){
        console.warn('Error loadBookItems:', e);
    }
}