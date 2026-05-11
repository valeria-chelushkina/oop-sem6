package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.library.dto.BookDTO;
import com.library.dto.BookRatingDTO;
import com.library.dto.CreateBookRequest;
import com.library.dto.RateBookRequest;
import com.library.service.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = {"/api/books", "/api/books/*"})
public class BookServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(BookServlet.class);
    private final BookService bookService = new BookServiceImpl();
    private final BookRatingService bookRatingService = new BookRatingServiceImpl();
    private final BookItemService bookItemService = new BookItemServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final Pattern ID_PATTERN = Pattern.compile("^/(\\d+)/?$");
    private static final Pattern RATINGS_PATTERN = Pattern.compile("^/(\\d+)/ratings/?$");
    private static final Pattern AVAILABLE_ITEMS_PATTERN = Pattern.compile("^/(\\d+)/available-items/?$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        logger.debug("GET request for path: {}", pathInfo);
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // List or Search
                String query = req.getParameter("query");
                List<String> genres = collectRepeatedParams(req, "genre");
                List<String> languages = collectRepeatedParams(req, "language");
                logger.info("Searching books with query='{}', genres={}, languages={}", query, genres, languages);
                List<BookDTO> books = bookService.searchBooks(query, genres, languages);
                writeJson(resp, HttpServletResponse.SC_OK, books);
                return;
            }

            Matcher availableItemsMatcher = AVAILABLE_ITEMS_PATTERN.matcher(pathInfo);
            if (availableItemsMatcher.matches()) {
                Long bookId = Long.valueOf(availableItemsMatcher.group(1));
                logger.info("Fetching available items for bookId: {}", bookId);
                writeJson(resp, HttpServletResponse.SC_OK, bookItemService.findAvailableByBookId(bookId));
                return;
            }

            Matcher ratingsMatcher = RATINGS_PATTERN.matcher(pathInfo);
            if (ratingsMatcher.matches()) {
                Long bookId = Long.valueOf(ratingsMatcher.group(1));
                logger.info("Fetching ratings for bookId: {}", bookId);
                List<BookRatingDTO> ratings = bookRatingService.findByBookId(bookId);
                writeJson(resp, HttpServletResponse.SC_OK, ratings);
                return;
            }

            Matcher idMatcher = ID_PATTERN.matcher(pathInfo);
            if (idMatcher.matches()) {
                Long id = Long.valueOf(idMatcher.group(1));
                logger.info("Fetching book details for id: {}", id);
                BookDTO book = bookService.findById(id);
                if (book == null) {
                    logger.warn("Book not found with id: {}", id);
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found.");
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, book);
                return;
            }

            logger.warn("Invalid path requested: {}", pathInfo);
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Invalid path.");
        } catch (SQLException e) {
            logger.error("Database error in doGet", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    /**
     * Handles book creation and rating submissions.
     * POST /api/books - Creates a new book (Librarian only).
     * POST /api/books/{id}/ratings - Submits/updates a user rating (1-5).
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        logger.debug("POST request for path: {}", pathInfo);
        try {
            if (pathInfo != null) {
                Matcher ratingsMatcher = RATINGS_PATTERN.matcher(pathInfo);
                if (ratingsMatcher.matches()) {
                    Long bookId = Long.valueOf(ratingsMatcher.group(1));
                    RateBookRequest body = objectMapper.readValue(req.getInputStream(), RateBookRequest.class);
                    logger.info("Rating bookId={} by userId={}: {} stars", bookId, body.getUserId(), body.getRating());
                    BookRatingDTO saved = bookRatingService.rateBook(bookId, body);
                    writeJson(resp, HttpServletResponse.SC_OK, saved);
                    return;
                }
            }

            // Create Book
            CreateBookRequest request = objectMapper.readValue(req.getInputStream(), CreateBookRequest.class);
            logger.info("Creating new book: {}", request.getTitle());
            Long createdId = bookService.createWithAuthors(request);
            logger.info("Book created successfully with id: {}", createdId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "Book created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (SQLException e) {
            logger.error("Database error in doPost", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid parameters in doPost: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body or parameters.");
        } catch (Exception e) {
            logger.error("Unexpected error in doPost", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("PUT request received");
        try {
            BookDTO request = objectMapper.readValue(req.getInputStream(), BookDTO.class);
            logger.info("Updating book with id: {}", request.getId());
            int affected = bookService.update(request);
            if (affected == 0) {
                logger.warn("Update failed: Book not found with id: {}", request.getId());
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found.");
                return;
            }
            logger.info("Book updated successfully: id={}", request.getId());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Book updated successfully.");
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            logger.warn("JSON mapping error in doPut: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body: " + e.getMessage());
        } catch (SQLException e) {
            logger.error("Database error in doPut", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error in doPut: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        logger.debug("DELETE request for path: {}", pathInfo);
        try {
            if (pathInfo != null) {
                Matcher ratingsMatcher = RATINGS_PATTERN.matcher(pathInfo);
                if (ratingsMatcher.matches()) {
                    Long bookId = Long.valueOf(ratingsMatcher.group(1));
                    String userIdParam = req.getParameter("userId");
                    if (userIdParam == null || userIdParam.isBlank()) {
                        logger.warn("Missing userId for rating deletion on bookId: {}", bookId);
                        writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'userId' is required.");
                        return;
                    }
                    logger.info("Deleting rating for bookId={} and userId={}", bookId, userIdParam);
                    int affected = bookRatingService.deleteRating(bookId, Long.valueOf(userIdParam));
                    if (affected == 0) {
                        logger.warn("Rating deletion failed: Rating not found for bookId={} userId={}", bookId, userIdParam);
                        writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Rating not found.");
                        return;
                    }
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("deleted", affected);
                    writeJson(resp, HttpServletResponse.SC_OK, payload);
                    return;
                }

                Matcher idMatcher = ID_PATTERN.matcher(pathInfo);
                if (idMatcher.matches()) {
                    Long id = Long.valueOf(idMatcher.group(1));
                    logger.info("Deleting book with id: {}", id);
                    int affected = bookService.deleteById(id);
                    if (affected == 0) {
                        logger.warn("Deletion failed: Book not found with id: {}", id);
                        writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found.");
                        return;
                    }
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("deleted", affected);
                    writeJson(resp, HttpServletResponse.SC_OK, payload);
                    return;
                }
            }

            // Fallback to query parameter
            String id = req.getParameter("id");
            if (id != null && !id.isBlank()) {
                logger.info("Deleting book with id (param): {}", id);
                int affected = bookService.deleteById(Long.valueOf(id));
                if (affected == 0) {
                    logger.warn("Deletion failed: Book not found with id: {}", id);
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found.");
                    return;
                }
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("deleted", affected);
                writeJson(resp, HttpServletResponse.SC_OK, payload);
                return;
            }

            logger.warn("Delete request missing id");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID is required.");
        } catch (SQLException e) {
            logger.error("Database error in doDelete", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            logger.warn("Invalid id format in doDelete");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id format.");
        }
    }

    /** Параметри виду {@code ?genre=a&genre=b} для OR у межах однієї групи. */
    private static List<String> collectRepeatedParams(HttpServletRequest req, String name) {
        String[] raw = req.getParameterValues(name);
        if (raw == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String v : raw) {
            if (v != null && !v.isBlank()) {
                out.add(v.trim());
            }
        }
        return out;
    }

    private void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resp.getWriter(), body);
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", message);
        writeJson(resp, status, error);
    }
}

