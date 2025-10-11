package com.gic.cinemas.backend.controller;

import com.gic.cinemas.backend.service.BookingService;
import com.gic.cinemas.common.dto.request.BookingRequest;
import com.gic.cinemas.common.dto.response.BookingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

  private final BookingService service;

  public BookingController(BookingService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
    BookingResponse response = service.createBooking(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BookingResponse> getBooking(@PathVariable String id) {
    BookingResponse response = service.getBooking(id);
    return ResponseEntity.ok(response);
  }
}
