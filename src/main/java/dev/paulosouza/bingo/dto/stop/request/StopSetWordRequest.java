package dev.paulosouza.bingo.dto.stop.request;

import lombok.Data;

import java.util.UUID;

@Data
public class StopSetWordRequest {

    private UUID playerId;

    private String word;

    private int position;

}
