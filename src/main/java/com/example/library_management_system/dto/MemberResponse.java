package com.example.library_management_system.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Long activeLoans;
}
