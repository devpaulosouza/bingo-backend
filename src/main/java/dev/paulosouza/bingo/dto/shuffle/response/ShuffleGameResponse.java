package dev.paulosouza.bingo.dto.shuffle.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.paulosouza.bingo.game.shuffle.ShufflePlayer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShuffleGameResponse {

    private List<ShufflePlayer> players;

    private List<ShufflePlayer> winners;

    private String[] shuffledWords;

    private String[] words;

    private boolean isGameRunning;

    private int playersCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private ZonedDateTime startAt;

    private boolean isStarted;

}
