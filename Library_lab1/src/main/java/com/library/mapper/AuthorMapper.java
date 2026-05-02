package com.library.mapper;

import com.library.dto.AuthorDTO;
import com.library.dto.CreateAuthorRequest;
import com.library.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "default")
public interface AuthorMapper {
    AuthorDTO toDto(Author author);

    Author toEntity(AuthorDTO authorDto);

    @Mapping(target = "id", ignore = true)
    Author toEntity(CreateAuthorRequest request);
}
