package dev.paulosouza.bingo.dto.stop.request;

import lombok.Data;

import java.util.UUID;

@Data
public class StopValidateWordRequest {

    private UUID playerId;

    private boolean isValid;

    private int position;

}
