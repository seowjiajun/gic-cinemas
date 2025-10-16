package com.gic.cinemas.backend.exception;

public class SeatingConfigNotFoundException extends RuntimeException {
  private final String movieTitle;
  private final int rowCount;
  private final int seatsPerRow;

  public SeatingConfigNotFoundException(String movieTitle, int rowCount, int seatsPerRow) {
    super(
        String.format(
            "No seating configuration found for '%s' (%dx%d)", movieTitle, rowCount, seatsPerRow));
    this.movieTitle = movieTitle;
    this.rowCount = rowCount;
    this.seatsPerRow = seatsPerRow;
  }

  public String getMovieTitle() {
    return movieTitle;
  }

  public int getRowCount() {
    return rowCount;
  }

  public int getSeatsPerRow() {
    return seatsPerRow;
  }
}
