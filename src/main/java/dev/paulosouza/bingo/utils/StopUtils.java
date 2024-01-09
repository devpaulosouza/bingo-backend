package dev.paulosouza.bingo.utils;

import dev.paulosouza.bingo.dto.stop.response.StopPlayerGameResponse;
import dev.paulosouza.bingo.game.stop.StopGame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
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

                if (i == 0) {
                    game.setScores(new long[game.getWords().length]);
                }

                if (game.getWords()[finalI] == null) {
                    continue;
                }

                int percentageValid = playersCount >= 10 ? 6 : 9;

                long minusScore = games.stream()
                        .filter(g -> !g.getPlayer().getId().equals(game.getPlayer().getId()))
                        .map(g -> g.getWords()[finalI])
                        .filter(Objects::nonNull)
                        .filter(w -> !w.isEmpty())
                        .map(String::toLowerCase)
                        .map(s -> s.replaceAll("\\s", ""))
                        .map(s -> s.replaceAll("[^\\p{ASCII}]", ""))
                        .filter(game.getWords()[finalI]::equalsIgnoreCase)
                        .count();

                long newScore = game.getValidWords()[i] <= percentageValid ? 0 : playersCount;

                if (StringUtils.isEmpty(game.getWords()[i])) {
                    newScore = 0;
                }

                game.getScores()[i] = (newScore - minusScore < 0) ? 0 : (newScore - minusScore);

            }

            if (game.getScore() < 0) {
                game.setScore(0);
            }
        });

        if (games.isEmpty()) {
            return Collections.emptyList();
        }

        games.forEach(g -> g.setScore(Arrays.stream(g.getScores()).sum()));

        return games.stream()
                .collect(Collectors.groupingBy(
                        StopGame::getScore,
                        TreeMap::new,
                        Collectors.toList()
                ))
                .lastEntry()
                .getValue();
    }


    public static void setOtherPLayersWordsResponse(
            StopGame game,
            int validateWordCount,
            List<StopGame> games,
            StopPlayerGameResponse response
    ) {
        int max = Math.min(10, games.size());

        if (validateWordCount != -1) {
            response.setOtherPlayersPosition(
                    games.stream()
                            .filter(g -> (g.getPosition() < max + game.getPosition()) && (g.getPosition() > game.getPosition()))
                            .map(StopGame::getPosition)
                            .toList()
            );

            if (response.getOtherPlayersPosition().size() < 10) {
                response.setOtherPlayersPosition(
                        games.stream()
                                .map(StopGame::getPosition)
                                .toList()
                );
            }

            response.setOtherPlayersWords(
                    games.stream()
                            .filter(g -> response.getOtherPlayersPosition().contains(g.getPosition()))
                            .map(StopGame::getWords)
                            .map(words -> words[validateWordCount])
                            .limit(max)
                            .toList()
            );
        }
    }

}
