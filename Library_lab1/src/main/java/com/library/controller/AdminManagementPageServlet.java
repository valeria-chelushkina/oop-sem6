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

@WebServlet ("/management")
public class AdminManagementPageServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(AdminManagementPageServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Redirecting to adminManagement.html");
        try{
            req.getRequestDispatcher("/adminManagement.html").forward(req, resp);
        }
        catch(ServletException e){
            logger.error("Servlet error during admin management page redirection", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Servlet error while redirecting the admin management page\"}");
        }
    }
}
