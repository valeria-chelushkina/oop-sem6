package com.library.service;

import com.library.dto.BookRatingDTO;
import com.library.dto.RateBookRequest;

import java.sql.SQLException;
import java.util.List;

public interface BookRatingService {
    List<BookRatingDTO> findByBookId(Long bookId) throws SQLException;

    BookRatingDTO rateBook(Long bookId, RateBookRequest request) throws SQLException;

    int deleteRating(Long bookId, Long userId) throws SQLException;
}
