package com.library.filters;

import com.library.dto.UserDTO;
import com.library.entity.enums.UserRole;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/api/*", "/adminManagement.html",
        "/book.html", "/management"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getServletPath();
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }

        // allow public files to pass through
        if(path.startsWith("/static/") || path.equals("/index.html") || path.equals("/") || path.equals("/login")) {
            chain.doFilter(request, response);
            return;
        }

        String method = req.getMethod();
        if("GET".equalsIgnoreCase(method)) {
            if(path.startsWith("/api/books") || path.startsWith("/api/authors") || path.startsWith("/api/genres") || path.startsWith("/api/filter") ) {
                chain.doFilter(request, response);
                return;
            }
        }

        // check for session
        HttpSession session = req.getSession(false);
        UserDTO user = (session!=null) ? (UserDTO) session.getAttribute("user") : null;

        // logic for guests (not logged in)
        if (user == null) {
            if (path.startsWith("/api/")) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }

        // role-based access control
        // only librarians can see and get to admin-related info
        if (path.startsWith("/management") || path.startsWith("/adminManagement.html") || path.startsWith("/api/admin")) {
            if (user.getRole() != UserRole.LIBRARIAN) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Librarians only!");
                return;
            }
        }

        // if all checks - continue
        chain.doFilter(request, response);
    }
}
