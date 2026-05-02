package com.library.mapper;

import com.library.dto.BookItemDTO;
import com.library.dto.CreateBookItemRequest;
import com.library.entity.BookItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "default")
public interface BookItemMapper {
    BookItemDTO toDto(BookItem bookItem);

    BookItem toEntity(BookItemDTO dto);

    @Mapping(target = "id", ignore = true)
    BookItem toEntity(CreateBookItemRequest request);
}
