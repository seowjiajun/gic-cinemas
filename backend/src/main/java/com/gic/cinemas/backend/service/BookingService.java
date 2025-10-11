package com.gic.cinemas.backend.service;

import com.gic.cinemas.backend.model.Booking;
import com.gic.cinemas.common.*;
import com.gic.cinemas.common.dto.SeatDto;
import com.gic.cinemas.common.dto.request.BookingRequest;
import com.gic.cinemas.common.dto.request.SeatingConfigRequest;
import com.gic.cinemas.common.dto.response.BookingResponse;
import com.gic.cinemas.common.dto.response.SeatingConfigResponse;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

  // ===== constants =====
  private static final String BOOKING_ID_PREFIX = "GIC";
  private static final String BOOKING_ID_FORMAT = "%04d";
  private static final String BOOKING_NOT_FOUND_MSG = "Booking not found: ";

  // ===== state =====
  private final Map<String, Booking> bookings = new LinkedHashMap<>();
  private final AtomicInteger counter = new AtomicInteger(1);

  // Seating config + occupancy
  private SeatingConfigResponse config; // null until configured
  private boolean[][] taken; // taken[row][col] true if booked

  // ----- configuration -----
  public SeatingConfigResponse setConfig(SeatingConfigRequest req) {
    this.config = new SeatingConfigResponse(req.title(), req.rows(), req.cols());
    this.taken = new boolean[req.rows()][req.cols()];
    this.bookings.clear();
    this.counter.set(1);
    return this.config;
  }

  public Optional<SeatingConfigResponse> getConfig() {
    return Optional.ofNullable(config);
  }

  public int remainingSeats() {
    if (taken == null) return 0;
    int total = taken.length * taken[0].length;
    int used = 0;
    for (boolean[] row : taken) for (boolean c : row) if (c) used++;
    return total - used;
  }

  // ----- booking -----
  public BookingResponse createBooking(BookingRequest request) {
    ensureConfigured();

    int want = request.count();
    int remaining = remainingSeats();
    if (want > remaining) throw new IllegalArgumentException("Only " + remaining + " seats left");

    List<SeatDto> seats = allocateSeats(want);
    String id = BOOKING_ID_PREFIX + String.format(BOOKING_ID_FORMAT, counter.getAndIncrement());

    Booking booking = new Booking(id, request.movie(), seats);
    bookings.put(id, booking);

    return new BookingResponse(id, request.movie(), seats);
  }

  public BookingResponse getBooking(String id) {
    Booking booking = bookings.get(id);
    if (booking == null) throw new NoSuchElementException(BOOKING_NOT_FOUND_MSG + id);
    return new BookingResponse(booking.id(), booking.movie(), booking.seats());
  }

  // ----- allocation (simple: scan from A01 → last row/col) -----
  private List<SeatDto> allocateSeats(int count) {
    ensureConfigured();
    int rows = config.rows(), cols = config.cols();
    List<SeatDto> out = new ArrayList<>(count);

    // Row A is index 0 -> seat code "A01", then "A02"..., then "B01"... etc.
    outer:
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        if (!taken[r][c]) {
          taken[r][c] = true;
          out.add(new SeatDto(code(r, c)));
          if (out.size() == count) break outer;
        }
      }
    }
    return out;
  }

  private String code(int rowIdx, int colIdx) {
    char rowChar = (char) ('A' + rowIdx);
    return rowChar + String.format("%02d", colIdx + 1);
  }

  private void ensureConfigured() {
    if (config == null || taken == null) {
      throw new IllegalStateException("Seating configuration not set");
    }
  }
}
