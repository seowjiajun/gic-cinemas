package com.gic.cinemas.backend.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "seating_config",
    uniqueConstraints = @UniqueConstraint(columnNames = {"movie_title", "rows", "seats_per_row"}))
public class SeatingConfigEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "movie_title", nullable = false)
  private String movieTitle;

  @Column(name = "rows", nullable = false)
  private int rows;

  @Column(name = "seats_per_row", nullable = false)
  private int seatsPerRow;

  // ✅ Empty constructor is required by JPA
  public SeatingConfigEntity() {}

  // ✅ Convenience constructor for quick creation
  public SeatingConfigEntity(String movieTitle, int rows, int seatsPerRow) {
    this.movieTitle = movieTitle;
    this.rows = rows;
    this.seatsPerRow = seatsPerRow;
  }

  // ✅ Getters and setters
  public Long getId() {
    return id;
  }

  public String getMovieTitle() {
    return movieTitle;
  }

  public void setMovieTitle(String movieTitle) {
    this.movieTitle = movieTitle;
  }

  public int getRows() {
    return rows;
  }

  public void setRows(int rows) {
    this.rows = rows;
  }

  public int getSeatsPerRow() {
    return seatsPerRow;
  }

  public void setSeatsPerRow(int seatsPerRow) {
    this.seatsPerRow = seatsPerRow;
  }
}
