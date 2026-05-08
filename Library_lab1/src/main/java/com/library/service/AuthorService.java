package com.library.service;

import com.library.dto.AuthorDTO;
import com.library.dto.CreateAuthorRequest;

import java.sql.SQLException;
import java.util.List;

public interface AuthorService {
    List<AuthorDTO> findAll() throws SQLException;
    AuthorDTO findById(Long id) throws SQLException;
    Long create(AuthorDTO authorDTO) throws SQLException;
    Long create(CreateAuthorRequest request) throws SQLException;
    int update(AuthorDTO authorDTO) throws SQLException;
    int deleteById(Long id) throws SQLException;
}