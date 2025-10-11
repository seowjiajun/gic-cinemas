package com.gic.cinemas.backend.model;

import com.gic.cinemas.common.dto.SeatDto;
import java.util.List;

public record Booking(String id, String movie, List<SeatDto> seats) {}
