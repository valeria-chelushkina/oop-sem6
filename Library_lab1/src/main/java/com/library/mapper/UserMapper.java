package com.library.mapper;

import com.library.dto.CreateUserRequest;
import com.library.dto.UserDto;
import com.library.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "default")
public interface UserMapper {
    UserDto toDto(User user);

    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(UserDto dto);

    @Mapping(target = "id", ignore = true)
    User toEntity(CreateUserRequest request);
}
