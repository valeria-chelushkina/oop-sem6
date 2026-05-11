package com.library.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.service.FilterService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/filters")
public class FilterServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(FilterServlet.class);
    private final FilterService filterService = new FilterService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("GET request for Search Filters");
        try {
            Map<String, List<String>> filters = filterService.getSearchFilters();
            logger.info("Successfully fetched search filters");

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(resp.getWriter(), filters);
        } catch (SQLException e) {
            logger.error("Database error while fetching filters", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error while fetching filters\"}");
        }
    }
}