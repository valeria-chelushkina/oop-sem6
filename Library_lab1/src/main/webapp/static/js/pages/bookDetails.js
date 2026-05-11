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

            // bind order events
            cardElement.querySelector('.order')?.addEventListener('click', () => OrderService.handleOrder(bookId, 'SUBSCRIPTION'));
            cardElement.querySelector('.reading-room-order')?.addEventListener('click', () => OrderService.handleOrder(bookId, 'READING_ROOM'));

            // setup rating system
            setupRatingSystem(cardElement, bookId, readerId);
        }

	}

    function setupRatingSystem(container, bookId, userId) {
        const ratingSection = container.querySelector('.interactive-rating-section');
        if (!userId) {
            ratingSection.innerHTML = '<p style="font-size: 14px; font-style: italic; color: #666;">Sign in to rate this book</p>';
            return;
        }

        const starsContainer = container.querySelector('#stars-interactive');
        const stars = starsContainer.querySelectorAll('.star');
        const removeBtn = container.querySelector('#remove-rating');

        // check if user already rated
        checkUserRating(bookId, userId, stars, removeBtn);

        stars.forEach(star => {
            star.addEventListener('mouseover', () => {
                const val = parseInt(star.dataset.value);
                highlightStars(stars, val, 'hovered');
            });

            star.addEventListener('mouseout', () => {
                removeHighlight(stars, 'hovered');
            });

            star.addEventListener('click', async () => {
                const val = parseInt(star.dataset.value);
                await submitRating(bookId, userId, val, stars, removeBtn);
            });
        });

        removeBtn.addEventListener('click', async () => {
            await deleteRating(bookId, userId, stars, removeBtn);
        });
    }

    async function checkUserRating(bookId, userId, stars, removeBtn) {
        try {
            const response = await fetch(`/api/books/${bookId}/ratings`);
            const ratings = await response.json();
            const userRating = ratings.find(r => r.userId === userId);

            if (userRating) {
                highlightStars(stars, userRating.rating, 'selected');
                removeBtn.style.display = 'inline-block';
            }
        } catch (error) {
            console.error("Failed to check user rating:", error);
        }
    }

    function highlightStars(stars, val, className) {
        stars.forEach(s => {
            if (parseInt(s.dataset.value) <= val) {
                s.classList.add(className);
            } else {
                s.classList.remove(className);
            }
        });
    }

    function removeHighlight(stars, className) {
        stars.forEach(s => s.classList.remove(className));
    }

    async function submitRating(bookId, userId, rating, stars, removeBtn) {
        try {
            const response = await fetch(`/api/books/${bookId}/ratings`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId, rating })
            });

            if (response.ok) {
                highlightStars(stars, rating, 'selected');
                removeBtn.style.display = 'inline-block';
                window.location.reload();
            }
        } catch (error) {
            alert("Failed to submit rating");
        }
    }

    async function deleteRating(bookId, userId, stars, removeBtn) {
        try {
            const response = await fetch(`/api/books/${bookId}/ratings?userId=${userId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                removeHighlight(stars, 'selected');
                removeBtn.style.display = 'none';
                window.location.reload();
            }
        } catch (error) {
            alert("Failed to remove rating");
        }
    }

	init();
})