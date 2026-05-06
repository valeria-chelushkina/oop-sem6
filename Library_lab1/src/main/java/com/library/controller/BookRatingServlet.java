package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.library.dto.BookRatingDTO;
import com.library.dto.RateBookRequest;
import com.library.service.BookRatingService;
import com.library.service.BookRatingServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GET/POST/DELETE {@code /api/books/{bookId}/ratings}.
 * POST body: {@code { "userId": 1, "rating": 5 }}.
 * DELETE: query {@code userId} mandatory.
 */
@WebServlet("/api/books/*")
public class BookRatingServlet extends HttpServlet {
    private static final Pattern RATINGS_PATH = Pattern.compile("^/(\\d+)/ratings/?$");

    private final BookRatingService bookRatingService = new BookRatingServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long bookId = parseBookId(req.getPathInfo());
        if (bookId == null) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Use GET /api/books/{id}/ratings.");
            return;
        }
        try {
            List<BookRatingDTO> ratings = bookRatingService.findByBookId(bookId);
            writeJson(resp, HttpServletResponse.SC_OK, ratings);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long bookId = parseBookId(req.getPathInfo());
        if (bookId == null) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Use POST /api/books/{id}/ratings.");
            return;
        }
        try {
            RateBookRequest body = objectMapper.readValue(req.getInputStream(), RateBookRequest.class);
            BookRatingDTO saved = bookRatingService.rateBook(bookId, body);
            writeJson(resp, HttpServletResponse.SC_OK, saved);
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long bookId = parseBookId(req.getPathInfo());
        if (bookId == null) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Use DELETE /api/books/{id}/ratings?userId=...");
            return;
        }
        String userIdParam = req.getParameter("userId");
        if (userIdParam == null || userIdParam.isBlank()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'userId' is required.");
            return;
        }
        try {
            int affected = bookRatingService.deleteRating(bookId, Long.valueOf(userIdParam));
            if (affected == 0) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Rating not found.");
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("deleted", affected);
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid userId format.");
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    private static Long parseBookId(String pathInfo) {
        if (pathInfo == null) {
            return null;
        }
        Matcher m = RATINGS_PATH.matcher(pathInfo);
        if (!m.matches()) {
            return null;
        }
        return Long.valueOf(m.group(1));
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
