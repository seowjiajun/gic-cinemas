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
        @UniqueConstraint(columnNames = {"seating_config_id", "row_label", "seat_number"}))
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
  private String rowLabel;

  @Column(name = "seat_number", nullable = false)
  private int seatNumber;

  public BookedSeatEntity(
      BookingEntity booking, SeatingConfigEntity seatingConfig, String rowLabel, int seatNumber) {
    this.booking = booking;
    this.seatingConfig = seatingConfig;
    this.rowLabel = rowLabel;
    this.seatNumber = seatNumber;
  }
}
