package dev.paulosouza.bingo.dto.stop.request;

import lombok.Data;

@Data
public class StopValidateWordRequest {

    private int playerPosition;

    private int position;

    private int points = 1;

}
