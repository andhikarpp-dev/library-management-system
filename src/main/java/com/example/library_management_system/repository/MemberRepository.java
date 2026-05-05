package com.example.library_management_system.repository;

import com.example.library_management_system.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    // Native SQL: members who currently have active loans
    @Query(value = """
            SELECT DISTINCT m.*
            FROM members m
            INNER JOIN loans l ON l.member_id = m.id
            WHERE l.status = 'ACTIVE'
            """, nativeQuery = true)
    List<Member> findMembersWithActiveLoans();

    // Native SQL: members with overdue books
    @Query(value = """
            SELECT DISTINCT m.*, COUNT(l.id) AS overdue_count
            FROM members m
            INNER JOIN loans l ON l.member_id = m.id
            WHERE l.status = 'OVERDUE'
            GROUP BY m.id, m.name, m.email, m.phone
            ORDER BY overdue_count DESC
            """, nativeQuery = true)
    List<Object[]> findMembersWithOverdueBooks();

    // Native SQL: member loan summary
    @Query(value = """
            SELECT m.id, m.name, m.email,
                   COUNT(l.id) AS total_loans,
                   SUM(CASE WHEN l.status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_loans,
                   SUM(CASE WHEN l.status = 'OVERDUE' THEN 1 ELSE 0 END) AS overdue_loans
            FROM members m
            LEFT JOIN loans l ON l.member_id = m.id
            WHERE m.id = :memberId
            GROUP BY m.id, m.name, m.email
            """, nativeQuery = true)
    List<Object[]> getMemberLoanSummary(@Param("memberId") Long memberId);
}
