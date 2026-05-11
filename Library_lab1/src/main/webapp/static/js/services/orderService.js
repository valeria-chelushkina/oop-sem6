import { LoanApi } from '../api/loanApi.js';
import { BookApi } from '../api/bookApi.js';

export const OrderService = {
    async handleOrder(bookId, loanType) {
        try {
            const statusResponse = await fetch('/api/auth/status');
            const auth = await statusResponse.json();
            
            if (!auth.authenticated) {
                window.location.assign('/login');
                return;
            }

            // check if user already has an active loan for this book
            const activeLoans = await LoanApi.checkActiveLoan(bookId, auth.id);
            if (activeLoans && activeLoans.length > 0) {
                alert("You already have an active order or loan for this book.");
                return;
            }

            // find best available book item
            const availableItems = await BookApi.getAvailableItems(bookId);
            if (!availableItems || availableItems.length === 0) {
                alert("No available copies for this book at the moment.");
                return;
            }

            let selectedItem = null;
            if (loanType === 'READING_ROOM') {
                // priority: READING_ROOM_ONLY, then AVAILABLE
                selectedItem = availableItems.find(i => i.status === 'READING_ROOM_ONLY') || 
                               availableItems.find(i => i.status === 'AVAILABLE');
            } else {
                // priority: AVAILABLE, then READING_ROOM_ONLY (if allowed)
                selectedItem = availableItems.find(i => i.status === 'AVAILABLE') || 
                               availableItems.find(i => i.status === 'READING_ROOM_ONLY');
            }

            if (!selectedItem) {
                alert("No suitable copies for this type of loan.");
                return;
            }

            // create loan
            const payload = {
                bookItemId: selectedItem.id,
                readerId: auth.id,
                loanType: loanType,
                dueDate: loanType === 'READING_ROOM' ? new Date().toISOString().split('T')[0] : null,
                status: 'ORDERED'
            };

            await LoanApi.create(payload);
            
            alert("Book ordered successfully! You can track it in your Orders page.");
            window.location.reload();

        } catch (error) {
            console.error("Order failed:", error);
            alert("Failed to place order: " + error.message);
        }
    },

    async updateButtonStates(container, bookId, readerId) {
        const orderBtn = container.querySelector('.order');
        const rrBtn = container.querySelector('.reading-room-order');
        
        if (!orderBtn && !rrBtn) return;

        try {
            if (readerId) {
                const activeLoans = await LoanApi.checkActiveLoan(bookId, readerId);
                if (activeLoans && activeLoans.length > 0) {
                    const status = activeLoans[0].status;
                    [orderBtn, rrBtn].forEach(btn => {
                        if (btn) {
                            btn.disabled = true;
                            btn.textContent = status === 'ORDERED' ? 'Ordered' : 'Loaned';
                            btn.style.opacity = '0.6';
                        }
                    });
                    return;
                }
            }

            // check availability
            const availableItems = await BookApi.getAvailableItems(bookId);
            if (!availableItems || availableItems.length === 0) {
                [orderBtn, rrBtn].forEach(btn => {
                    if (btn) {
                        btn.disabled = true;
                        btn.textContent = 'Unavailable';
                    }
                });
            }
        } catch (error) {
            console.error("Failed to update button states:", error);
        }
    }
};
