package com.library.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@WebServlet ("/book/*")
public class BookPageServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(BookPageServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        logger.debug("Book page request for path: {}", pathInfo);

        if (pathInfo == null || pathInfo.equals("/")) {
            logger.warn("Empty book path requested, redirecting to home");
            resp.sendRedirect("/");
            return;
        }

        try{
            req.getRequestDispatcher("/book.html").forward(req, resp);
        }
        catch(ServletException e){
            logger.error("Servlet error during book details page redirection", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Servlet error while redirecting the book page\"}");
        }
    }
}