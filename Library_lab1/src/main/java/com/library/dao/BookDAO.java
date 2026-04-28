package com.library.dao;

import com.library.entity.Book;
import com.library.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public List<Book> findAll() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(Book.builder()
                        .id(rs.getLong("id"))
                        .title(rs.getString("title"))
                        .isbn(rs.getString("isbn"))
                        .publisher(rs.getString("publisher"))
                        .publicationYear(rs.getInt("publicationYear"))
                        .coverURL(rs.getString("coverURL"))
                        .language(rs.getString("language"))
                        .pagesCount(rs.getInt("pagesCount"))
                        .genre(rs.getString("genre"))
                        .description(rs.getString("description"))
                        .build());
            }
        }
        return books;
    }
}
