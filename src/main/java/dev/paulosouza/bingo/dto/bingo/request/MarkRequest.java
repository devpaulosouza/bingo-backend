package dev.paulosouza.bingo.dto.bingo.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MarkRequest {

    @NotNull
    private UUID playerId;

    @Min(0)
    @Max(4)
    private int i;

    @Min(0)
    @Max(4)
    private int j;

    private boolean marked;

}
