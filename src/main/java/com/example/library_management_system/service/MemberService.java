package com.example.library_management_system.service;

import com.example.library_management_system.dto.MemberRequest;
import com.example.library_management_system.dto.MemberResponse;
import com.example.library_management_system.entity.Loan;
import com.example.library_management_system.entity.Member;
import com.example.library_management_system.exception.BusinessException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse registerMember(MemberRequest request) {
        memberRepository.findByEmail(request.getEmail())
                .ifPresent(m -> { throw new BusinessException("Email already registered: " + request.getEmail()); });

        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        return toResponse(memberRepository.save(member));
    }

    public List<MemberResponse> getAllMembers() {
        // Java Stream: map entities to response DTOs
        return memberRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MemberResponse getMemberById(Long id) {
        return memberRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }

    public MemberResponse updateMember(Long id, MemberRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        member.setName(request.getName());
        member.setPhone(request.getPhone());
        return toResponse(memberRepository.save(member));
    }

    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        // Java Stream: check if member has active loans before deletion
        boolean hasActiveLoans = member.getLoans() != null && member.getLoans()
                .stream()
                .anyMatch(l -> l.getStatus() == Loan.LoanStatus.ACTIVE);

        if (hasActiveLoans) {
            throw new BusinessException("Cannot delete member with active loans");
        }
        memberRepository.deleteById(id);
    }

    public List<MemberResponse> getMembersWithActiveLoans() {
        return memberRepository.findMembersWithActiveLoans()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getMemberLoanSummary(Long memberId) {
        List<Object[]> results = memberRepository.getMemberLoanSummary(memberId);
        if (results == null || results.isEmpty()) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }
        Object[] row = results.get(0);
        // Java Stream: transform raw SQL result into a typed Map
        Map<String, Object> summary = new HashMap<>();
        summary.put("memberId", ((Number) row[0]).longValue());
        summary.put("name", row[1]);
        summary.put("email", row[2]);
        summary.put("totalLoans", ((Number) row[3]).longValue());
        summary.put("activeLoans", ((Number) row[4]).longValue());
        summary.put("overdueLoans", ((Number) row[5]).longValue());
        return summary;
    }

    private MemberResponse toResponse(Member member) {
        long activeLoans = (member.getLoans() == null) ? 0L :
                member.getLoans().stream()
                        .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE)
                        .count();
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .activeLoans(activeLoans)
                .build();
    }
}
