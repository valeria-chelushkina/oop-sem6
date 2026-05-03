package com.library.service;

import com.library.dao.BookDAO;
import com.library.dao.GenreDAO;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterService {
    private final BookDAO bookDAO = new BookDAO();
    private final GenreDAO genreDAO = new GenreDAO();

    public Map<String, List<String>> getSearchFilters() throws SQLException {
        Map<String, List<String>> filters = new HashMap<>();
        filters.put("languages", bookDAO.findUniqueLanguages());
        filters.put("genres", genreDAO.findAllGenreNames());
        return filters;
    }
}