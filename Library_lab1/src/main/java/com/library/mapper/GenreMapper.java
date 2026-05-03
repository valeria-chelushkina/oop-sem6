package com.library.mapper;

import com.library.dto.CreateGenreRequest;
import com.library.dto.GenreDTO;
import com.library.entity.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper (componentModel = "default")
public interface GenreMapper {
    GenreDTO toDto(Genre genre);

    Genre toEntity(GenreDTO genreDTO);

    @Mapping(target = "id", ignore = true)
    Genre toEntity(CreateGenreRequest request);
}