package com.gic.cinemas.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "booking")
public class BookingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "seating_config_id", nullable = false)
  private SeatingConfigEntity seatingConfig;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status = Status.HELD; // start as HELD

  @Column(name = "expires_at")
  private Instant expiresAt; // set when HELD

  public enum Status {
    HELD,
    CONFIRMED,
    CANCELED
  }

  protected BookingEntity() {}

  public BookingEntity(SeatingConfigEntity seatingConfig) {
    this.seatingConfig = seatingConfig;
    this.status = Status.HELD;
  }

  // --------- getters and setters ----------

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public SeatingConfigEntity getSeatingConfig() {
    return seatingConfig;
  }

  public void setSeatingConfig(SeatingConfigEntity seatingConfig) {
    this.seatingConfig = seatingConfig;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  // convenience code like GIC0001
  @Transient
  public String getBookingCode() {
    return id == null ? null : String.format("GIC%04d", id);
  }
}
