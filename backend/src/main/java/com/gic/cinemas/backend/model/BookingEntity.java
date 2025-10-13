package com.gic.cinemas.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "booking")
@SequenceGenerator(name = "booking_seq", sequenceName = "booking_seq", allocationSize = 1)
public class BookingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seq")
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "seating_config_id", nullable = false)
  private SeatingConfigEntity seatingConfig;

  @Column(name = "confirmed", nullable = false)
  private boolean confirmed = false;

  public BookingEntity(SeatingConfigEntity seatingConfig) {
    this.seatingConfig = seatingConfig;
  }

  // convenience code like GIC0001
  @Transient
  public String getBookingCode() {
    return id == null ? null : String.format("GIC%04d", id);
  }
}
