package com.gic.cinemas.backend.repository;

import com.gic.cinemas.backend.model.BookedSeatEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookedSeatRepository extends JpaRepository<BookedSeatEntity, Long> {

  @Query(
      """
    SELECT (sc.rowCount * sc.seatsPerRow) - COUNT(bs)
    FROM SeatingConfigEntity sc
    LEFT JOIN BookedSeatEntity bs
      ON bs.seatingConfig.id = sc.id
    WHERE sc.id = :seatingConfigId
    GROUP BY sc.rowCount, sc.seatsPerRow
  """)
  Long countAvailableSeatsByConfigId(Long seatingConfigId);

  @Query(
      """
    select bs.rowLabel, bs.seatNo
    from BookedSeatEntity bs
    where bs.seatingConfig.id = :seatConfigId
  """)
  List<SeatCoordinate> findBookedSeatCoordinates(Long seatConfigId);
}
