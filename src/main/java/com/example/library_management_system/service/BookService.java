package com.example.library_management_system.service;

import com.example.library_management_system.dto.BookRequest;
import com.example.library_management_system.dto.BookResponse;
import com.example.library_management_system.dto.GenreStatResponse;
import com.example.library_management_system.entity.Book;
import com.example.library_management_system.exception.BusinessException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public BookResponse addBook(BookRequest request) {
        bookRepository.findByIsbn(request.getIsbn())
                .ifPresent(b -> { throw new BusinessException("Book with ISBN " + request.getIsbn() + " already exists"); });

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .genre(request.getGenre())
                .stock(request.getStock())
                .build();

        return toResponse(bookRepository.save(book));
    }

    public List<BookResponse> getAllBooks() {
        // Java Stream: convert list of entities to DTOs
        return bookRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BookResponse getBookById(Long id) {
        return bookRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    public List<BookResponse> searchBooks(String keyword) {
        // Java Stream: filter & map results
        return bookRepository.searchByTitleOrAuthor(keyword)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getLowStockBooks(int threshold) {
        return bookRepository.findBooksWithLowStock(threshold)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setGenre(request.getGenre());
        book.setStock(request.getStock());
        return toResponse(bookRepository.save(book));
    }

    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        book.setIsDeleted(true);
        bookRepository.save(book);
    }

    public List<GenreStatResponse> getGenreStatistics() {
        List<Object[]> rows = bookRepository.findGenreStatistics();
        // Java Stream: transform raw Object[] rows into typed DTOs
        return rows.stream()
                .map(row -> GenreStatResponse.builder()
                        .genre((String) row[0])
                        .totalBooks(((Number) row[1]).longValue())
                        .totalLoaned(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    public Map<String, Long> getBooksByGenreGrouped() {
        // Java Stream: groupingBy + counting
        return bookRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(Book::getGenre, Collectors.counting()));
    }

    public List<BookResponse> getTopBorrowedBooks(int limit) {
        return bookRepository.findTopBorrowedBooks(limit)
                .stream()
                .map(row -> BookResponse.builder()
                        .id(((Number) row[0]).longValue())
                        .title((String) row[1])
                        .author((String) row[2])
                        .genre((String) row[3])
                        .totalLoaned(((Number) row[4]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    // Helper: map entity -> DTO
    private BookResponse toResponse(Book book) {
        int loaned = (book.getLoans() == null) ? 0 :
                (int) book.getLoans().stream()
                        .filter(l -> l.getStatus() == com.example.library_management_system.entity.Loan.LoanStatus.ACTIVE)
                        .count();
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .genre(book.getGenre())
                .stock(book.getStock())
                .totalLoaned(loaned)
                .build();
    }
}
