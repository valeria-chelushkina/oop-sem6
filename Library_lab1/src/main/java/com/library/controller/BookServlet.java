package com.library.controller;

import com.library.dao.BookDAO;

import com.library.entity.Book;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/books")

public class BookServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(BookServlet.class);

    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        res.setContentType("text/html");
        res.setCharacterEncoding("UTF-8");

        try (PrintWriter out = res.getWriter()) {
            List<Book> books = bookDAO.findAll();
            out.println("<html><body><h1>=== Library list book ===</h1></ br>");

            if (books.isEmpty()) {
                out.println("<p>No books in DB yet.</p>");
            } else {
                for (Book book : books) {
                    out.println("<p>ID: " + book.getId() + "</p>");
                    out.println("<p>Title: " + book.getTitle() + "</p>");
                    out.println("<p>Pages count: " + book.getPagesCount() + "</p>");
                    out.println("</ hr>");
                }
            }
            out.println("</body></html>");
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.error("Error while getting books", e);
            res.getWriter().println("<html><body><p>Error while getting books list.</p></body</html>");
        }
    }
}
