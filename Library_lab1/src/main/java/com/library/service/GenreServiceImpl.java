package com.library.service;

import com.library.dao.GenreDAO;
import com.library.dto.CreateGenreRequest;
import com.library.dto.GenreDTO;
import com.library.entity.Genre;
import com.library.mapper.GenreMapper;
import org.mapstruct.factory.Mappers;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class GenreServiceImpl implements GenreService{
    private final GenreDAO genreDAO;
    private final GenreMapper genreMapper;

    public GenreServiceImpl() {
        this.genreDAO = new GenreDAO();
        this.genreMapper = Mappers.getMapper(GenreMapper.class);
    }

    @Override
    public List<GenreDTO> findAll() throws SQLException {
        return genreDAO.findAll().stream()
                .map(genreMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public GenreDTO findById(Long id) throws SQLException {
        List<Genre> genres = genreDAO.findById(id);
        if (genres.isEmpty()) {
            return null;
        }
        return genreMapper.toDto(genres.getFirst());
    }

    @Override
    public Long create(GenreDTO genreDTO) throws SQLException {
        return genreDAO.create(genreMapper.toEntity(genreDTO));
    }

    @Override
    public Long create(CreateGenreRequest request) throws SQLException {
        return genreDAO.create(genreMapper.toEntity(request));
    }

    @Override
    public int update(GenreDTO genreDTO) throws SQLException {
        if (genreDTO == null || genreDTO.getId() == null) {
            throw new IllegalArgumentException("Author id is required for update.");
        }
        return genreDAO.update(genreMapper.toEntity(genreDTO));
    }

    @Override
    public int deleteById(Long id) throws SQLException {
        return genreDAO.deleteById(id);
    }
}