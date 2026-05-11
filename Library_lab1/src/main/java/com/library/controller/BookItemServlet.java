package com.library.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/book-items")
public class BookItemServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(BookItemServlet.class);
    private final BookItemService bookItemService = new BookItemServiceImpl();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("GET request for BookItemServlet");
        try {
            String id = req.getParameter("id");
            String bookId = req.getParameter("bookId");
            String availableCount = req.getParameter("availableCount");

            if ("true".equalsIgnoreCase(availableCount) && bookId != null) {
                logger.info("Fetching available copies count for bookId: {}", bookId);
                long count = bookItemService.countNumberOfAvailableCopies(Long.valueOf(bookId));
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("bookId", bookId);
                payload.put("availableCount", count);
                writeJson(resp, HttpServletResponse.SC_OK, payload);
                return;
            }

            if ("true".equalsIgnoreCase(availableCount)) {
                logger.info("Fetching total available copies count");
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("availableCount", bookItemService.countAvailable());
                writeJson(resp, HttpServletResponse.SC_OK, payload);
                return;
            }

            if(bookId != null)
            {
                logger.info("Fetching all book items for bookId: {}", bookId);
                List<BookItemDTO> items = bookItemService.findByBookId(Long.valueOf(bookId));
                writeJson(resp, HttpServletResponse.SC_OK, items);
                return;
            }

            if (id != null && !id.isBlank()) {
                logger.info("Fetching book item by id: {}", id);
                BookItemDTO bookItem = bookItemService.findById(Long.valueOf(id));
                if (bookItem == null) {
                    logger.warn("Book item not found with id: {}", id);
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book item not found.");
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, bookItem);
                return;
            }

            logger.info("Fetching all book items");
            List<BookItemDTO> items = bookItemService.findAll();
            writeJson(resp, HttpServletResponse.SC_OK, items);
        } catch (SQLException e) {
            logger.error("Database error in doGet", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            logger.warn("Invalid id format in doGet");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id format.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("POST request for BookItemServlet");
        try {
            CreateBookItemRequest request = objectMapper.readValue(req.getInputStream(), CreateBookItemRequest.class);
            logger.info("Creating new book item for bookId: {}", request.getBookId());
            Long createdId = bookItemService.create(request);
            logger.info("Book item created successfully with id: {}", createdId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "Book item created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (JsonProcessingException e) {
            logger.warn("JSON error in doPost: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON or enum values (use BookItemStatus names, e.g. AVAILABLE).");
        } catch (SQLException e) {
            logger.error("Database error in doPost", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("PUT request for BookItemServlet");
        try {
            BookItemDTO request = objectMapper.readValue(req.getInputStream(), BookItemDTO.class);
            logger.info("Updating book item id: {}", request.getId());
            int affected = bookItemService.update(request);
            if (affected == 0) {
                logger.warn("Update failed: Book item not found with id: {}", request.getId());
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book item not found.");
                return;
            }
            logger.info("Book item updated successfully: id={}", request.getId());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Book item updated successfully.");
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (JsonProcessingException e) {
            logger.warn("JSON error in doPut: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON or enum values (use BookItemStatus names, e.g. AVAILABLE).");
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
        String id = req.getParameter("id");
        logger.debug("DELETE request for BookItemServlet, id: {}", id);
        if (id == null || id.isBlank()) {
            logger.warn("Delete request missing id");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'id' is required.");
            return;
        }
        try {
            logger.info("Deleting book item with id: {}", id);
            int affected = bookItemService.deleteById(Long.valueOf(id));
            if (affected == 0) {
                logger.warn("Deletion failed: Book item not found with id: {}", id);
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Book item not found.");
                return;
            }
            logger.info("Book item deleted successfully: id={}", id);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("deleted", affected);
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            logger.error("Database error in doDelete", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            logger.warn("Invalid id format in doDelete");
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
