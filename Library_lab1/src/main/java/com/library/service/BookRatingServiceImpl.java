package com.library.service;

import com.library.dao.BookRatingDAO;
import com.library.dto.BookRatingDTO;
import com.library.dto.RateBookRequest;
import com.library.entity.BookRating;
import com.library.mapper.BookRatingMapper;
import org.mapstruct.factory.Mappers;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class BookRatingServiceImpl implements BookRatingService {
    private final BookRatingDAO bookRatingDAO;
    private final BookRatingMapper bookRatingMapper;

    public BookRatingServiceImpl() {
        this.bookRatingDAO = new BookRatingDAO();
        this.bookRatingMapper = Mappers.getMapper(BookRatingMapper.class);
    }

    @Override
    public List<BookRatingDTO> findByBookId(Long bookId) throws SQLException {
        return bookRatingDAO.getByBookId(bookId).stream()
                .map(bookRatingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookRatingDTO rateBook(Long bookId, RateBookRequest request) throws SQLException {
        if (bookId == null) {
            throw new IllegalArgumentException("bookId is required.");
        }
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required.");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5.");
        }
        BookRating toSave = BookRating.builder()
                .bookId(bookId)
                .userId(request.getUserId())
                .rating(request.getRating())
                .build();
        BookRating saved = bookRatingDAO.upsert(toSave);
        return bookRatingMapper.toDto(saved);
    }

    @Override
    public int deleteRating(Long bookId, Long userId) throws SQLException {
        if (bookId == null || userId == null) {
            throw new IllegalArgumentException("bookId and userId are required.");
        }
        return bookRatingDAO.deleteByBookAndUser(bookId, userId);
    }
}
