package com.gic.cinemas.common.dto.response;

public record SeatingAvailabilityResponse(
    Long seatingConfigId, String title, int rows, int cols, int availableSeats) {}
