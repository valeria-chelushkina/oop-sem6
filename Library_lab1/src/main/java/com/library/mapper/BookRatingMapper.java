package com.library.mapper;

import com.library.dto.BookRatingDTO;
import com.library.entity.BookRating;
import org.mapstruct.Mapper;

@Mapper(componentModel = "default")
public interface BookRatingMapper {
    BookRatingDTO toDto(BookRating bookRating);

    BookRating toEntity(BookRatingDTO dto);
}
