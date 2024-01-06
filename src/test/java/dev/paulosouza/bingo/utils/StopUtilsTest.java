package dev.paulosouza.bingo.utils;

import dev.paulosouza.bingo.game.Player;
import dev.paulosouza.bingo.game.stop.StopGame;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class StopUtilsTest {

    @Test
    void checkWinner() {
        // given
        List<StopGame> games = new ArrayList<>();

        games.add(this.buildGame("0", 0));
        games.add(this.buildGame("1", 1));

        games.get(0).getWords()[0] = "Test1";
        games.get(0).getWords()[1] = "Test2";
        games.get(1).getWords()[0] = "Test1";

        // when
        List<StopGame> winners = StopUtils.checkWinner(games);

        // then
        Assertions.assertEquals(1, winners.size());
        Assertions.assertEquals(games.get(0).getId(), winners.get(0).getId());
        Assertions.assertEquals(3, winners.get(0).getScore());
    }

    @Test
    void checkWinner2() {
        // given
        List<StopGame> games = new ArrayList<>();

        games.add(this.buildGame("0", 0));
        games.add(this.buildGame("1", 1));
        games.add(this.buildGame("2", 2));
        games.add(this.buildGame("3", 3));
        games.add(this.buildGame("4", 4));

        games.get(0).getWords()[0] = "Test1";
        games.get(0).getWords()[1] = "Test2";
        games.get(1).getWords()[0] = "Test1";
        games.get(2).getWords()[0] = "Test2";
        games.get(2).getWords()[1] = "Test3";
        games.get(3).getWords()[1] = "Test3";

        games.get(0).getValidWords()[0] = 7;
        games.get(1).getValidWords()[0] = 7;

        // when
        List<StopGame> winners = StopUtils.checkWinner(games);

        // then
        Assertions.assertEquals(1, winners.size());
        Assertions.assertEquals(games.get(2).getId(), winners.get(0).getId());
        Assertions.assertEquals(9, winners.get(0).getScore());
    }

    @Test
    void checkWinnerDraw() {
        // given
        List<StopGame> games = new ArrayList<>();

        games.add(this.buildGame("0", 0));
        games.add(this.buildGame("1", 1));

        games.get(0).getWords()[0] = "Test1";
        games.get(0).getWords()[1] = "Test2";
        games.get(1).getWords()[0] = "Test1";
        games.get(1).getWords()[1] = "Test2";

        // when
        List<StopGame> winners = StopUtils.checkWinner(games);

        // then
        Assertions.assertEquals(2, winners.size());
        Assertions.assertEquals(games.get(0).getId(), winners.get(0).getId());
        Assertions.assertEquals(2, winners.get(0).getScore());
        Assertions.assertEquals(2, winners.get(1).getScore());
    }

    private StopGame buildGame(String username, int position) {
        Player player = new Player();

        player.setId(UUID.randomUUID());
        player.setName(username);
        player.setUsername(username);

        StopGame game = StopGame.builder()
                .id(UUID.randomUUID())
                .player(player)
                .build();

        game.setWords(new String[2]);
        game.setPosition(position);

        return game;
    }

}