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
            const order = document.querySelector('order');
            const readingRoom = document.querySelector('reading-room-order');
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

export async function isOnlyReadingRoom(container, bookId) {
               try{
                   const url = '/api/book-items?bookId='+bookId;
                   const response = await fetch(url);
                   if(!response.ok){
                       console.warn("Couldn't load /api/book-items:", response.status);
                       return;
                   }
                   const bookItem = await response.json();
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
                   })
                   const orderBtn = container.querySelector('.order');
                   if(orderBtn && isReadingPresent > 0 && canOrder === 0){
                       orderBtn.disabled = true;
                       orderBtn.title = "No available copies.";
                   }
                   console.log('isReadingPresent' + isReadingPresent);
                   console.log('canOrder ' + canOrder);
               }
               catch(e){
                   console.warn('Error isOnlyReadingRoom:', e);
               }
           }