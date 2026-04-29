package com.library.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Users {
    private Long id;
    private String firstName;
    private String lastName;
    private String taxId;
    private String email;
    private String passwordHash;
    private String role; // READER or LIBRARIAN
    private LocalDateTime registrationDate;
}
