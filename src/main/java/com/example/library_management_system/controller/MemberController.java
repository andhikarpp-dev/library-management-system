package com.example.library_management_system.controller;

import com.example.library_management_system.dto.MemberRequest;
import com.example.library_management_system.dto.MemberResponse;
import com.example.library_management_system.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> registerMember(@RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.registerMember(request));
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(@PathVariable Long id, @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.updateMember(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active-loans")
    public ResponseEntity<List<MemberResponse>> getMembersWithActiveLoans() {
        return ResponseEntity.ok(memberService.getMembersWithActiveLoans());
    }

    @GetMapping("/{id}/loan-summary")
    public ResponseEntity<Map<String, Object>> getMemberLoanSummary(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberLoanSummary(id));
    }
}
