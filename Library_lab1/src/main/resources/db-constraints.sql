-- Users role constraints
ALTER TABLE users
    ADD CONSTRAINT chk_users_role
    CHECK (role IN ('READER', 'LIBRARIAN'));

-- Loan enum-like constraints
ALTER TABLE loans
    ADD CONSTRAINT chk_loans_type
    CHECK (loan_type IN ('SUBSCRIPTION', 'READING_ROOM'));

ALTER TABLE loans
    ADD CONSTRAINT chk_loans_status
    CHECK (status IN ('ORDERED', 'ISSUED', 'RETURNED', 'LOST', 'DAMAGED', 'ARCHIVED'));

-- Book item status constraints
ALTER TABLE book_items
    ADD CONSTRAINT chk_book_items_status
    CHECK (status IN ('AVAILABLE', 'ORDERED', 'ISSUED', 'LOST', 'DAMAGED', 'ARCHIVED', 'READING_ROOM_ONLY'));
