package com.gic.cinemas.common.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CheckBookingResponse(
    @NotBlank String bookingId,
    @NotNull List<SeatDto> bookedSeats,
    @NotNull List<SeatDto> takenSeats) {}
