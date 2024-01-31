package dev.paulosouza.bingo.dto.stop.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.paulosouza.bingo.game.Player;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StopPlayerGameResponse {

    private String[] words;

    private List<String> drawnWords;

    private Character letter;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime canStopAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime stopAt;

    private boolean canStop;

    private boolean isStopped;

    private boolean validatingWords;

    private Integer validateWordCount;

    private List<String> otherPlayersWords;

    private List<Integer> otherPlayersPosition;

    private List<Player> winners;

    private int playersCount;

}
