package com.example.library_management_system.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreStatResponse {
    private String genre;
    private Long totalBooks;
    private Long totalLoaned;
}
