package com.gic.cinemas.backend;

import static org.assertj.core.api.Assertions.assertThat;

import com.gic.cinemas.backend.service.SeatAllocator;
import com.gic.cinemas.common.dto.response.SeatDto;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SeatAllocatorTest {
  private final SeatAllocator seatAllocator = new SeatAllocator();

  static Stream<Arguments> provideSeatAllocations() {
    return Stream.of(
        // even seats, even tickets
        Arguments.of(1, 8, 4, seats("A3", "A4", "A5", "A6")),
        // odd seats, odd tickets
        Arguments.of(1, 7, 3, seats("A3", "A4", "A5")),
        // even seats, odd tickets
        Arguments.of(1, 8, 3, seats("A3", "A4", "A5")),
        // odd seats, even tickets
        Arguments.of(1, 7, 4, seats("A2", "A3", "A4", "A5")));
  }

  @ParameterizedTest
  @MethodSource("provideSeatAllocations")
  @DisplayName("allocateDefault allocates center-outward correctly for various layouts")
  void testAllocateDefaultOnEmptyRow(
      int rowCount, int seatsPerRow, int tickets, List<SeatDto> expectedReservedSeats) {
    List<SeatDto> reservedSeats =
        seatAllocator.allocateDefault(rowCount, seatsPerRow, tickets, List.of());
    assertThat(reservedSeats).containsExactlyInAnyOrderElementsOf(expectedReservedSeats);
  }

  static Stream<Arguments> provideSeatAllocationsWithOverflow() {
    return Stream.of(
        // even seats, even overflow
        Arguments.of(4, 4, 6, seats("A1", "A2", "A3", "A4"), seats("B2", "B3")),
        // even seats, odd overflow
        Arguments.of(4, 4, 7, seats("A1", "A2", "A3", "A4"), seats("B1", "B2", "B3")),
        // odd seats, even overflow
        Arguments.of(4, 5, 7, seats("A1", "A2", "A3", "A4", "A5"), seats("B2", "B3")),
        // odd seats, odd overflow
        Arguments.of(4, 5, 8, seats("A1", "A2", "A3", "A4", "A5"), seats("B2", "B3", "B4")));
  }

  @ParameterizedTest
  @MethodSource("provideSeatAllocationsWithOverflow")
  @DisplayName("allocateDefault should overflow to next row when first row is full")
  void testAllocateDefaultOverflowsToNextRow(
      int rowCount,
      int seatsPerRow,
      int tickets,
      List<SeatDto> expectedReservedSeatsRowA,
      List<SeatDto> expectedReservedSeatsRowB) {
    List<SeatDto> reservedSeats =
        seatAllocator.allocateDefault(rowCount, seatsPerRow, tickets, List.of());
    assertThat(reservedSeats)
        .filteredOn(r -> r.rowLabel().equals("A"))
        .containsExactlyInAnyOrderElementsOf(expectedReservedSeatsRowA);
    assertThat(reservedSeats)
        .filteredOn(r -> r.rowLabel().equals("B"))
        .containsExactlyInAnyOrderElementsOf(expectedReservedSeatsRowB);
  }

  static Stream<Arguments> provideSeatAllocationsCenterTaken() {
    return Stream.of(
        // even seats, even tickets, middle taken
        Arguments.of(4, 4, 2, seats("A2", "A3"), seats("A1", "A4")),
        // even seats, odd tickets, middle left taken
        Arguments.of(4, 4, 3, seats("A2"), seats("A1", "A3", "A4")),
        // odd seats, odd tickets, middle right taken
        Arguments.of(4, 4, 3, seats("A3"), seats("A1", "A2", "A4")),
        // even seats, even tickets, middle taken
        Arguments.of(4, 5, 4, seats("A3"), seats("A1", "A2", "A4", "A5")),
        // even seats, odd tickets, middle taken
        Arguments.of(4, 5, 3, seats("A3"), seats("A1", "A2", "A4")));
  }

  @ParameterizedTest
  @MethodSource("provideSeatAllocationsCenterTaken")
  @DisplayName("allocateDefault should skip taken center seats and continue outward")
  void testAllocateDefaultSkipsTakenCenter(
      int rowCount,
      int seatsPerRow,
      int tickets,
      List<SeatDto> bookedSeats,
      List<SeatDto> expectedReservedSeats) {
    List<SeatDto> reservedSeats =
        seatAllocator.allocateDefault(rowCount, seatsPerRow, tickets, bookedSeats);
    assertThat(reservedSeats).containsExactlyInAnyOrderElementsOf(expectedReservedSeats);
  }

  static Stream<Arguments> provideSeatAllocationsAlternateSeatsTaken() {
    return Stream.of(
        // even seats, even tickets, middle taken
        Arguments.of(4, 4, 2, seats("A1", "A3"), seats("A2", "A4")),
        // even seats, odd tickets, middle left taken
        Arguments.of(4, 4, 2, seats("A2", "A4"), seats("A1", "A3")),
        // even seats, even tickets, middle taken
        Arguments.of(4, 5, 2, seats("A1", "A3", "A5"), seats("A2", "A4")),
        // even seats, odd tickets, middle taken
        Arguments.of(4, 5, 3, seats("A2", "A4"), seats("A1", "A3", "A5")));
  }

  @ParameterizedTest
  @MethodSource("provideSeatAllocationsAlternateSeatsTaken")
  @DisplayName("allocateDefault should skip taken alternate seats and continue outward")
  void testAllocateDefaultSkipsTakenAlternateSeats(
      int rowCount,
      int seatsPerRow,
      int tickets,
      List<SeatDto> bookedSeats,
      List<SeatDto> expectedReservedSeats) {
    List<SeatDto> reservedSeats =
        seatAllocator.allocateDefault(rowCount, seatsPerRow, tickets, bookedSeats);
    assertThat(reservedSeats).containsExactlyInAnyOrderElementsOf(expectedReservedSeats);
  }

  static Stream<Arguments> provideAllocateFromStartOnEmptyRowsParams() {
    return Stream.of(
        // no overflow
        Arguments.of(4, 4, 4, new SeatDto("A", 1), seats("A1", "A2", "A3", "A4")),
        Arguments.of(4, 4, 2, new SeatDto("A", 1), seats("A1", "A2")),
        Arguments.of(4, 4, 3, new SeatDto("A", 2), seats("A2", "A3", "A4")),
        // with overflow
        Arguments.of(4, 4, 4, new SeatDto("A", 3), seats("A3", "A4", "B2", "B3")),
        Arguments.of(4, 4, 3, new SeatDto("A", 3), seats("A3", "A4", "B2")),
        Arguments.of(4, 5, 5, new SeatDto("A", 3), seats("A3", "A4", "A5", "B2", "B3")),
        Arguments.of(4, 5, 6, new SeatDto("A", 3), seats("A3", "A4", "A5", "B2", "B3", "B4")));
  }

  @ParameterizedTest
  @MethodSource("provideAllocateFromStartOnEmptyRowsParams")
  @DisplayName("allocateFromStart should fill rightward until end of row")
  void testAllocateFromStartOnEmptyRows(
      int rowCount,
      int seatsPerRow,
      int tickets,
      SeatDto startSeat,
      List<SeatDto> expectedReservedSeats) {
    List<SeatDto> reservedSeats =
        seatAllocator.allocateFromStartSeat(rowCount, seatsPerRow, tickets, startSeat, List.of());
    assertThat(reservedSeats).containsExactlyInAnyOrderElementsOf(expectedReservedSeats);
  }

  static Stream<Arguments> provideStartSeatSkipsTaken() {
    return Stream.of(
        Arguments.of(4, 4, 3, new SeatDto("A", 1), seats("A2"), seats("A1", "A3", "A4")),
        Arguments.of(4, 4, 2, new SeatDto("A", 1), seats("A2"), seats("A1", "A3")),
        Arguments.of(4, 4, 2, new SeatDto("A", 2), seats("A3"), seats("A2", "A4")));
  }

  @ParameterizedTest
  @MethodSource("provideStartSeatSkipsTaken")
  @DisplayName("allocateDefault should skip taken alternate seats and continue outward")
  void testAllocateFromStartSkipsTakenSeats(
      int rowCount,
      int seatsPerRow,
      int tickets,
      SeatDto startSeat,
      List<SeatDto> bookedSeats,
      List<SeatDto> expectedReservedSeats) {
    List<SeatDto> reservedSeats =
        seatAllocator.allocateFromStartSeat(rowCount, seatsPerRow, tickets, startSeat, bookedSeats);
    assertThat(reservedSeats).containsExactlyInAnyOrderElementsOf(expectedReservedSeats);
  }

  private static List<SeatDto> seats(String... seatCodes) {
    return Arrays.stream(seatCodes)
        .map(
            code ->
                new SeatDto(
                    code.substring(0, 1), // row label (A, B, etc.)
                    Integer.parseInt(code.substring(1)) // seat number (3, 4, etc.)
                    ))
        .toList();
  }
}
