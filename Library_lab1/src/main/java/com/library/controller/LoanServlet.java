package com.library.controller;

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

@WebServlet("/api/loans/*")
public class LoanServlet extends HttpServlet {
    private final LoanService loanService = new LoanServiceImpl();
    private final LoanMapper loanMapper = Mappers.getMapper(LoanMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String id = req.getParameter("id");
            String active = req.getParameter("active");
            String readerId = req.getParameter("readerId");

            if (id != null && !id.isBlank()) {
                LoanDTO loan = loanService.findById(Long.valueOf(id));
                if (loan == null) {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Loan not found.");
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, loan);
                return;
            }

            if (readerId != null && !readerId.isBlank()) {
                List<LoanDTO> orderedLoans = loanService.findOrderedByReader(Long.valueOf(readerId));
                writeJson(resp, HttpServletResponse.SC_OK, orderedLoans);
                return;
            }

            if ("true".equalsIgnoreCase(active)) {
                writeJson(resp, HttpServletResponse.SC_OK, loanService.findActiveLoans());
                return;
            }

            writeJson(resp, HttpServletResponse.SC_OK, loanService.findAll());
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric parameter format.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
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
        writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unsupported endpoint.");
    }

    private void createLoan(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CreateLoanRequest request = objectMapper.readValue(req.getInputStream(), CreateLoanRequest.class);
            Long createdId = loanService.create(loanMapper.toDto(loanMapper.toEntity(request)));
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", createdId);
            payload.put("message", "Loan created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    private void createOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CreateLoanRequest request = objectMapper.readValue(req.getInputStream(), CreateLoanRequest.class);
            if (request.getBookItemId() == null || request.getReaderId() == null || request.getLoanType() == null || request.getDueDate() == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "bookItemId, readerId, loanType and dueDate are required.");
                return;
            }
            int affected = loanService.createOrder(
                    request.getBookItemId(),
                    request.getReaderId(),
                    request.getLoanType(),
                    Date.valueOf(request.getDueDate())
            );
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("created", affected);
            payload.put("message", "Order created successfully.");
            writeJson(resp, HttpServletResponse.SC_CREATED, payload);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        }
    }

    private void issueLoan(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        try {
            Long loanId = Long.valueOf(path.substring("/issue/".length()));
            String librarianIdParam = req.getParameter("librarianId");
            if (librarianIdParam == null || librarianIdParam.isBlank()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'librarianId' is required.");
                return;
            }
            Long librarianId = Long.valueOf(librarianIdParam);
            int affected = loanService.issueLoan(loanId, librarianId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Loan issued successfully.");
            writeJson(resp, HttpServletResponse.SC_OK, payload);
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id format.");
        }
    }

    private void returnLoan(HttpServletResponse resp, String path) throws IOException {
        try {
            Long loanId = Long.valueOf(path.substring("/return/".length()));
            int affected = loanService.returnLoan(loanId);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("updated", affected);
            payload.put("message", "Loan returned successfully.");
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
