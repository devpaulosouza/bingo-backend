package dev.paulosouza.bingo.game.stop;

import dev.paulosouza.bingo.game.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopGame {

    private UUID id;

    private Player player;

    private int position;

    private String[] words;

    private boolean[] validWords;

    public void setWords(String[] words) {
        this.words = words;
        this.validWords = new boolean[10];
        Arrays.fill(this.validWords, true);
    }

}
