package com.library.service;

import com.library.dto.UserDTO;
import com.library.dto.CreateUserRequest;

import java.sql.SQLException;
import java.util.List;

public interface UserService {
    List<UserDTO> findAll() throws SQLException;
    UserDTO findById(Long id) throws SQLException;
    UserDTO findByEmail(String email) throws SQLException;
    Long create(CreateUserRequest request) throws SQLException;
    Long create(UserDTO userDTO) throws SQLException;
    int update(UserDTO userDTO) throws SQLException;
    int deleteById(Long id) throws SQLException;
}