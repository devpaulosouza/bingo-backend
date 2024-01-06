package dev.paulosouza.bingo.utils;

import dev.paulosouza.bingo.game.stop.StopGame;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
public class StopUtils {

    private StopUtils() {

    }

    @SuppressWarnings("java:S3358")
    public static List<StopGame> checkWinner(List<StopGame> games) {

        int playersCount = games.size();

        games.forEach(game -> {
            game.setScore(0);

            for (int i = 0; i < game.getWords().length; i++) {
                int finalI = i;

                if (game.getWords()[finalI] == null) {
                    continue;
                }

                int percentageValid = playersCount >= 10 ? 6 : playersCount >= 3 ? 8 : 9;

                game.setScore(game.getScore() + (game.getValidWords()[i] < percentageValid ? 0 : playersCount));

                game.setScore(
                        game.getScore() - games.stream()
                                .filter(g -> !g.getPlayer().getId().equals(game.getPlayer().getId()))
                                .map(g -> g.getWords()[finalI])
                                .filter(Objects::nonNull)
                                .map(String::toLowerCase)
                                .map(s -> s.replaceAll("\\s", ""))
                                .map(s -> s.replaceAll("[^\\p{ASCII}]", ""))
                                .filter(game.getWords()[finalI]::equalsIgnoreCase)
                                .count()
                );
            }

            if (game.getScore() < 0) {
                game.setScore(0);
            }

            log.info("game score. username = {} score = {}", game.getPlayer().getUsername(), game.getScore());
        });

        return games.stream()
                .collect(Collectors.groupingBy(
                        StopGame::getScore,
                        TreeMap::new,
                        Collectors.toList()
                ))
                .lastEntry()
                .getValue();
    }

}
