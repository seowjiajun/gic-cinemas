package com.gic.cinemas.common.dto.request;

import com.gic.cinemas.common.dto.SeatDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeSeatsRequest(@NotBlank String bookingId, @NotNull @Valid SeatDto startSeat) {}
