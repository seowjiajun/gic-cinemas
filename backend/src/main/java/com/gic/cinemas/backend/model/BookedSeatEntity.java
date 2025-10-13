package com.gic.cinemas.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "booked_seat",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"seating_config_id", "row_label", "seat_no"}))
public class BookedSeatEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "booking_id", nullable = false)
  private BookingEntity booking;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "seating_config_id", nullable = false)
  private SeatingConfigEntity seatingConfig;

  @Column(name = "row_label", nullable = false)
  private int rowLabel;

  @Column(name = "seat_no", nullable = false)
  private int seatNo;

  public BookedSeatEntity(
      BookingEntity booking, SeatingConfigEntity seatingConfig, int rowLabel, int seatNo) {
    this.booking = booking;
    this.seatingConfig = seatingConfig;
    this.rowLabel = rowLabel;
    this.seatNo = seatNo;
  }
}
