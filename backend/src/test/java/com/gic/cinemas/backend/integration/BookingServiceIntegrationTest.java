package com.gic.cinemas.backend.integration;

import static org.assertj.core.api.Assertions.*;

import com.gic.cinemas.backend.SeatMapBuilder;
import com.gic.cinemas.backend.exception.BookingNotFoundException;
import com.gic.cinemas.backend.exception.NoAvailableSeatsException;
import com.gic.cinemas.backend.model.BookingEntity;
import com.gic.cinemas.backend.repository.BookedSeatRepository;
import com.gic.cinemas.backend.repository.BookingRepository;
import com.gic.cinemas.backend.repository.SeatingConfigRepository;
import com.gic.cinemas.backend.service.BookingService;
import com.gic.cinemas.backend.service.SeatAllocator;
import com.gic.cinemas.backend.service.SeatingConfigHelper;
import com.gic.cinemas.backend.validation.BookingValidator;
import com.gic.cinemas.backend.validation.SeatingConfigValidator;
import com.gic.cinemas.common.dto.BookingStatus;
import com.gic.cinemas.common.dto.SeatDto;
import com.gic.cinemas.common.dto.response.BookingConfirmedResponse;
import com.gic.cinemas.common.dto.response.CheckBookingResponse;
import com.gic.cinemas.common.dto.response.ReservedSeatsResponse;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
  BookingService.class,
  SeatAllocator.class,
  SeatMapBuilder.class,
  SeatingConfigValidator.class,
  BookingValidator.class,
  SeatingConfigHelper.class
})
class BookingServiceIntegrationTest {

  @Autowired private BookingService bookingService;
  @Autowired private BookingRepository bookingRepository;
  @Autowired private BookedSeatRepository bookedSeatRepository;
  @Autowired private SeatingConfigRepository seatingConfigRepository;

  private static Stream<Arguments> provideBookingScenarios() {
    return Stream.of(
        Arguments.of("Inception", 3, 10, 3),
        Arguments.of("Interstellar", 5, 5, 2),
        Arguments.of("Interstellar", 5, 5, 3),
        Arguments.of("Tenet", 2, 8, 4),
        Arguments.of("Tenet", 2, 8, 8));
  }

  @ParameterizedTest(name = "[{index}] {0} ({1}x{2}, {3} tickets) → preview only, no persistence")
  @MethodSource("provideBookingScenarios")
  @DisplayName("reserveSeats allocates and persists held seats")
  @Transactional
  void testReserveSeatsOnEmptyRow(
      String movieTitle, int rowCount, int seatsPerRow, int numberOfTickets) {
    ReservedSeatsResponse response =
        bookingService.reserveSeats(movieTitle, rowCount, seatsPerRow, numberOfTickets);

    assertThat(response.bookingId()).isEqualTo("GIC0001");
    assertThat(response.reservedSeats()).hasSize(numberOfTickets);
    assertThat(response.takenSeats()).isNotNull();

    Long availableSeatsCount =
        bookedSeatRepository
            .countAvailableSeatsByConfigId(
                seatingConfigRepository
                    .findIdByTitleAndLayout(movieTitle, rowCount, seatsPerRow)
                    .orElseThrow())
            .orElseThrow();

    assertThat(availableSeatsCount).isEqualTo((long) rowCount * seatsPerRow - numberOfTickets);
    assertThat(bookingRepository.findAll()).hasSize(1);
  }

  private static Stream<Arguments> provideNotEnoughSeatsScenarios() {
    return Stream.of(
        Arguments.of("Inception", 3, 10, 31),
        Arguments.of("Interstellar", 5, 5, 26),
        Arguments.of("Tenet", 2, 8, 17));
  }

  @ParameterizedTest(name = "[{index}] {0} ({1}x{2}, {3} tickets) → preview only, no persistence")
  @MethodSource("provideNotEnoughSeatsScenarios")
  @DisplayName("startBooking throws NoAvailableSeatsException when none left")
  void startBookingThrowsWhenNoAvailableSeats(
      String movieTitle, int rowCount, int seatsPerRow, int numberOfTickets) {
    assertThatThrownBy(
            () -> bookingService.reserveSeats(movieTitle, rowCount, seatsPerRow, numberOfTickets))
        .isInstanceOf(NoAvailableSeatsException.class)
        .hasMessage("not enough seats");
  }

  @Test
  void checkBookingThrowsBookingNotFound() {
    String bookingId = "GIC0001";
    assertThatThrownBy(() -> bookingService.checkBookings(bookingId))
        .isInstanceOf(BookingNotFoundException.class)
        .hasMessage("Booking not found for bookingId: " + bookingId);
  }

  @Test
  @Transactional
  @DisplayName("confirmBooking marks a pending booking as CONFIRMED")
  void confirmBookingMarksPendingAsConfirmed() {
    // Arrange: create a pending booking with 3 seats held
    String movieTitle = "Inception";
    int rowCount = 3, seatsPerRow = 10, numberOfTickets = 3;

    ReservedSeatsResponse reserveResponse =
        bookingService.reserveSeats(movieTitle, rowCount, seatsPerRow, numberOfTickets);
    BookingEntity before =
        bookingRepository.findByBookingId(reserveResponse.bookingId()).orElseThrow();
    assertThat(before.getStatus()).isEqualTo(BookingStatus.PENDING);

    BookingConfirmedResponse confirmResponse =
        bookingService.confirmBooking(reserveResponse.bookingId());
    BookingEntity after =
        bookingRepository.findByBookingId(confirmResponse.bookingId()).orElseThrow();
    assertThat(after.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

    long cfgId =
        seatingConfigRepository
            .findIdByTitleAndLayout(movieTitle, rowCount, seatsPerRow)
            .orElseThrow();
    long available = bookedSeatRepository.countAvailableSeatsByConfigId(cfgId).orElseThrow();
    assertThat(available).isEqualTo((long) rowCount * seatsPerRow - numberOfTickets);
  }

  @Transactional
  @DisplayName("changeBooking replaces held seats for an existing pending booking and extends hold")
  @Test
  void changeBookingReplacesSeatsAndExtendsHold() {
    // --- Arrange: initial booking ---
    String movie = "Inception";
    int rows = 3, perRow = 5, tickets = 4;

    // Check booking is pending
    ReservedSeatsResponse reservedSeats = bookingService.reserveSeats(movie, rows, perRow, tickets);
    String bookingId = reservedSeats.bookingId();
    BookingEntity booking = bookingRepository.findByBookingId(bookingId).orElseThrow();
    LocalDateTime oldReservedUntil = booking.getReservedUntil();
    assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

    // Check available seats have reduced
    Long seatingConfigId =
        seatingConfigRepository.findIdByTitleAndLayout(movie, rows, perRow).orElseThrow();
    long availableSeatsCount =
        bookedSeatRepository.countAvailableSeatsByConfigId(seatingConfigId).orElseThrow();
    assertThat(availableSeatsCount).isEqualTo((long) rows * perRow - tickets);

    SeatDto startSeat = new SeatDto("B", 3);
    ReservedSeatsResponse changed = bookingService.changeBooking(bookingId, startSeat);

    // --- Assert: same booking, seats replaced, count unchanged, hold extended ---
    assertThat(changed.bookingId()).isEqualTo(bookingId);
    assertThat(changed.reservedSeats()).hasSize(tickets);

    // Expect B3, B4, B5 (till row end), then overflow 1 seat to next row toward the front at mid
    // (A3)
    assertThat(changed.reservedSeats())
        .contains(
            new SeatDto("B", 3), new SeatDto("B", 4), new SeatDto("B", 5), new SeatDto("C", 3));

    // Booking still pending, and reservedUntil rolled forward
    BookingEntity after = bookingRepository.findByBookingId(bookingId).orElseThrow();
    assertThat(after.getStatus()).isEqualTo(BookingStatus.PENDING);
    assertThat(after.getReservedUntil()).isAfter(oldReservedUntil);

    // Availability stays the same (same number of seats held, but seats were replaced)
    long availableAfter =
        bookedSeatRepository.countAvailableSeatsByConfigId(seatingConfigId).orElseThrow();
    assertThat(availableAfter).isEqualTo((long) rows * perRow - tickets);

    // Ensure we didn't create a new booking row
    assertThat(bookingRepository.findAll()).hasSize(1);
  }

  @Test
  @DisplayName("checkBookings returns own reserved seats and others' booked seats correctly")
  void testCheckBookingsReturnsCorrectSeatPartitions() {
    // Arrange
    String movieTitle = "Inception";
    int rowCount = 3, seatsPerRow = 5, tickets = 4;

    ReservedSeatsResponse reservedSeats =
        bookingService.reserveSeats(movieTitle, rowCount, seatsPerRow, tickets);
    bookingService.confirmBooking(reservedSeats.bookingId());
    CheckBookingResponse bookings = bookingService.checkBookings(reservedSeats.bookingId());

    assertThat(bookings.bookedSeats())
        .containsExactlyInAnyOrderElementsOf(reservedSeats.reservedSeats());
  }
}
