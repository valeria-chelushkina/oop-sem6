package com.library.mapper;

import com.library.dto.BookDto;
import com.library.dto.CreateBookRequest;
import com.library.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "default", uses = AuthorMapper.class)
public interface BookMapper {
    BookDto toDto(Book book);

    Book toEntity(BookDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authors", ignore = true)
    Book toEntity(CreateBookRequest request);
}
