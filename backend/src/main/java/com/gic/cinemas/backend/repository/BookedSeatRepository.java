package com.gic.cinemas.backend.repository;

import com.gic.cinemas.backend.model.BookedSeatEntity;
import com.gic.cinemas.common.dto.BookingStatus;
import com.gic.cinemas.common.dto.SeatDto;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

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
  Optional<Long> countAvailableSeatsByConfigId(Long seatingConfigId);

  @Query(
      """
  select new com.gic.cinemas.common.dto.SeatDto(bs.rowLabel, bs.seatNumber)
  from BookedSeatEntity bs
  where bs.seatingConfig.id = :seatConfigId
    and bs.booking.status in (
         com.gic.cinemas.common.dto.BookingStatus.PENDING,
         com.gic.cinemas.common.dto.BookingStatus.CONFIRMED
       )
""")
  List<SeatDto> findBookedSeats(Long seatConfigId);

  // Seats taken for a config by PENDING or CONFIRMED bookings
  List<SeatDto> findBySeatingConfigIdAndBookingStatusIn(
      Long seatingConfigId, Collection<BookingStatus> statuses);

  // Same, but exclude a specific booking (for changeBooking on existing booking)
  List<SeatDto> findBySeatingConfigIdAndBookingIdNotAndBookingStatusIn(
      Long seatingConfigId, Long bookingId, Collection<BookingStatus> statuses);

  // Count how many seats belong to a booking using its public string ID
  int countByBooking_BookingId(String bookingId);

  @Transactional
  void deleteAllByBooking_BookingId(String bookingId);

  List<BookedSeatEntity> findAllByBooking_SeatingConfig_Id(Long seatingConfigId);
}
