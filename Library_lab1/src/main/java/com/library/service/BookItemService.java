package com.library.service;

import com.library.dto.BookItemDTO;
import com.library.dto.CreateBookItemRequest;

import java.sql.SQLException;
import java.util.List;

public interface BookItemService {
    List<BookItemDTO> findAll() throws SQLException;
    BookItemDTO findById(Long id) throws SQLException;
    List<BookItemDTO> findByBookId(Long id) throws SQLException;
    int countNumberOfAvailableCopies(Long id) throws SQLException;
    int countAvailable() throws SQLException;
    Long create(CreateBookItemRequest request) throws SQLException;
    Long create(BookItemDTO bookItemDTO) throws SQLException;
    int update(BookItemDTO bookItemDTO) throws SQLException;
    int deleteById(Long id) throws SQLException;
}

