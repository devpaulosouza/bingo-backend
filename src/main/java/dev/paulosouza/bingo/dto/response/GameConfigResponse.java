package dev.paulosouza.bingo.dto.response;

import dev.paulosouza.bingo.dto.request.GameType;
import lombok.Data;

@Data
public class GameConfigResponse {

    private GameType gameType;

}
