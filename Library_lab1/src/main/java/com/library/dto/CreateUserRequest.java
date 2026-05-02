package com.library.dto;

import com.library.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String taxId;
    private String email;
    private String passwordHash;
    private UserRole role;
    private LocalDateTime registrationDate;
}
