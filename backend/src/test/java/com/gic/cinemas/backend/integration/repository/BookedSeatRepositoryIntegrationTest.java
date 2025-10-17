package com.gic.cinemas.backend.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gic.cinemas.backend.model.BookedSeatEntity;
import com.gic.cinemas.backend.model.BookingEntity;
import com.gic.cinemas.backend.model.SeatingConfigEntity;
import com.gic.cinemas.backend.repository.BookedSeatRepository;
import com.gic.cinemas.backend.repository.BookingRepository;
import com.gic.cinemas.backend.repository.SeatingConfigRepository;
import com.gic.cinemas.common.dto.BookingStatus;
import com.gic.cinemas.common.dto.SeatDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
class BookedSeatRepositoryIntegrationTest {

  @Autowired private BookedSeatRepository bookedSeatRepository;
  @Autowired private BookingRepository bookingRepository;
  @Autowired private SeatingConfigRepository seatingConfigRepository;

  private static SeatingConfigEntity cfg(String movieTitle, int rowCount, int seatsPerRow) {
    return new SeatingConfigEntity(movieTitle, rowCount, seatsPerRow);
  }

  private static BookingEntity booking(
      String bookingId, SeatingConfigEntity seatingConfig, BookingStatus status) {
    BookingEntity booking =
        new BookingEntity(bookingId, seatingConfig, LocalDateTime.now(), status);
    return booking;
  }

  private static BookedSeatEntity seat(
      String rowLabel, int seatNumber, BookingEntity booking, SeatingConfigEntity seatingConfig) {
    BookedSeatEntity bookedSeat =
        new BookedSeatEntity(booking, seatingConfig, rowLabel, seatNumber);
    return bookedSeat;
  }

  private void seedBasicData() {
    // Config: 3x4 => 12 seats total
    SeatingConfigEntity sc = seatingConfigRepository.saveAndFlush(cfg("Inception", 3, 4));

    // Bookings: Pending, Confirmed, Cancelled
    BookingEntity bPending =
        bookingRepository.saveAndFlush(booking("GIC0001", sc, BookingStatus.PENDING));
    BookingEntity bConfirmed =
        bookingRepository.saveAndFlush(booking("GIC0002", sc, BookingStatus.CONFIRMED));
    BookingEntity bCancelled =
        bookingRepository.saveAndFlush(booking("GIC0003", sc, BookingStatus.CANCELLED));

    // Seats: 5 pending+confirmed, 2 cancelled (should be excluded from some queries)
    bookedSeatRepository.saveAndFlush(seat("A", 1, bPending, sc));
    bookedSeatRepository.saveAndFlush(seat("A", 2, bPending, sc));
    bookedSeatRepository.saveAndFlush(seat("A", 3, bConfirmed, sc));
    bookedSeatRepository.saveAndFlush(seat("B", 1, bConfirmed, sc));
    bookedSeatRepository.saveAndFlush(seat("B", 2, bConfirmed, sc));
    // Cancelled seats
    bookedSeatRepository.saveAndFlush(seat("C", 1, bCancelled, sc));
    bookedSeatRepository.saveAndFlush(seat("C", 2, bCancelled, sc));
  }

  @Test
  @DisplayName("countAvailableSeatsByConfigId: returns total - all booked (any status)")
  void countAvailableSeats() {
    seedBasicData();

    Long scId = seatingConfigRepository.findAll().get(0).getId();
    // total seats = 12, total booked rows = 7 (includes CANCELLED in this JPQL)
    Optional<Long> available = bookedSeatRepository.countAvailableSeatsByConfigId(scId);
    assertThat(available).isPresent();
    assertThat(available.get()).isEqualTo(12L - 7L);
  }

  @Test
  @DisplayName("countAvailableSeatsByConfigId: empty Optional for non-existent config")
  void countAvailableSeats_nonExistentConfig() {
    Optional<Long> available = bookedSeatRepository.countAvailableSeatsByConfigId(999_999L);
    assertThat(available).isEmpty();
  }

  @Test
  @DisplayName("findBookedSeats: returns only PENDING & CONFIRMED as SeatDto")
  void findBookedSeats_filtersByStatus() {
    seedBasicData();
    Long scId = seatingConfigRepository.findAll().get(0).getId();

    List<SeatDto> seats = bookedSeatRepository.findBookedSeats(scId);
    // Should return 5 from (PENDING + CONFIRMED), exclude 2 CANCELLED
    assertThat(seats).hasSize(5);
    assertThat(seats).extracting(SeatDto::rowLabel).contains("A", "B");
    assertThat(seats).extracting(SeatDto::seatNumber).contains(1, 2, 3);
  }

  @Test
  @DisplayName("findBySeatingConfigIdAndBookingStatusIn: derived projection obeys status filter")
  void findByConfigAndStatusIn() {
    seedBasicData();
    Long scId = seatingConfigRepository.findAll().get(0).getId();

    List<SeatDto> seats =
        bookedSeatRepository.findBySeatingConfigIdAndBookingStatusIn(
            scId, List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

    assertThat(seats).hasSize(5);
  }

  @Test
  @DisplayName("findBySeatingConfigIdAndBookingIdNotAndBookingStatusIn: excludes given booking")
  void findByConfigAndNotBookingAndStatusIn() {
    seedBasicData();
    Long scId = seatingConfigRepository.findAll().get(0).getId();
    // Exclude PENDING booking "GIC0001" which has 2 seats
    BookingEntity pending = bookingRepository.findByBookingId("GIC0001").orElseThrow();

    List<SeatDto> seats =
        bookedSeatRepository.findBySeatingConfigIdAndBookingIdNotAndBookingStatusIn(
            scId, pending.getId(), List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

    // Total PENDING+CONFIRMED = 5; excluding the 2 from GIC0001 leaves 3
    assertThat(seats).hasSize(3);
  }

  @Test
  @DisplayName("countByBooking_BookingId counts seats under public bookingId")
  void countByBookingPublicId() {
    seedBasicData();
    int count =
        bookedSeatRepository.countByBooking_BookingId(
            "GIC0002"); // confirmed: B1, B2 plus A3 => 3 seats
    assertThat(count).isEqualTo(3);
  }

  @Test
  @Transactional
  @DisplayName("deleteAllByBooking_BookingId removes seats for that booking only")
  void deleteByBookingPublicId() {
    seedBasicData();
    // Before
    int before = bookedSeatRepository.countByBooking_BookingId("GIC0001");
    assertThat(before).isEqualTo(2);

    bookedSeatRepository.deleteAllByBooking_BookingId("GIC0001");

    int after = bookedSeatRepository.countByBooking_BookingId("GIC0001");
    assertThat(after).isZero();

    // Other bookings untouched
    assertThat(bookedSeatRepository.countByBooking_BookingId("GIC0002")).isEqualTo(3);
  }

  @Test
  @DisplayName("findAllByBooking_SeatingConfig_Id returns all BookedSeatEntity rows for config")
  void findAllByBookingSeatingConfigId() {
    seedBasicData();
    Long scId = seatingConfigRepository.findAll().get(0).getId();

    List<BookedSeatEntity> all = bookedSeatRepository.findAllByBooking_SeatingConfig_Id(scId);
    // All 7 rows across all statuses
    assertThat(all).hasSize(7);
    assertThat(all).extracting(BookedSeatEntity::getRowLabel).contains("A", "B", "C");
  }
}
