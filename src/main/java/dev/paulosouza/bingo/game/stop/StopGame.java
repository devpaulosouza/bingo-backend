package dev.paulosouza.bingo.game.stop;

import dev.paulosouza.bingo.game.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopGame {

    private UUID id;

    private Player player;

    private int position;

    private String[] words;

    private int[] validWords;

    private long score;

    private long[] scores;

    private HashSet<Player>[] invalidPlayers;

    public void setWords(String[] words) {
        this.words = words;
        this.validWords = new int[words.length];
        this.invalidPlayers = new HashSet[words.length];
        Arrays.fill(this.validWords, 10);
        Arrays.fill(this.words, "");
        Arrays.fill(this.invalidPlayers, new HashSet<>());
    }

}
