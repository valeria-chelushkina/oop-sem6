package com.library.service;

import com.library.dao.AuthorDAO;
import com.library.dto.AuthorDTO;
import com.library.entity.Author;
import com.library.mapper.AuthorMapper;
import org.mapstruct.factory.Mappers;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AuthorServiceImpl implements AuthorService {
    private final AuthorDAO authorDAO;
    private final AuthorMapper authorMapper;

    public AuthorServiceImpl() {
        this.authorDAO = new AuthorDAO();
        this.authorMapper = Mappers.getMapper(AuthorMapper.class);
    }

    @Override
    public List<AuthorDTO> findAll() throws SQLException {
        return authorDAO.findAll().stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AuthorDTO findById(Long id) throws SQLException {
        List<Author> authors = authorDAO.findById(id);
        if (authors.isEmpty()) {
            return null;
        }
        return authorMapper.toDto(authors.get(0));
    }

    @Override
    public Long create(AuthorDTO authorDTO) throws SQLException {
        return authorDAO.create(authorMapper.toEntity(authorDTO));
    }

    @Override
    public int update(AuthorDTO authorDTO) throws SQLException {
        if (authorDTO == null || authorDTO.getId() == null) {
            throw new IllegalArgumentException("Author id is required for update.");
        }
        return authorDAO.update(authorMapper.toEntity(authorDTO));
    }

    @Override
    public int deleteById(Long id) throws SQLException {
        return authorDAO.deleteById(id);
    }
}

