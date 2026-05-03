-- Оцінки книг читачами (один рядок на пару book + user).
CREATE TABLE IF NOT EXISTS book_ratings (
    id         BIGSERIAL PRIMARY KEY,
    book_id    BIGINT    NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    user_id    BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    rating     INTEGER   NOT NULL CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT uq_book_ratings_book_user UNIQUE (book_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_book_ratings_book_id ON book_ratings (book_id);
