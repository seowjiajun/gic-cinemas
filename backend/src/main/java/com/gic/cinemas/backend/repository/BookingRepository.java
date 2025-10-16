package com.gic.cinemas.backend.repository;

import com.gic.cinemas.backend.model.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

  @Query("SELECT COALESCE(MAX(b.id), 0) FROM BookingEntity b")
  Long findMaxId();

  BookingEntity findByBookingId(String bookingId);
}
