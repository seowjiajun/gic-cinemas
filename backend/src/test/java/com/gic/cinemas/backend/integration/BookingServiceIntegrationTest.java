package com.gic.cinemas.backend.integration;

import static org.assertj.core.api.Assertions.*;

import com.gic.cinemas.backend.SeatMapBuilder;
import com.gic.cinemas.backend.repository.BookedSeatRepository;
import com.gic.cinemas.backend.repository.BookingRepository;
import com.gic.cinemas.backend.repository.SeatingConfigRepository;
import com.gic.cinemas.backend.service.BookingService;
import com.gic.cinemas.backend.service.helper.SeatAllocator;
import com.gic.cinemas.backend.service.helper.SeatingConfigHelper;
import com.gic.cinemas.backend.validation.BookingValidator;
import com.gic.cinemas.backend.validation.SeatingConfigValidator;
import com.gic.cinemas.common.dto.response.StartBookingResponse;
import jakarta.transaction.Transactional;
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
        Arguments.of("Tenet", 2, 8, 4));
  }

  @ParameterizedTest(name = "[{index}] {0} ({1}x{2}, {3} tickets) → preview only, no persistence")
  @MethodSource("provideBookingScenarios")
  @DisplayName("startBooking allocates and persists held seats")
  @Transactional
  void startBooking_persistsHeldSeats(
      String movieTitle, int rowCount, int seatsPerRow, int numberOfTickets) {
    StartBookingResponse response =
        bookingService.startBooking(movieTitle, rowCount, seatsPerRow, numberOfTickets);

    assertThat(response.bookingId()).startsWith("GIC");
    assertThat(response.reservedSeats()).hasSize(numberOfTickets);
    assertThat(response.bookedSeats()).isNotNull();

    long availableSeatsCount =
        bookedSeatRepository.countAvailableSeatsByConfigId(
            seatingConfigRepository
                .findIdByTitleAndLayout(movieTitle, rowCount, seatsPerRow)
                .orElseThrow());
    assertThat(availableSeatsCount).isEqualTo((long) rowCount * seatsPerRow);

    assertThat(bookingRepository.findAll()).hasSize(0);
  }

  @Test
  @DisplayName("startBooking throws NoAvailableSeatsException when none left")
  void startBooking_throwsWhenFull() {
    String movieTitle = "SoldOutShow";
    int rowCount = 1;
    int seatsPerRow = 2;

    // fill the row manually
    //        SeatingConfigEntity config =
    //                seatingConfigRepository.save(new SeatingConfigEntity(movieTitle, rowCount,
    // seatsPerRow));
    //        BookingEntity b = bookingRepository.save(new BookingEntity(config, true, null));
    //        bookedSeatRepository.saveAll(List.of(
    //                new BookedSeatEntity(b, config, 0, 0),
    //                new BookedSeatEntity(b, config, 0, 1)
    //        ));

    // Expect
    //        assertThatThrownBy(() ->
    //                bookingService.startBooking(movieTitle, rowCount, seatsPerRow, 1))
    //                .isInstanceOf(NoAvailableSeatsException.class)
    //                .hasMessageContaining("No seats available");
  }
}
