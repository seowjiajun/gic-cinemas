package com.gic.cinemas.backend.repository;

import com.gic.cinemas.backend.model.BookedSeatEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookedSeatRepository extends JpaRepository<BookedSeatEntity, Long> {
  boolean existsBySeatingConfig_IdAndRowNoAndColNo(Long cfgId, int rowNo, int colNo);

  void deleteByBookingId(Long bookingId);

  List<BookedSeatEntity> findBySeatingConfig_Id(Long seatingConfigId);
}
