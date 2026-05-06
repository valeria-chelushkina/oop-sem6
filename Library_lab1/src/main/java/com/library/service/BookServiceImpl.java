package com.library.service;

import com.library.dao.BookDAO;
import com.library.dto.BookDTO;
import com.library.dto.CreateAuthorRequest;
import com.library.dto.CreateBookRequest;
import com.library.dto.CreateGenreRequest;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Genre;
import com.library.mapper.BookMapper;
import org.mapstruct.factory.Mappers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BookServiceImpl implements BookService {
    private final BookDAO bookDAO;
    private final BookMapper bookMapper;

    public BookServiceImpl() {
        this.bookDAO = new BookDAO();
        this.bookMapper = Mappers.getMapper(BookMapper.class);
    }

    @Override
    public List<BookDTO> findAll() throws SQLException {
        return bookDAO.findAll().stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public BookDTO findById(Long id) throws SQLException {
        List<Book> books = bookDAO.findById(id);
        if (books.isEmpty()) {
            return null;
        }
        return bookMapper.toDto(books.getFirst());
    }

    @Override
    public List<BookDTO> findByTitle(String title) throws SQLException {
        return bookDAO.findByTitle(title).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public List<BookDTO> findByAuthor(String author) throws SQLException {
        return bookDAO.findByAuthor(author).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public List<BookDTO> findByGenre(String genre) throws SQLException {
        return bookDAO.findByGenre(genre).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public List<BookDTO> findByLanguage(String language) throws SQLException{
        return bookDAO.findByLanguage(language).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public List<BookDTO> findByTitleOrAuthor (String query) throws SQLException {
        return bookDAO.findByTitleOrAuthor(query).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public List<BookDTO> searchBooks(String query, List<String> genres, List<String> languages) throws SQLException {
        return bookDAO.searchBooks(query, genres, languages).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public Long create(BookDTO bookDTO) throws SQLException {
        Book book = bookMapper.toEntity(bookDTO);
        return bookDAO.createWithAuthors(book);
    }

    @Override
    public Long createWithAuthors(CreateBookRequest request) throws SQLException {
        Book book = bookMapper.toEntity(request);
        book.setAuthors(collectAuthorsFromRequest(request));
        book.setGenres(collectGenresFromRequest(request));
        return bookDAO.createWithAuthors(book);
    }

    @Override
    public int update(BookDTO bookDTO) throws SQLException {
        if (bookDTO == null || bookDTO.getId() == null) {
            throw new IllegalArgumentException("Book id is required for update.");
        }
        return bookDAO.update(bookMapper.toEntity(bookDTO));
    }

    @Override
    public int deleteById(Long id) throws SQLException {
        return bookDAO.deleteById(id);
    }

    private List<Author> collectAuthorsFromRequest(CreateBookRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }

        List<Author> authors = new ArrayList<>();
        if (request.getAuthorIds() != null) {
            authors.addAll(
                    request.getAuthorIds().stream()
                            .filter(Objects::nonNull)
                            .map(id -> Author.builder().id(id).build())
                            .toList()
            );
        }
        if (request.getNewAuthors() != null) {
            authors.addAll(
                    request.getNewAuthors().stream()
                            .filter(Objects::nonNull)
                            .map(this::mapCreateAuthorRequestToAuthor)
                            .toList()
            );
        }
        return authors;
    }

    private Author mapCreateAuthorRequestToAuthor(CreateAuthorRequest request) {
        return Author.builder()
                .penName(request.getPenName())
                .biography(request.getBiography())
                .build();
    }

    private List<Genre> collectGenresFromRequest(CreateBookRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }
        List<Genre> genres = new ArrayList<>();
        if (request.getGenreIds() != null) {
            genres.addAll(
                    request.getGenreIds().stream()
                            .filter(Objects::nonNull)
                            .map(id -> Genre.builder().id(id).build())
                            .toList()
            );
        }
        if (request.getNewGenres() != null) {
            genres.addAll(
                    request.getNewGenres().stream()
                            .filter(Objects::nonNull)
                            .map(this::mapCreateGenreRequestToGenre)
                            .toList()
            );
        }
        return genres;
    }

    private Genre mapCreateGenreRequestToGenre(CreateGenreRequest request) {
        return Genre.builder()
                .name(request.getName())
                .build();
    }
}
