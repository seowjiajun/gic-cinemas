package com.gic.cinemas.backend.integration;

import static com.gic.cinemas.backend.assertions.SeatingAvailabilityResponseAssert.assertThatSeatingAvailabilityResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gic.cinemas.backend.exception.SeatingConfigNotFoundException;
import com.gic.cinemas.backend.repository.BookedSeatRepository;
import com.gic.cinemas.backend.repository.SeatingConfigRepository;
import com.gic.cinemas.backend.service.BookingService;
import com.gic.cinemas.backend.service.SeatAllocator;
import com.gic.cinemas.backend.service.SeatingConfigHelper;
import com.gic.cinemas.backend.service.SeatingConfigService;
import com.gic.cinemas.backend.validation.BookingValidator;
import com.gic.cinemas.backend.validation.SeatingConfigValidator;
import com.gic.cinemas.common.dto.response.SeatingAvailabilityResponse;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
  BookingValidator.class,
  SeatAllocator.class,
  SeatingConfigService.class,
  SeatingConfigValidator.class,
  SeatingConfigHelper.class
})
class SeatingConfigServiceIntegrationTest {

  @Autowired private BookingService bookingService;
  @Autowired private SeatingConfigService seatingConfigService;
  @Autowired private SeatingConfigRepository seatingConfigRepository;
  @Autowired private BookedSeatRepository bookedSeatRepository;

  @Nested
  @DisplayName("Happy path")
  class HappyPath {
    static Stream<Arguments> provideConfigs() {
      return Stream.of(
          Arguments.of("Inception", 1, 1, 1L),
          Arguments.of("Deadpool & Wolverine", 13, 25, 325L),
          Arguments.of("The Lord of the Rings", 26, 50, 1300L));
    }

    @DisplayName("findOrCreate creates config and computes availability")
    @ParameterizedTest(name = "[{index}] {0} → {1}x{2} = {3} seats")
    @MethodSource("provideConfigs")
    void testFindOrCreateCreatesNewConfig(
        String movieTitle, int rowCount, int seatsPerRow, long expectedAvailableSeatsCount) {
      SeatingAvailabilityResponse response =
          seatingConfigService.findOrCreateSeatingConfig(movieTitle, rowCount, seatsPerRow);

      assertThatSeatingAvailabilityResponse(response)
          .title(movieTitle)
          .layout(rowCount, seatsPerRow)
          .availableSeats(expectedAvailableSeatsCount);
      assertThat(seatingConfigRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("findOrCreate reuses existing config without duplicate")
    void testFindOrCreateReusesExistingConfig() {
      seatingConfigService.findOrCreateSeatingConfig("Inception", 8, 10);
      seatingConfigService.findOrCreateSeatingConfig("Inception", 8, 10);

      assertThat(seatingConfigRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAvailability")
    void testGetAvailability() {
      String movieTitle = "Inception";
      int rowCount = 8;
      int seatsPerRow = 10;

      seatingConfigService.findOrCreateSeatingConfig(movieTitle, rowCount, seatsPerRow);
      SeatingAvailabilityResponse dto =
          seatingConfigService.getSeatingAvailability(movieTitle, rowCount, seatsPerRow);
      assertThat(dto.availableSeatsCount()).isEqualTo((long) rowCount * seatsPerRow);
    }

    static Stream<Arguments> provideGetSeatingAvailabilityAfterReservationParams() {
      return Stream.of(Arguments.of("Inception", 8, 10, 4), Arguments.of("Inception", 8, 10, 80));
    }

    @ParameterizedTest
    @MethodSource("provideGetSeatingAvailabilityAfterReservationParams")
    void testGetSeatingAvailabilityAfterReservation(
        String movieTitle, int rowCount, int seatsPerRow, int tickets) {
      bookingService.reserveSeats(movieTitle, rowCount, seatsPerRow, tickets);
      SeatingAvailabilityResponse dto =
          seatingConfigService.getSeatingAvailability(movieTitle, rowCount, seatsPerRow);
      assertThat(dto.availableSeatsCount()).isEqualTo((long) rowCount * seatsPerRow - tickets);
    }
  }

  @Nested
  @DisplayName("Error path")
  class ErrorPath {
    static Stream<Arguments> provideInvalidInputs() {
      return Stream.of(
          Arguments.of(" ", 13, 25, "movie title is required"),
          Arguments.of("Inception", 0, 25, "rowCount and seatsPerRow must be > 0"),
          Arguments.of("Inception", 13, 0, "rowCount and seatsPerRow must be > 0"),
          Arguments.of("Inception", 27, 25, "rowCount must not exceed 26"),
          Arguments.of("Inception", 13, 51, "seatsPerRow must not exceed 50"));
    }

    @DisplayName("findOrCreate throws exception on invalid input")
    @ParameterizedTest(name = "[{index}] \"{0}\" → rows={1}, seats={2}")
    @MethodSource("provideInvalidInputs")
    void testFindOrCreateFailsValidation(
        String movieTitle, int rowCount, int seatsPerRow, String exceptionMessage) {
      assertThatThrownBy(
              () ->
                  seatingConfigService.findOrCreateSeatingConfig(movieTitle, rowCount, seatsPerRow))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage(exceptionMessage);
      assertThat(seatingConfigRepository.count()).isEqualTo(0);
    }

    @Test
    void testGetSeatingAvailabilityThrowsSeatingConfigNotFound() {
      assertThatThrownBy(() -> seatingConfigService.getSeatingAvailability("Inception", 8, 10))
          .isInstanceOf(SeatingConfigNotFoundException.class)
          .hasMessageContaining("No seating configuration found for");
    }
  }
}
