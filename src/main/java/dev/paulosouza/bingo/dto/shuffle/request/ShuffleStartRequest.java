package dev.paulosouza.bingo.dto.shuffle.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ShuffleStartRequest {

    @NotEmpty
    private String[] words;

    private int totalWinners;

}
