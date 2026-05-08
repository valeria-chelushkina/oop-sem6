package com.library.service;

import com.library.dto.CreateGenreRequest;
import com.library.dto.GenreDTO;

import java.sql.SQLException;
import java.util.List;

public interface GenreService {
    List<GenreDTO> findAll() throws SQLException;
    GenreDTO findById(Long id) throws SQLException;
    Long create(GenreDTO genreDTO) throws SQLException;
    Long create(CreateGenreRequest request) throws SQLException;
    int update(GenreDTO genreDTO) throws SQLException;
    int deleteById(Long id) throws SQLException;
}