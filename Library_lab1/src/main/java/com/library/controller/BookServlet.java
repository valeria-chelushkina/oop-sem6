package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.library.dto.BookDTO;
import com.library.dto.CreateBookRequest;
import com.library.service.BookService;
import com.library.service.BookServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/books")
public class BookServlet extends HttpServlet {
    private final BookService bookService = new BookServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String id = req.getParameter("id");
            String title = req.getParameter("title");
            String author = req.getParameter("author");
            String genre = req.getParameter("genre");
            String language = req.getParameter("language");
            String query = req.getParameter("query");

            if (id != null && !id.isBlank()) {
                BookDTO book = bookService.findById(Long.valueOf(id));
                if (book == null) {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found.");
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, book);
                return;
            }

            List<BookDTO> books;
            if (query != null && !query.isBlank()) {
                books = bookService.findByTitleOrAuthor(query);
            } else if (genre != null && !genre.isBlank()) {
                books = bookService.findByGenre(genre);
            } else if (language!=null&& !language.isBlank()) {
                books=bookService.findByLanguage(language);
            }
            else {
                books = bookService.findAll();
            }
            writeJson(resp, HttpServletResponse.SC_OK, books);
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid query parameter format.");
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CreateBookRequest request = objectMapper.readValue(req.getInputStream(), CreateBookRequest.class);
            Long createdId = bookService.createWithAuthors(request);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "Book created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        if (id == null || id.isBlank()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'id' is required.");
            return;
        }
        try {
            int affected = bookService.deleteById(Long.valueOf(id));
            if (affected == 0) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book not found.");
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("deleted", affected);
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id format.");
        }
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