package com.library.mapper;

import com.library.dto.AuthorDto;
import com.library.dto.CreateAuthorRequest;
import com.library.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "default")
public interface AuthorMapper {
    AuthorDto toDto(Author author);

    Author toEntity(AuthorDto authorDto);

    @Mapping(target = "id", ignore = true)
    Author toEntity(CreateAuthorRequest request);
}
