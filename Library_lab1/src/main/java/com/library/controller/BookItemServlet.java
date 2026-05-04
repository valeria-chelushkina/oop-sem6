package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.library.dto.BookItemDTO;
import com.library.dto.CreateBookItemRequest;
import com.library.service.BookItemService;
import com.library.service.BookItemServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/book-items")
public class BookItemServlet extends HttpServlet {
    private final BookItemService bookItemService = new BookItemServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String id = req.getParameter("id");
            String bookId = req.getParameter("bookId");
            String availableCount = req.getParameter("availableCount");

            if ("true".equalsIgnoreCase(availableCount) && bookId != null) {
                long count = bookItemService.countNumberOfAvailableCopies(Long.valueOf(bookId));
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("bookId", bookId);
                payload.put("availableCount", count);
                writeJson(resp, HttpServletResponse.SC_OK, payload);
                return;
            }

            if ("true".equalsIgnoreCase(availableCount)) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("availableCount", bookItemService.countAvailable());
                writeJson(resp, HttpServletResponse.SC_OK, payload);
                return;
            }

            if(bookId != null)
            {
                List<BookItemDTO> items = bookItemService.findByBookId(Long.valueOf(bookId));
                writeJson(resp, HttpServletResponse.SC_OK, items);
                return;
            }

            if (id != null && !id.isBlank()) {
                BookItemDTO bookItem = bookItemService.findById(Long.valueOf(id));
                if (bookItem == null) {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book item not found.");
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, bookItem);
                return;
            }

            List<BookItemDTO> items = bookItemService.findAll();
            writeJson(resp, HttpServletResponse.SC_OK, items);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id format.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CreateBookItemRequest request = objectMapper.readValue(req.getInputStream(), CreateBookItemRequest.class);
            Long createdId = bookItemService.create(request);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "Book item created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            BookItemDTO request = objectMapper.readValue(req.getInputStream(), BookItemDTO.class);
            int affected = bookItemService.update(request);
            if (affected == 0) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book item not found.");
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Book item updated successfully.");
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
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
            int affected = bookItemService.deleteById(Long.valueOf(id));
            if (affected == 0) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book item not found.");
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
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("error", message);
        writeJson(resp, status, payload);
    }
}
