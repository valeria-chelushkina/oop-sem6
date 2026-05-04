package com.library.service;

import com.library.dao.BookItemDAO;
import com.library.dto.BookItemDTO;
import com.library.dto.CreateBookItemRequest;
import com.library.entity.BookItem;
import com.library.mapper.BookItemMapper;
import org.mapstruct.factory.Mappers;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class BookItemServiceImpl implements BookItemService {
    private final BookItemDAO bookItemDAO;
    private final BookItemMapper bookItemMapper;

    public BookItemServiceImpl() {
        this.bookItemDAO = new BookItemDAO();
        this.bookItemMapper = Mappers.getMapper(BookItemMapper.class);
    }

    @Override
    public List<BookItemDTO> findAll() throws SQLException {
        return bookItemDAO.findAll().stream()
                .map(bookItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookItemDTO findById(Long id) throws SQLException {
        List<BookItem> bookItems = bookItemDAO.findById(id);
        if (bookItems.isEmpty()) {
            return null;
        }
        return bookItemMapper.toDto(bookItems.get(0));
    }

    @Override
    public List<BookItemDTO> findByBookId(Long id) throws SQLException{
        return bookItemDAO.findByBookId(id).stream()
                .map(bookItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public int countNumberOfAvailableCopies(Long id) throws SQLException {
        return bookItemDAO.countNumberOfAvailableCopies(id);
    }

    @Override
    public int countAvailable() throws SQLException {
        return bookItemDAO.countAvailableBookItems();
    }

    @Override
    public Long create(CreateBookItemRequest request) throws SQLException {
        return bookItemDAO.create(bookItemMapper.toEntity(request));
    }

    @Override
    public Long create(BookItemDTO bookItemDTO) throws SQLException {
        return bookItemDAO.create(bookItemMapper.toEntity(bookItemDTO));
    }

    @Override
    public int update(BookItemDTO bookItemDTO) throws SQLException {
        if (bookItemDTO == null || bookItemDTO.getId() == null) {
            throw new IllegalArgumentException("BookItem id is required for update.");
        }
        return bookItemDAO.update(bookItemMapper.toEntity(bookItemDTO));
    }

    @Override
    public int deleteById(Long id) throws SQLException {
        return bookItemDAO.deleteById(id);
    }
}

