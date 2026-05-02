package com.library.mapper;

import com.library.dto.CreateUserRequest;
import com.library.dto.UserDTO;
import com.library.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "default")
public interface UserMapper {
    UserDTO toDto(User user);

    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(UserDTO dto);

    @Mapping(target = "id", ignore = true)
    User toEntity(CreateUserRequest request);
}
