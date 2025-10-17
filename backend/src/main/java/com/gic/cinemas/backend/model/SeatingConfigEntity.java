package com.gic.cinemas.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "seating_config",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"movie_title", "row_count", "seats_per_row"}))
public class SeatingConfigEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "movie_title", nullable = false)
  private String movieTitle;

  @Column(name = "row_count", nullable = false)
  private int rowCount;

  @Column(name = "seats_per_row", nullable = false)
  private int seatsPerRow;

  public SeatingConfigEntity(String movieTitle, int rowCount, int seatsPerRow) {
    this.movieTitle = movieTitle;
    this.rowCount = rowCount;
    this.seatsPerRow = seatsPerRow;
  }
}
