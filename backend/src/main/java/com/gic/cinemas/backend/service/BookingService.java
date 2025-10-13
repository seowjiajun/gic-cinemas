package com.gic.cinemas.backend.service;

import com.gic.cinemas.backend.SeatMapBuilder;
import com.gic.cinemas.backend.exception.NoAvailableSeatsException;
import com.gic.cinemas.backend.model.SeatingConfigEntity;
import com.gic.cinemas.backend.repository.BookedSeatRepository;
import com.gic.cinemas.backend.repository.BookingRepository;
import com.gic.cinemas.backend.repository.SeatCoordinate;
import com.gic.cinemas.backend.repository.SeatingConfigRepository;
import com.gic.cinemas.backend.service.helper.SeatAllocator;
import com.gic.cinemas.backend.service.helper.SeatingConfigHelper;
import com.gic.cinemas.backend.validation.BookingValidator;
import com.gic.cinemas.common.dto.response.ConfirmBookingResponse;
import com.gic.cinemas.common.dto.response.SeatDto;
import com.gic.cinemas.common.dto.response.StartBookingResponse;
import java.time.Duration;
import java.util.BitSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

  private static final String BOOKING_CODE_PREFIX = "GIC";
  private static final Duration HOLD_DURATION = Duration.ofMinutes(2);

  private final BookingRepository bookingRepository;
  private final BookedSeatRepository bookedSeatRepository;
  private final SeatingConfigRepository seatingConfigRepository;
  private final BookingValidator bookingValidator;
  private final SeatingConfigHelper seatingConfigHelper;
  private final SeatAllocator seatAllocator;

  public BookingService(
      BookingRepository bookingRepository,
      BookedSeatRepository bookedSeatRepository,
      SeatingConfigRepository seatingConfigRepository,
      BookingValidator bookingValidator,
      SeatingConfigHelper seatingConfigHelper,
      SeatAllocator seatAllocator) {
    this.bookingRepository = bookingRepository;
    this.bookedSeatRepository = bookedSeatRepository;
    this.seatingConfigRepository = seatingConfigRepository;
    this.bookingValidator = bookingValidator;
    this.seatingConfigHelper = seatingConfigHelper;
    this.seatAllocator = seatAllocator;
  }

  /**
   * Phase 1: Create a HELD booking and place holds for the requested seats. Seats are persisted
   * immediately to prevent conflicts; the booking expires if not confirmed.
   */
  @Transactional
  public StartBookingResponse startBooking(
      String movieTitle, int rowCount, int seatsPerRow, int numberOfTickets) {
    bookingValidator.validate(movieTitle, rowCount, seatsPerRow, numberOfTickets);

    SeatingConfigEntity seatingConfigEntity =
        seatingConfigHelper.findOrCreateConfig(movieTitle, rowCount, seatsPerRow);

    Long availableSeatsCount =
        bookedSeatRepository.countAvailableSeatsByConfigId(seatingConfigEntity.getId());
    if (availableSeatsCount == 0) {
      throw new NoAvailableSeatsException("No seats available");
    }

    List<SeatCoordinate> bookedSeatCoordinates =
        bookedSeatRepository.findBookedSeatCoordinates(seatingConfigEntity.getId());
    BitSet[] seatMap = SeatMapBuilder.buildSeatMap(rowCount, seatsPerRow, bookedSeatCoordinates);

    List<SeatDto> reservedSeats =
        seatAllocator.allocateWithOverflow(seatMap, rowCount, seatsPerRow, numberOfTickets);
    List<SeatDto> bookedSeats =
        bookedSeatCoordinates.stream()
            .map(s -> new SeatDto(String.valueOf(s.getRowLabel()), s.getSeatNumber()))
            .toList();
    String bookingId = String.format("GIC%04d", bookingRepository.getNextId());

    return new StartBookingResponse(bookingId, bookedSeats, reservedSeats);
  }

  @Transactional
  public ConfirmBookingResponse confirmBooking(List<SeatDto> confirmedSeats) {
    return new ConfirmBookingResponse("");
  }
}
