package com.example.library_management_system.service;

import com.example.library_management_system.dto.LoanRequest;
import com.example.library_management_system.dto.LoanResponse;
import com.example.library_management_system.entity.Book;
import com.example.library_management_system.entity.Loan;
import com.example.library_management_system.entity.Member;
import com.example.library_management_system.exception.BusinessException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.repository.BookRepository;
import com.example.library_management_system.repository.LoanRepository;
import com.example.library_management_system.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public LoanResponse borrowBook(LoanRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.getBookId()));

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));

        if (book.getStock() <= 0) {
            throw new BusinessException("Book '" + book.getTitle() + "' is out of stock");
        }

        // Check member doesn't already have this book on loan
        boolean alreadyBorrowed = loanRepository.findActiveLoansByMember(member.getId())
                .stream()
                .anyMatch(l -> l.getBook().getId().equals(book.getId()));

        if (alreadyBorrowed) {
            throw new BusinessException("Member already has an active loan for this book");
        }

        int days = (request.getLoanDays() != null && request.getLoanDays() > 0) ? request.getLoanDays() : 14;

        Loan loan = Loan.builder()
                .book(book)
                .member(member)
                .loanDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(days))
                .status(Loan.LoanStatus.ACTIVE)
                .build();

        // Reduce stock
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);

        return toResponse(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        if (loan.getStatus() == Loan.LoanStatus.RETURNED) {
            throw new BusinessException("This book has already been returned");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(Loan.LoanStatus.RETURNED);

        // Restore stock
        Book book = loan.getBook();
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        return toResponse(loanRepository.save(loan));
    }

    @Transactional
    public int syncOverdueLoans() {
        return loanRepository.markOverdueLoans(LocalDate.now());
    }

    public List<LoanResponse> getLoansByMember(Long memberId) {
        return loanRepository.findByMemberId(memberId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getLoansDueSoon(int days) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        // Java Stream: map raw SQL result rows into typed Maps
        return loanRepository.findLoansDueSoon(today, deadline)
                .stream()
                .map(row -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("loanId", ((Number) row[0]).longValue());
                    m.put("bookTitle", row[10]);
                    m.put("memberName", row[11]);
                    m.put("dueDate", row[4].toString());
                    return m;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getLoanActivityByMonth() {
        // Java Stream: convert month-activity rows into a list of maps
        return loanRepository.getLoanActivityByMonth()
                .stream()
                .map(row -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("month", row[0]);
                    m.put("totalLoans", ((Number) row[1]).longValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // Java Stream: statistics computed in-memory from loan list
    public Map<String, Long> getLoanStatusSummary() {
        return loanRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        l -> l.getStatus().name(),
                        Collectors.counting()
                ));
    }

    private LoanResponse toResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .bookTitle(loan.getBook().getTitle())
                .memberName(loan.getMember().getName())
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .returnDate(loan.getReturnDate())
                .status(loan.getStatus())
                .build();
    }
}
