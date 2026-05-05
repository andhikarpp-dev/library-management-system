package com.example.library_management_system.controller;

import com.example.library_management_system.dto.LoanRequest;
import com.example.library_management_system.dto.LoanResponse;
import com.example.library_management_system.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/borrow")
    public ResponseEntity<LoanResponse> borrowBook(@RequestBody LoanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.borrowBook(request));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponse> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.returnBook(id));
    }

    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<LoanResponse>> getLoansByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(loanService.getLoansByMember(memberId));
    }

    @PostMapping("/sync-overdue")
    public ResponseEntity<Map<String, Object>> syncOverdue() {
        int updated = loanService.syncOverdueLoans();
        Map<String, Object> response = new HashMap<>();
        response.put("updatedCount", updated);
        response.put("message", "Overdue loans synced successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/due-soon")
    public ResponseEntity<List<Map<String, Object>>> getLoansDueSoon(@RequestParam(defaultValue = "3") int days) {
        return ResponseEntity.ok(loanService.getLoansDueSoon(days));
    }

    @GetMapping("/activity-by-month")
    public ResponseEntity<List<Map<String, Object>>> getActivityByMonth() {
        return ResponseEntity.ok(loanService.getLoanActivityByMonth());
    }

    @GetMapping("/status-summary")
    public ResponseEntity<Map<String, Long>> getStatusSummary() {
        return ResponseEntity.ok(loanService.getLoanStatusSummary());
    }
}
