package com.gic.cinemas.common.dto.response;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReserveSeatsResponse(
    @NotNull String bookingId,
    @NotNull List<SeatDto> bookedSeats,
    @NotNull List<SeatDto> reservedSeats) {}
