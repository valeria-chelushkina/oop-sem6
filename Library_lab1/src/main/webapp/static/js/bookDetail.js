document.addEventListener('DOMContentLoaded', () => {

    const searchBar = document.getElementById('search-bar');
    const searchForm = document.getElementById('search-form');
    const bookContainer = document.getElementById('container')
    const startingPath = 'http://localhost:8081';
    if (!searchBar) {
            console.error('The expected #search-bar element is missing from the markup.');
            return;
    }

    function getId(){
        const id = new URLSearchParams(window.location.search);
        return id.get('id');
    }

    async function loadBook() {
        try{
            const id = getId();
            if (!id) {
                renderBook(null);
                return;
            }
            //const url = `${startingPath}/api/books?id=${id}`;
            const url = '/api/books?id='+id;
            const response = await fetch(url);
            if(!response.ok){
                console.error('API error:', response.status);
                renderBook(null);
                return;
            }
            const bookData = await response.json();
            if (Array.isArray(bookData)) {
                bookData = bookData.length > 0 ? bookData[0] : null;
            }
            console.log(bookData);
            if (!bookData || bookData.error || !bookData.title) {
                renderBook(null);
            } else {
                renderBook(bookData);
            }
        }
        catch(error){
            console.error('Book download error:', error);
            renderBook(null);
        }
    }

    function renderBook(book){
        bookContainer.innerHTML = '';
        if (!book) {
            bookContainer.innerHTML = '<p class="no-books-found">No book with such id found.</p>';
            document.title = 'Not found'
            return;
        }

        document.title = book.title;
        const coverSrc = book.coverURL || book.coverUrl || '';
        const mainBlock = document.createElement('div');
        mainBlock.className = 'main-block';
        mainBlock.innerHTML = `
            <div class="left-block">
                <div class="book-cover">
                    <img src="${coverSrc || 'placeholder.jpg'}" alt="${book.title}" title="${book.title}">
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
                <button id="order">Order</button>
                <button id="reading-room-order">Order to reading room</button>
                <p class="available-copies"></p>
            </div>
        `

        showRating(mainBlock, book.averageRating);
        loadBookItems(mainBlock, book.id);
        genresForBook(mainBlock, book.genres);
        authorsForBook(mainBlock, book.authors);
        isOnlyReadingRoom(mainBlock, book.id);
        showNumberRated(mainBlock, book.ratingsCount);

        container.appendChild(mainBlock);
        container.appendChild(document.createElement('hr'));
        const descriptionBlock = document.createElement('div');
        descriptionBlock.className = 'description-block';
        descriptionBlock.innerHTML = `
            <h2>Description</h2>
            <div class="description-text">${book.description || 'No description.'}</div>
        `
        container.appendChild(descriptionBlock);
    }


    // repeated func
    function genresForBook(container, bookGenres){
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

    // repeated func
    function showRating(container, averageRating) {
            const starPercentage = (averageRating / 5) * 100;
            const starPercentageRounded = `${(Math.round(starPercentage / 10) * 10)}%`;
            const starsInner = container.querySelector('.stars-inner');
            const ratingNum = container.querySelector('.rating-number');
            if (starsInner) starsInner.style.width = starPercentageRounded;
            if (ratingNum) ratingNum.innerHTML = averageRating || 0;
    }

    function showNumberRated(container, ratingsCount){
        container.querySelector('.people-rated').innerHTML = `${ratingsCount} people rated`
    }


    // repeated func
    async function loadBookItems(container, id) {
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
                    document.getElementById('order').disabled = true;
                    document.getElementById('reading-room-order').disabled = true;
                    document.getElementById('order').title = "No available copies.";
                    document.getElementById('reading-room-order').title = "No available copies.";
                }
            }
            catch(e){
                console.warn('Error loadBookItems:', e);
            }
        }

    function authorsForBook(container, bookAuthors){

        const authorContainer = container.querySelector(".authors");
        if(!authorContainer) {
            return;
        }

        if (!Array.isArray(bookAuthors)) {
            console.warn("bookAuthors is not an array:", bookAuthors);
            const span = document.createElement('span');
            span.className = 'author';
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

    async function isOnlyReadingRoom(container, bookId) {
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
            if(isReadingPresent > 0 && canOrder === 0){
                document.getElementById('order').disabled = true;
                document.getElementById('order').title = "No available copies.";
            }
            console.log('isReadingPresent' + isReadingPresent);
            console.log('canOrder ' + canOrder);
        }
        catch(e){
            console.warn('Error isOnlyReadingRoom:', e);
        }
    }


    if(searchForm){
    searchForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const query = searchBar.value.trim();
        const qs = 'query=' + query;
        const path = '/';
        window.location.assign(qs ? `${path}?${qs}` : path);
    })
    }

    async function init() {
            await loadBook();
    }

    void init();

})