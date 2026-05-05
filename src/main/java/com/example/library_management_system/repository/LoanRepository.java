package com.example.library_management_system.repository;

import com.example.library_management_system.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByMemberId(Long memberId);

    List<Loan> findByBookId(Long bookId);

    // Native SQL: find active loans by member
    @Query(value = "SELECT * FROM loans WHERE member_id = :memberId AND status = 'ACTIVE'", nativeQuery = true)
    List<Loan> findActiveLoansByMember(@Param("memberId") Long memberId);

    // Native SQL: bulk update overdue loans
    @Modifying
    @Query(value = "UPDATE loans SET status = 'OVERDUE' WHERE due_date < :today AND status = 'ACTIVE'", nativeQuery = true)
    int markOverdueLoans(@Param("today") LocalDate today);

    // Native SQL: loans due within N days
    @Query(value = """
            SELECT l.*, b.title AS book_title, m.name AS member_name
            FROM loans l
            JOIN books b ON b.id = l.book_id
            JOIN members m ON m.id = l.member_id
            WHERE l.status = 'ACTIVE'
              AND l.due_date BETWEEN :today AND :deadline
            ORDER BY l.due_date ASC
            """, nativeQuery = true)
    List<Object[]> findLoansDueSoon(@Param("today") LocalDate today, @Param("deadline") LocalDate deadline);

    // Native SQL: loan activity per month
    @Query(value = """
            SELECT TO_CHAR(loan_date, 'YYYY-MM') AS month,
                   COUNT(*) AS total_loans
            FROM loans
            GROUP BY TO_CHAR(loan_date, 'YYYY-MM')
            ORDER BY month DESC
            """, nativeQuery = true)
    List<Object[]> getLoanActivityByMonth();
}
