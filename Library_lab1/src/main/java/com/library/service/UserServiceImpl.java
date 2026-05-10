package com.library.service;

import com.library.dao.UserDAO;
import com.library.dto.CreateUserRequest;
import com.library.dto.UserDTO;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import org.mapstruct.factory.Mappers;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;
    private final UserMapper userMapper;

    public UserServiceImpl() {
        this.userDAO = new UserDAO();
        this.userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Override
    public List<UserDTO> findAll() throws SQLException {
        return userDAO.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO findById(Long id) throws SQLException {
        List<User> users = userDAO.findById(id);
        if (users.isEmpty()) {
            return null;
        }
        return userMapper.toDto(users.getFirst());
    }

    @Override
    public UserDTO findByEmail(String email) throws SQLException {
        List<User> users = userDAO.findByEmail(email);
        if (users.isEmpty()) {
            return null;
        }
        return userMapper.toDto(users.getFirst());
    }

    @Override
    public Long create(CreateUserRequest request) throws SQLException {
        return userDAO.create(userMapper.toEntity(request));
    }

    @Override
    public Long create(UserDTO userDTO) throws SQLException {
        return userDAO.create(userMapper.toEntity(userDTO));
    }

    @Override
    public int update(UserDTO userDTO) throws SQLException {
        if (userDTO == null || userDTO.getId() == null) {
            throw new IllegalArgumentException("User id is required for update.");
        }
        return userDAO.update(userMapper.toEntity(userDTO));
    }

    @Override
    public int deleteById(Long id) throws SQLException {
        return userDAO.deleteById(id);
    }
}

