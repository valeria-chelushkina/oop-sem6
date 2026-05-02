package com.library.mapper;

import com.library.dto.BookItemDto;
import com.library.dto.CreateBookItemRequest;
import com.library.entity.BookItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "default")
public interface BookItemMapper {
    BookItemDto toDto(BookItem bookItem);

    BookItem toEntity(BookItemDto dto);

    @Mapping(target = "id", ignore = true)
    BookItem toEntity(CreateBookItemRequest request);
}
