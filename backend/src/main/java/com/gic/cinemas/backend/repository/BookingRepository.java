package com.gic.cinemas.backend.repository;

import com.gic.cinemas.backend.model.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

  @Query(value = "SELECT nextval('booking_seq')", nativeQuery = true)
  Long getNextId();
}
