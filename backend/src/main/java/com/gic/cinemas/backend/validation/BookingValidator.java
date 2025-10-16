package com.gic.cinemas.backend.validation;

import com.gic.cinemas.backend.BookingStatus;
import com.gic.cinemas.backend.exception.*;
import com.gic.cinemas.backend.model.BookingEntity;
import com.gic.cinemas.backend.repository.BookingRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class BookingValidator {
  private final BookingRepository bookingRepository;

  public BookingValidator(BookingRepository bookingRepository) {
    this.bookingRepository = bookingRepository;
  }

  public void validateSeatsAvailable(long availableSeatsCount) {
    if (availableSeatsCount == 0) {
      throw new NoAvailableSeatsException("No seats available", availableSeatsCount);
    }
  }

  public void validateBooking(BookingEntity booking, String bookingId) {
    validateExists(booking, bookingId);
    validatePending(booking);
    validateNotExpired(booking);
  }

  public void validateBooking(BookingEntity booking, String bookingId, LocalDateTime currentTime) {
    validateExists(booking, bookingId);
    validatePending(booking);
    validateNotExpired(booking, currentTime);
  }

  public void validateExists(BookingEntity booking, String bookingId) {
    if (booking == null) {
      throw new BookingNotFoundException(bookingId);
    }
  }

  public void validatePending(BookingEntity booking) {
    if (booking.getStatus() != BookingStatus.PENDING) {
      throw new BookingNotPendingException(booking.getBookingId());
    }
  }

  public void validateNotExpired(BookingEntity booking) {
    if (booking.getReservedUntil().isBefore(LocalDateTime.now())) {
      booking.setStatus(BookingStatus.EXPIRED);
      bookingRepository.save(booking);
      throw new BookingExpiredException(booking.getBookingId());
    }
  }

  public void validateNotExpired(BookingEntity booking, LocalDateTime currentTime) {
    if (booking.getReservedUntil().isBefore(currentTime)) {
      booking.setStatus(BookingStatus.EXPIRED);
      bookingRepository.save(booking);
      throw new BookingExpiredException(booking.getBookingId());
    }
  }

  public void validateTicketsHeld(int tickets) {
    if (tickets <= 0) {
      throw new NoHeldSeatsException(tickets);
    }
  }
}
