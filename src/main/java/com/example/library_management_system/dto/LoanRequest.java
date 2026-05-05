package com.example.library_management_system.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequest {
    private Long bookId;
    private Long memberId;
    private Integer loanDays;
}
