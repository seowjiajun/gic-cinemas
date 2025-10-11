package com.gic.cinemas.backend.service;

import com.gic.cinemas.backend.model.BookedSeatEntity;
import com.gic.cinemas.backend.model.BookingEntity;
import com.gic.cinemas.backend.model.SeatingConfigEntity;
import com.gic.cinemas.backend.repository.BookedSeatRepository;
import com.gic.cinemas.backend.repository.BookingRepository;
import com.gic.cinemas.backend.repository.SeatingConfigRepository;
import com.gic.cinemas.common.dto.SeatDto;
import com.gic.cinemas.common.dto.response.BookingResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

  private static final String BOOKING_CODE_PREFIX = "GIC";
  private static final Duration HOLD_DURATION = Duration.ofMinutes(2);

  private final BookingRepository bookingRepository;
  private final BookedSeatRepository bookedSeatRepository;
  private final SeatingConfigRepository seatingConfigRepository;

  public BookingService(
      BookingRepository bookingRepository,
      BookedSeatRepository bookedSeatRepository,
      SeatingConfigRepository seatingConfigRepository) {
    this.bookingRepository = bookingRepository;
    this.bookedSeatRepository = bookedSeatRepository;
    this.seatingConfigRepository = seatingConfigRepository;
  }

  /**
   * Phase 1: Create a HELD booking and place holds for the requested seats. Seats are persisted
   * immediately to prevent conflicts; the booking expires if not confirmed.
   */
  @Transactional
  public BookingResponse startBooking(
      Long seatingConfigId, String customerName, List<SeatDto> seats) {
    if (seatingConfigId == null) {
      throw new IllegalArgumentException("seatingConfigId is required");
    }
    if (customerName == null || customerName.isBlank()) {
      throw new IllegalArgumentException("customerName is required");
    }
    if (seats == null || seats.isEmpty()) {
      throw new IllegalArgumentException("seats must not be empty");
    }

    SeatingConfigEntity cfg =
        seatingConfigRepository
            .findById(seatingConfigId)
            .orElseThrow(
                () -> new IllegalArgumentException("SeatingConfig not found: " + seatingConfigId));

    validateRequestedSeatsWithinBounds(cfg, seats);
    ensureNoDuplicates(seats);
    ensureSeatsNotTaken(cfg.getId(), seats);

    BookingEntity booking = new BookingEntity(cfg);
    booking.setStatus(BookingEntity.Status.HELD);
    booking.setExpiresAt(Instant.now().plus(HOLD_DURATION));
    booking = bookingRepository.save(booking);

    for (SeatDto s : seats) {
      bookedSeatRepository.save(new BookedSeatEntity(booking, cfg, s.row(), s.col()));
    }

    return new BookingResponse(
        booking.getId(),
        booking.getBookingCode(), // e.g., "GIC0007"
        cfg.getId(),
        customerName,
        seats.size());
  }

  /**
   * While in HELD, replace the set of held seats for this booking (used when user reselects).
   * Extends the hold window.
   */
  @Transactional
  public void updateHeldSeats(String bookingCode, List<SeatDto> newSeats) {
    if (newSeats == null || newSeats.isEmpty()) {
      throw new IllegalArgumentException("seats must not be empty");
    }

    BookingEntity booking = findByCode(bookingCode);
    if (booking.getStatus() != BookingEntity.Status.HELD) {
      throw new IllegalStateException("Booking is not in HELD state");
    }
    if (isExpired(booking)) {
      expireAndThrow(booking, "Hold expired");
    }

    SeatingConfigEntity cfg = booking.getSeatingConfig();
    validateRequestedSeatsWithinBounds(cfg, newSeats);
    ensureNoDuplicates(newSeats);

    // Remove current held seats for this booking, then hold the new set
    bookedSeatRepository.deleteByBookingId(booking.getId());
    ensureSeatsNotTaken(cfg.getId(), newSeats); // re-check conflicts after removal
    for (SeatDto s : newSeats) {
      bookedSeatRepository.save(new BookedSeatEntity(booking, cfg, s.row(), s.col()));
    }

    // Extend hold
    booking.setExpiresAt(Instant.now().plus(HOLD_DURATION));
  }

  /** Phase 2: Confirm a HELD booking (finalize). Fails if expired. */
  @Transactional
  public void confirm(String bookingCode) {
    BookingEntity booking = findByCode(bookingCode);
    if (booking.getStatus() != BookingEntity.Status.HELD) {
      throw new IllegalStateException("Booking is not in HELD state");
    }
    if (isExpired(booking)) {
      expireAndThrow(booking, "Hold expired");
    }
    booking.setStatus(BookingEntity.Status.CONFIRMED);
    booking.setExpiresAt(null);
  }

  /** User cancels the booking before confirmation: release held seats and mark canceled. */
  @Transactional
  public void cancel(String bookingCode) {
    BookingEntity booking = findByCode(bookingCode);
    // Release all seats for this booking
    bookedSeatRepository.deleteByBookingId(booking.getId());
    booking.setStatus(BookingEntity.Status.CANCELED);
    booking.setExpiresAt(null);
  }

  // ------------------------- helpers -------------------------

  private BookingEntity findByCode(String bookingCode) {
    if (bookingCode == null || bookingCode.isBlank()) {
      throw new IllegalArgumentException("bookingCode is required");
    }
    Long id = parsePkFromCode(bookingCode);
    return bookingRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingCode));
  }

  private boolean isExpired(BookingEntity booking) {
    Instant exp = booking.getExpiresAt();
    return exp != null && exp.isBefore(Instant.now());
  }

  private void expireAndThrow(BookingEntity booking, String message) {
    // release seats and cancel
    bookedSeatRepository.deleteByBookingId(booking.getId());
    booking.setStatus(BookingEntity.Status.CANCELED);
    booking.setExpiresAt(null);
    throw new IllegalStateException(message);
  }

  private void validateRequestedSeatsWithinBounds(SeatingConfigEntity cfg, List<SeatDto> seats) {
    for (SeatDto s : seats) {
      if (s.row() < 0
          || s.row() >= cfg.getRows()
          || s.col() < 0
          || s.col() >= cfg.getSeatsPerRow()) {
        throw new IllegalArgumentException("Seat out of bounds: (" + s.row() + "," + s.col() + ")");
      }
    }
  }

  private void ensureNoDuplicates(List<SeatDto> seats) {
    Set<Long> seen = new HashSet<>();
    for (SeatDto s : seats) {
      long k = (((long) s.row()) << 32) ^ (s.col() & 0xffffffffL);
      if (!seen.add(k)) {
        throw new IllegalArgumentException(
            "Duplicate seat in request: (" + s.row() + "," + s.col() + ")");
      }
    }
  }

  private void ensureSeatsNotTaken(Long seatingConfigId, List<SeatDto> seats) {
    for (SeatDto s : seats) {
      boolean taken =
          bookedSeatRepository.existsBySeatingConfig_IdAndRowNoAndColNo(
              seatingConfigId, s.row(), s.col());
      if (taken) {
        throw new IllegalStateException("Seat already taken: (" + s.row() + "," + s.col() + ")");
      }
    }
  }

  /** Booking code format is "GIC%04d" derived from numeric PK. Example: "GIC0007" -> 7 */
  private Long parsePkFromCode(String code) {
    if (!code.startsWith(BOOKING_CODE_PREFIX)) {
      throw new IllegalArgumentException("Invalid booking code: " + code);
    }
    String numeric = code.substring(BOOKING_CODE_PREFIX.length());
    try {
      return Long.parseLong(numeric);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid booking code: " + code);
    }
  }
}
