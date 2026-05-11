package com.library.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.library.dto.CreateLoanRequest;
import com.library.dto.LoanDTO;
import com.library.mapper.LoanMapper;
import com.library.service.LoanService;
import com.library.service.LoanServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/loans", "/api/loans/*"})
public class LoanServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(LoanServlet.class);
    private final LoanService loanService = new LoanServiceImpl();
    private final LoanMapper loanMapper = Mappers.getMapper(LoanMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("GET request received for LoanServlet");
        try {
            String id = req.getParameter("id");
            String active = req.getParameter("active");
            String readerId = req.getParameter("readerId");
            String bookId = req.getParameter("bookId");

            if (id != null && !id.isBlank()) {
                logger.info("Fetching loan by id: {}", id);
                LoanDTO loan = loanService.findById(Long.valueOf(id));
                if (loan == null) {
                    logger.warn("Loan not found with id: {}", id);
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Loan not found.");
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, loan);
                return;
            }

            if (readerId != null && !readerId.isBlank()) {
                if (bookId != null && !bookId.isBlank()) {
                    logger.info("Checking active loan for bookId={} and readerId={}", bookId, readerId);
                    List<LoanDTO> activeByBook = loanService.findActiveByBookAndReader(Long.valueOf(bookId), Long.valueOf(readerId));
                    writeJson(resp, HttpServletResponse.SC_OK, activeByBook);
                    return;
                }
                logger.info("Fetching orders/loans for readerId: {}", readerId);
                List<LoanDTO> orderedLoans = loanService.findOrderedByReader(Long.valueOf(readerId));
                writeJson(resp, HttpServletResponse.SC_OK, orderedLoans);
                return;
            }

            if ("true".equalsIgnoreCase(active)) {
                logger.info("Fetching all active loans");
                writeJson(resp, HttpServletResponse.SC_OK, loanService.findActiveLoans());
                return;
            }

            logger.info("Fetching all loans");
            writeJson(resp, HttpServletResponse.SC_OK, loanService.findAll());
        } catch (SQLException e) {
            logger.error("Database error in doGet", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            logger.warn("Invalid numeric parameter in doGet");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric parameter format.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        logger.debug("POST request received for path: {}", path);
        if (path == null || "/".equals(path)) {
            createLoan(req, resp);
            return;
        }
        if (path.equals("/order")) {
            createOrder(req, resp);
            return;
        }
        if (path.startsWith("/issue/")) {
            issueLoan(req, resp, path);
            return;
        }
        if (path.startsWith("/return/")) {
            returnLoan(resp, path);
            return;
        }
        logger.warn("Unsupported endpoint called: {}", path);
        writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unsupported endpoint.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        logger.debug("PUT request received for LoanServlet");
        try{
            LoanDTO request = objectMapper.readValue(req.getInputStream(), LoanDTO.class);
            logger.info("Updating loan id: {}", request.getId());
            int affected = loanService.update(request);
            if(affected == 0){
                logger.warn("Update failed: Loan not found with id: {}", request.getId());
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Loan not found.");
                return;
            }
            logger.info("Loan updated successfully: id={}", request.getId());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Loan updated successfully.");
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (JsonProcessingException e) {
            logger.warn("JSON processing error in doPut: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON or date/enum values.");
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
        logger.debug("DELETE request received for id: {}", id);
        if (id == null || id.isBlank()) {
            logger.warn("Delete request missing id");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'id' is required.");
            return;
        }
        try {
            logger.info("Deleting loan with id: {}", id);
            int affected = loanService.deleteById(Long.valueOf(id));
            if (affected == 0) {
                logger.warn("Deletion failed: Loan not found with id: {}", id);
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Loan not found.");
                return;
            }
            logger.info("Loan deleted successfully: id={}", id);
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

    private void createLoan(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CreateLoanRequest request = objectMapper.readValue(req.getInputStream(), CreateLoanRequest.class);
            logger.info("Creating new loan for bookItemId: {}, readerId: {}", request.getBookItemId(), request.getReaderId());
            Long createdId = loanService.create(loanMapper.toDto(loanMapper.toEntity(request)));
            logger.info("Loan created successfully with id: {}", createdId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "Loan created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (JsonProcessingException e) {
            logger.warn("JSON error in createLoan: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON or date/enum values.");
        } catch (SQLException e) {
            logger.error("Database error in createLoan", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    private void createOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CreateLoanRequest request = objectMapper.readValue(req.getInputStream(), CreateLoanRequest.class);
            if (request.getBookItemId() == null || request.getReaderId() == null || request.getLoanType() == null || request.getDueDate() == null) {
                logger.warn("Incomplete order request");
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "bookItemId, readerId, loanType and dueDate are required.");
                return;
            }
            logger.info("Creating order for bookItemId: {}, readerId: {}", request.getBookItemId(), request.getReaderId());
            int affected = loanService.createOrder(
                    request.getBookItemId(),
                    request.getReaderId(),
                    request.getLoanType(),
                    Date.valueOf(request.getDueDate())
            );
            logger.info("Order created successfully: {} rows affected", affected);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("created", affected);
            payload.put("message", "Order created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (JsonProcessingException e) {
            logger.warn("JSON error in createOrder: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON or date/enum values.");
        } catch (SQLException e) {
            logger.error("Database error in createOrder", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    private void issueLoan(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        try {
            Long loanId = Long.valueOf(path.substring("/issue/".length()));
            String librarianIdParam = req.getParameter("librarianId");
            if (librarianIdParam == null || librarianIdParam.isBlank()) {
                logger.warn("Issue loan requested without librarianId for loanId: {}", loanId);
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'librarianId' is required.");
                return;
            }
            Long librarianId = Long.valueOf(librarianIdParam);
            logger.info("Issuing loanId: {} by librarianId: {}", loanId, librarianId);
            int affected = loanService.issueLoan(loanId, librarianId);
            logger.info("Loan issued successfully: id={}", loanId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Loan issued successfully.");
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            logger.error("Database error in issueLoan", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            logger.warn("Invalid id format in issueLoan");
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id format.");
        }
    }

    private void returnLoan(HttpServletResponse resp, String path) throws IOException {
        try {
            Long loanId = Long.valueOf(path.substring("/return/".length()));
            logger.info("Returning loanId: {}", loanId);
            int affected = loanService.returnLoan(loanId);
            logger.info("Loan returned successfully: id={}", loanId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Loan returned successfully.");
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            logger.error("Database error in returnLoan", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            logger.warn("Invalid id format in returnLoan");
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