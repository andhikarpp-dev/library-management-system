package com.example.library_management_system.controller;

import com.example.library_management_system.dto.BookRequest;
import com.example.library_management_system.dto.BookResponse;
import com.example.library_management_system.dto.GenreStatResponse;
import com.example.library_management_system.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponse> addBook(@RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(request));
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> searchBooks(@RequestParam String keyword) {
        return ResponseEntity.ok(bookService.searchBooks(keyword));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<BookResponse>> getLowStock(@RequestParam(defaultValue = "3") int threshold) {
        return ResponseEntity.ok(bookService.getLowStockBooks(threshold));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/genre-stats")
    public ResponseEntity<List<GenreStatResponse>> getGenreStats() {
        return ResponseEntity.ok(bookService.getGenreStatistics());
    }

    @GetMapping("/grouped-by-genre")
    public ResponseEntity<Map<String, Long>> getBooksGroupedByGenre() {
        return ResponseEntity.ok(bookService.getBooksByGenreGrouped());
    }

    @GetMapping("/top-borrowed")
    public ResponseEntity<List<BookResponse>> getTopBorrowed(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(bookService.getTopBorrowedBooks(limit));
    }
}
