package com.gic.cinemas.backend.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "booked_seat",
    uniqueConstraints = @UniqueConstraint(columnNames = {"seating_config_id", "row_no", "col_no"}))
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

  @Column(name = "row_no", nullable = false)
  private int rowNo;

  @Column(name = "col_no", nullable = false)
  private int colNo;

  protected BookedSeatEntity() {}

  public BookedSeatEntity(
      BookingEntity booking, SeatingConfigEntity seatingConfig, int rowNo, int colNo) {
    this.booking = booking;
    this.seatingConfig = seatingConfig;
    this.rowNo = rowNo;
    this.colNo = colNo;
  }

  // getters
  public int getRowNo() {
    return rowNo;
  }

  public int getColNo() {
    return colNo;
  }

  public BookingEntity getBooking() {
    return booking;
  }
}
