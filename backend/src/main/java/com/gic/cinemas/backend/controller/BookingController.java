package com.gic.cinemas.backend.controller;

import com.gic.cinemas.backend.service.BookingService;
import com.gic.cinemas.common.dto.request.ChangeSeatsRequest;
import com.gic.cinemas.common.dto.request.ReserveSeatsRequest;
import com.gic.cinemas.common.dto.response.BookingConfirmedResponse;
import com.gic.cinemas.common.dto.response.CheckBookingResponse;
import com.gic.cinemas.common.dto.response.ReserveSeatsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

  private final BookingService service;

  public BookingController(BookingService service) {
    this.service = service;
  }

  @PostMapping("/reserve")
  public ResponseEntity<ReserveSeatsResponse> reserveDefault(
      @RequestBody ReserveSeatsRequest request) {
    ReserveSeatsResponse response =
        service.reserveSeats(
            request.movieTitle(),
            request.rowCount(),
            request.seatsPerRow(),
            request.numberOfTickets());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/change-booking")
  public ResponseEntity<ReserveSeatsResponse> changeBooking(
      @RequestBody ChangeSeatsRequest request) {
    ReserveSeatsResponse response = service.changeBooking(request.bookingId(), request.startSeat());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/confirm/{bookingId}")
  public ResponseEntity<BookingConfirmedResponse> confirmBooking(@PathVariable String bookingId) {
    BookingConfirmedResponse response = service.confirmBooking(bookingId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/check/{bookingId}")
  public ResponseEntity<CheckBookingResponse> checkBookings(@PathVariable String bookingId) {
    CheckBookingResponse response = service.checkBookings(bookingId);
    return ResponseEntity.ok(response);
  }
}
