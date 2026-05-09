package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.library.dto.BookDTO;
import com.library.dto.BookRatingDTO;
import com.library.dto.CreateBookRequest;
import com.library.dto.RateBookRequest;
import com.library.service.BookRatingService;
import com.library.service.BookRatingServiceImpl;
import com.library.service.BookService;
import com.library.service.BookServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    private final BookService bookService = new BookServiceImpl();
    private final BookRatingService bookRatingService = new BookRatingServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final Pattern ID_PATTERN = Pattern.compile("^/(\\d+)/?$");
    private static final Pattern RATINGS_PATTERN = Pattern.compile("^/(\\d+)/ratings/?$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // List or Search
                String query = req.getParameter("query");
                List<BookDTO> books = bookService.searchBooks(
                        query,
                        collectRepeatedParams(req, "genre"),
                        collectRepeatedParams(req, "language")
                );
                writeJson(resp, HttpServletResponse.SC_OK, books);
                return;
            }

            Matcher ratingsMatcher = RATINGS_PATTERN.matcher(pathInfo);
            if (ratingsMatcher.matches()) {
                Long bookId = Long.valueOf(ratingsMatcher.group(1));
                List<BookRatingDTO> ratings = bookRatingService.findByBookId(bookId);
                writeJson(resp, HttpServletResponse.SC_OK, ratings);
                return;
            }

            Matcher idMatcher = ID_PATTERN.matcher(pathInfo);
            if (idMatcher.matches()) {
                Long id = Long.valueOf(idMatcher.group(1));
                BookDTO book = bookService.findById(id);
                if (book == null) {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found.");
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, book);
                return;
            }

            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Invalid path.");
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo != null) {
                Matcher ratingsMatcher = RATINGS_PATTERN.matcher(pathInfo);
                if (ratingsMatcher.matches()) {
                    Long bookId = Long.valueOf(ratingsMatcher.group(1));
                    RateBookRequest body = objectMapper.readValue(req.getInputStream(), RateBookRequest.class);
                    BookRatingDTO saved = bookRatingService.rateBook(bookId, body);
                    writeJson(resp, HttpServletResponse.SC_OK, saved);
                    return;
                }
            }

            // Create Book
            CreateBookRequest request = objectMapper.readValue(req.getInputStream(), CreateBookRequest.class);
            Long createdId = bookService.createWithAuthors(request);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "Book created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (SQLException e) {
            e.printStackTrace();
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body or parameters.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            BookDTO request = objectMapper.readValue(req.getInputStream(), BookDTO.class);
            int affected = bookService.update(request);
            if (affected == 0) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found.");
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Book updated successfully.");
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body: " + e.getMessage());
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo != null) {
                Matcher ratingsMatcher = RATINGS_PATTERN.matcher(pathInfo);
                if (ratingsMatcher.matches()) {
                    Long bookId = Long.valueOf(ratingsMatcher.group(1));
                    String userIdParam = req.getParameter("userId");
                    if (userIdParam == null || userIdParam.isBlank()) {
                        writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'userId' is required.");
                        return;
                    }
                    int affected = bookRatingService.deleteRating(bookId, Long.valueOf(userIdParam));
                    if (affected == 0) {
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
                    int affected = bookService.deleteById(id);
                    if (affected == 0) {
                        writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found.");
                        return;
                    }
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("deleted", affected);
                    writeJson(resp, HttpServletResponse.SC_OK, payload);
                    return;
                }
            }

            // Fallback to query parameter for backward compatibility or if no path ID
            String id = req.getParameter("id");
            if (id != null && !id.isBlank()) {
                int affected = bookService.deleteById(Long.valueOf(id));
                if (affected == 0) {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found.");
                    return;
                }
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("deleted", affected);
                writeJson(resp, HttpServletResponse.SC_OK, payload);
                return;
            }

            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID is required.");
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
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

