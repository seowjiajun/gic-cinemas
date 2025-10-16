package com.gic.cinemas.backend.model;

import com.gic.cinemas.backend.BookingStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "booking")
public class BookingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 16)
  private String bookingId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private BookingStatus status = BookingStatus.PENDING;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "seating_config_id", nullable = false)
  private SeatingConfigEntity seatingConfig;

  @Column(nullable = false)
  private LocalDateTime reservedUntil;

  public BookingEntity(
      String bookingId, SeatingConfigEntity seatingConfig, LocalDateTime reservedUntil) {
    this.bookingId = bookingId;
    this.seatingConfig = seatingConfig;
    this.reservedUntil = reservedUntil;
  }
}
