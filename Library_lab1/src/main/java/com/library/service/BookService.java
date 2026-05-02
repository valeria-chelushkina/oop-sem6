package com.library.service;

import com.library.dto.BookDTO;
import com.library.dto.CreateBookRequest;

import java.sql.SQLException;
import java.util.List;

public interface BookService {
    List<BookDTO> findAll() throws SQLException;
    BookDTO findById(Long id) throws SQLException;
    List<BookDTO> findByTitle(String title) throws SQLException;
    List<BookDTO> findByAuthor(String author) throws SQLException;
    List<BookDTO> findByGenre(String genre) throws SQLException;
    List<BookDTO> findByLanguage(String language) throws SQLException;
    List<BookDTO> findByTitleOrAuthor(String query) throws SQLException;
    Long create(BookDTO bookDTO) throws SQLException;
    Long createWithAuthors(CreateBookRequest request) throws SQLException;
    int update(BookDTO bookDTO) throws SQLException;
    int deleteById(Long id) throws SQLException;
}
