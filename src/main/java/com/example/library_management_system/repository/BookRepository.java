package com.example.library_management_system.repository;

import com.example.library_management_system.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    // Native SQL: find books with low stock
    @Query(value = "SELECT * FROM books WHERE stock <= :threshold ORDER BY stock ASC", nativeQuery = true)
    List<Book> findBooksWithLowStock(@Param("threshold") int threshold);

    // Native SQL: search books by title or author (case-insensitive)
    @Query(value = "SELECT * FROM books WHERE LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(author) LIKE LOWER(CONCAT('%', :keyword, '%'))", nativeQuery = true)
    List<Book> searchByTitleOrAuthor(@Param("keyword") String keyword);

    // Native SQL: genre statistics with total loaned count
    @Query(value = """
            SELECT b.genre,
                   COUNT(DISTINCT b.id) AS total_books,
                   COUNT(l.id)          AS total_loaned
            FROM books b
            LEFT JOIN loans l ON l.book_id = b.id
            GROUP BY b.genre
            ORDER BY total_loaned DESC
            """, nativeQuery = true)
    List<Object[]> findGenreStatistics();

    // Native SQL: top N most borrowed books
    @Query(value = """
            SELECT b.id, b.title, b.author, b.genre, COUNT(l.id) AS borrow_count
            FROM books b
            LEFT JOIN loans l ON l.book_id = b.id
            GROUP BY b.id, b.title, b.author, b.genre
            ORDER BY borrow_count DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopBorrowedBooks(@Param("limit") int limit);
}
