package com.gic.cinemas.common.dto.response;

import com.gic.cinemas.common.dto.SeatDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReservedSeatsResponse(
    @NotBlank String bookingId,
    @NotNull List<@Valid SeatDto> takenSeats,
    @NotNull @Size(min = 1) List<@Valid SeatDto> reservedSeats) {}
