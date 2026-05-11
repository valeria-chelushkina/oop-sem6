package com.library.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/orders")
public class OrdersPageServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(OrdersPageServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Redirecting to orders.html");
        try {
            req.getRequestDispatcher("/orders.html").forward(req, resp);
        } catch (ServletException e) {
            logger.error("Servlet error during orders page redirection", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Servlet error while redirecting the orders page\"}");
        }
    }
}
