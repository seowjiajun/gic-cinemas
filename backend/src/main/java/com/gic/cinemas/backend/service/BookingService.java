package com.gic.cinemas.backend.service;

import com.gic.cinemas.backend.BookingStatus;
import com.gic.cinemas.backend.exception.NoAvailableSeatsException;
import com.gic.cinemas.backend.exception.SeatJustTakenException;
import com.gic.cinemas.backend.model.BookedSeatEntity;
import com.gic.cinemas.backend.model.BookingEntity;
import com.gic.cinemas.backend.model.SeatingConfigEntity;
import com.gic.cinemas.backend.repository.BookedSeatRepository;
import com.gic.cinemas.backend.repository.BookingRepository;
import com.gic.cinemas.backend.validation.BookingValidator;
import com.gic.cinemas.common.dto.response.BookingConfirmedResponse;
import com.gic.cinemas.common.dto.response.CheckBookingResponse;
import com.gic.cinemas.common.dto.response.ReserveSeatsResponse;
import com.gic.cinemas.common.dto.response.SeatDto;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

  private static final String BOOKING_CODE_PREFIX = "GIC";
  private static final Duration HOLD_DURATION = Duration.ofMinutes(2);

  private final BookingRepository bookingRepository;
  private final BookedSeatRepository bookedSeatRepository;
  private final BookingValidator bookingValidator;
  private final SeatingConfigHelper seatingConfigHelper;
  private final SeatAllocator seatAllocator;

  public BookingService(
      BookingRepository bookingRepository,
      BookedSeatRepository bookedSeatRepository,
      BookingValidator bookingValidator,
      SeatingConfigHelper seatingConfigHelper,
      SeatAllocator seatAllocator) {
    this.bookingRepository = bookingRepository;
    this.bookedSeatRepository = bookedSeatRepository;
    this.bookingValidator = bookingValidator;
    this.seatingConfigHelper = seatingConfigHelper;
    this.seatAllocator = seatAllocator;
  }

  /**
   * Phase 1: Create a HELD booking and place holds for the requested seats. Seats are persisted
   * immediately to prevent conflicts; the booking expires if not confirmed.
   */
  @Transactional
  public ReserveSeatsResponse reserveSeats(
      String movieTitle, int rowCount, int seatsPerRow, int numberOfTickets) {
    SeatingConfigEntity seatingConfigEntity =
        seatingConfigHelper.findOrCreateConfig(movieTitle, rowCount, seatsPerRow);
    Long availableSeatsCount =
        bookedSeatRepository.countAvailableSeatsByConfigId(seatingConfigEntity.getId());
    bookingValidator.validateSeatsAvailable(availableSeatsCount);

    List<SeatDto> bookedSeats = bookedSeatRepository.findBookedSeats(seatingConfigEntity.getId());
    List<SeatDto> reservedSeats =
        seatAllocator.allocateDefault(rowCount, seatsPerRow, numberOfTickets, bookedSeats);

    // Create a PENDING booking (5-min hold)
    LocalDateTime currentTime = LocalDateTime.now();
    BookingEntity booking =
        new BookingEntity(generateBookingId(), seatingConfigEntity, currentTime.plusMinutes(5));
    booking = bookingRepository.saveAndFlush(booking);

    try {
      for (SeatDto seat : reservedSeats) {
        BookedSeatEntity bookedSeat =
            new BookedSeatEntity(booking, seatingConfigEntity, seat.rowLabel(), seat.seatNumber());
        bookedSeatRepository.save(bookedSeat);
      }
      bookedSeatRepository.flush();
    } catch (DataIntegrityViolationException e) {
      throw new SeatJustTakenException();
    }

    return new ReserveSeatsResponse(booking.getBookingId(), bookedSeats, reservedSeats);
  }

  @Transactional
  public BookingConfirmedResponse confirmBooking(String bookingId) {
    BookingEntity booking = bookingRepository.findByBookingId(bookingId);
    bookingValidator.validateBooking(booking, bookingId);
    booking.setStatus(BookingStatus.CONFIRMED);
    bookingRepository.save(booking);

    return new BookingConfirmedResponse(bookingId);
  }

  @Transactional
  public ReserveSeatsResponse changeBooking(String bookingId, SeatDto startSeat) {
    BookingEntity booking = bookingRepository.findByBookingId(bookingId);
    LocalDateTime currentTime = LocalDateTime.now();
    bookingValidator.validateBooking(booking, bookingId, currentTime);

    int seatsToBook = bookedSeatRepository.countByBooking_BookingId(bookingId);
    bookingValidator.validateTicketsHeld(seatsToBook);

    // 1) Build seat map EXCLUDING this booking’s current holds
    SeatingConfigEntity seatingConfig = booking.getSeatingConfig();
    int rowCount = seatingConfig.getRowCount();
    int seatsPerRow = seatingConfig.getSeatsPerRow();

    List<BookingStatus> statuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
    List<SeatDto> takenSeatsExcludingCurrent =
        bookedSeatRepository.findBySeatingConfigIdAndBookingIdNotAndBookingStatusIn(
            seatingConfig.getId(), booking.getId(), statuses);

    long totalSeats = (long) rowCount * seatsPerRow;
    int availableSeats = (int) (totalSeats - takenSeatsExcludingCurrent.size());
    if (availableSeats < seatsToBook) {
      throw new NoAvailableSeatsException(
          "Not enough seats available to allocate (requested %d, available %d)"
              .formatted(seatsToBook, availableSeats),
          availableSeats);
    }

    List<SeatDto> reservedSeats =
        seatAllocator.allocateFromStartSeat(
            rowCount, seatsPerRow, seatsToBook, startSeat, takenSeatsExcludingCurrent);

    // 3) Replace seats atomically (by STRING bookingId)
    bookedSeatRepository.deleteAllByBooking_BookingId(bookingId);
    bookedSeatRepository.flush();
    try {
      for (SeatDto seat : reservedSeats) {
        BookedSeatEntity bookedSeat =
            new BookedSeatEntity(booking, seatingConfig, seat.rowLabel(), seat.seatNumber());
        bookedSeatRepository.save(bookedSeat);
      }
      bookedSeatRepository.flush();
    } catch (DataIntegrityViolationException e) {
      throw new SeatJustTakenException();
    }

    // 4) Roll the hold window forward
    booking.setReservedUntil(currentTime.plusMinutes(5));
    bookingRepository.save(booking);

    // 5) Response (what others already took, plus your new held seats)
    List<SeatDto> alreadyBooked =
        takenSeatsExcludingCurrent.stream()
            .map(sc -> new SeatDto(sc.rowLabel(), sc.seatNumber()))
            .toList();

    return new ReserveSeatsResponse(booking.getBookingId(), alreadyBooked, reservedSeats);
  }

  @Transactional(readOnly = true)
  public CheckBookingResponse checkBookings(String bookingId) {
    // 0) Load & validate booking
    BookingEntity booking = bookingRepository.findByBookingId(bookingId);
    bookingValidator.validateExists(booking, bookingId);

    SeatingConfigEntity cfg = booking.getSeatingConfig();

    // 1) Fetch ALL booked seats for this seating config (this booking + others)
    List<BookedSeatEntity> allBookedForConfig =
        bookedSeatRepository.findAllByBooking_SeatingConfig_Id(cfg.getId());

    // 2) Partition into "mine (CONFIRMED only)" vs "others" (keep your current rule for others)
    List<SeatDto> mySeats = new ArrayList<>();
    List<SeatDto> otherSeats = new ArrayList<>();

    for (BookedSeatEntity s : allBookedForConfig) {
      SeatDto dto = new SeatDto(s.getRowLabel(), s.getSeatNumber());
      if (bookingId.equals(s.getBooking().getBookingId())) {
        if (s.getBooking().getStatus() == BookingStatus.CONFIRMED) {
          mySeats.add(dto); // only confirmed seats for THIS booking
        }
      } else {
        otherSeats.add(dto); // others (pending or confirmed) as before
      }
    }

    // 3) Response
    return new CheckBookingResponse(booking.getBookingId(), mySeats, otherSeats);
  }

  private String generateBookingId() {
    long maxId = bookingRepository.findMaxId();
    return "GIC" + String.format("%04d", maxId + 1);
  }
}
