package com.example.library_management_system.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberRequest {
    private String name;
    private String email;
    private String phone;
}
