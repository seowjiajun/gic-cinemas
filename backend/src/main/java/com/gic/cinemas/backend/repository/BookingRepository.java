package com.gic.cinemas.backend.repository;

import com.gic.cinemas.backend.model.BookingEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

  /** All bookings for a seating config (useful for admin / reports). */
  List<BookingEntity> findBySeatingConfig_Id(Long seatingConfigId);

  /** Quick count of bookings under a config. */
  int countBySeatingConfig_Id(Long seatingConfigId);

  /** Lookup a booking by id but scoped to a specific seating config. */
  Optional<BookingEntity> findByIdAndSeatingConfig_Id(Long bookingId, Long seatingConfigId);

  /** Find by current status (HELD / CONFIRMED / CANCELED). */
  List<BookingEntity> findByStatus(BookingEntity.Status status);

  /** For cleanup jobs: expired holds. */
  List<BookingEntity> findByStatusAndExpiresAtBefore(BookingEntity.Status status, Instant cutoff);

  /** Useful guard: does a booking with this id have this status? */
  boolean existsByIdAndStatus(Long bookingId, BookingEntity.Status status);
}
