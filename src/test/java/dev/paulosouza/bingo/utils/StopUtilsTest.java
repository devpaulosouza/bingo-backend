package dev.paulosouza.bingo.utils;

import dev.paulosouza.bingo.dto.stop.response.StopPlayerGameResponse;
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

    @Test
    void checkWinnerDraw2() {
        // given
        List<StopGame> games = new ArrayList<>();

        games.add(this.buildGame("0", 0));
        games.add(this.buildGame("1", 1));
        games.add(this.buildGame("2", 2));

        games.get(0).getWords()[0] = "Test1";
        games.get(1).getWords()[0] = "Test1";

        // when
        List<StopGame> winners = StopUtils.checkWinner(games);

        // then
        Assertions.assertEquals(2, winners.size());
        Assertions.assertEquals(2, winners.get(0).getScore());
    }

    @Test
    void checkWinnerDraw3() {
        // given
        List<StopGame> games = new ArrayList<>();

        games.add(this.buildGame("0", 0));
        games.add(this.buildGame("1", 1));

        games.get(0).getWords()[0] = "Test1";
        games.get(0).getWords()[1] = "Test2";
        games.get(1).getWords()[0] = "Test1";
        games.get(1).getWords()[1] = "Test2";

        games.get(0).getValidWords()[0] = 0;
        games.get(1).getValidWords()[0] = 0;

        // when
        List<StopGame> winners = StopUtils.checkWinner(games);

        // then
        Assertions.assertEquals(2, winners.size());
        Assertions.assertEquals(1, winners.get(0).getScore());
    }

    @Test
    void setOtherPLayersWordsResponse() {
        // given
        List<StopGame> games = List.of(
                this.buildGame("A", 0),
                this.buildGame("B", 1),
                this.buildGame("C", 2),
                this.buildGame("D", 3)
        );
        StopPlayerGameResponse response = new StopPlayerGameResponse();

        games.get(0).setWords(new String[]{ "A1", "A2" });
        games.get(1).setWords(new String[]{ "B1", "B2" });
        games.get(2).setWords(new String[]{ null, "C2" });
        games.get(3).setWords(new String[]{ "D1", "D2" });

        // when
        StopUtils.setOtherPLayersWordsResponse(games.get(0), 0, games, response);

        // then

        Assertions.assertEquals(4, response.getOtherPlayersWords().size());

        Assertions.assertEquals(0, response.getOtherPlayersPosition().get(0));
        Assertions.assertEquals(1, response.getOtherPlayersPosition().get(1));
        Assertions.assertEquals(2, response.getOtherPlayersPosition().get(2));
        Assertions.assertEquals(3, response.getOtherPlayersPosition().get(3));

        Assertions.assertEquals("A1", response.getOtherPlayersWords().get(0));
        Assertions.assertEquals("B1", response.getOtherPlayersWords().get(1));
        Assertions.assertNull(response.getOtherPlayersWords().get(2));
        Assertions.assertEquals("D1", response.getOtherPlayersWords().get(3));
    }

    @Test
    void setOtherPLayersWordsResponse2() {
        // given
        List<StopGame> games = List.of(
                this.buildGame("A", 0),
                this.buildGame("B", 1),
                this.buildGame("C", 2),
                this.buildGame("D", 3)
        );
        StopPlayerGameResponse response = new StopPlayerGameResponse();

        games.get(0).setWords(new String[]{ "A1", "A2" });
        games.get(1).setWords(new String[]{ "B1", "B2" });
        games.get(2).setWords(new String[]{ null, "C2" });
        games.get(3).setWords(new String[]{ "D1", "D2" });

        // when
        StopUtils.setOtherPLayersWordsResponse(games.get(0), 1, games, response);

        // then

        Assertions.assertEquals(4, response.getOtherPlayersWords().size());

        Assertions.assertEquals(0, response.getOtherPlayersPosition().get(0));
        Assertions.assertEquals(1, response.getOtherPlayersPosition().get(1));
        Assertions.assertEquals(2, response.getOtherPlayersPosition().get(2));
        Assertions.assertEquals(3, response.getOtherPlayersPosition().get(3));

        Assertions.assertEquals("A2", response.getOtherPlayersWords().get(0));
        Assertions.assertEquals("B2", response.getOtherPlayersWords().get(1));
        Assertions.assertEquals("C2", response.getOtherPlayersWords().get(2));
        Assertions.assertEquals("D2", response.getOtherPlayersWords().get(3));
    }

    @Test
    void setOtherPLayersWordsResponse3() {
        // given
        List<StopGame> games = List.of(
                this.buildGame("A", 0),
                this.buildGame("B", 1),
                this.buildGame("C", 2),
                this.buildGame("D", 3)
        );
        StopPlayerGameResponse response = new StopPlayerGameResponse();

        games.get(0).setWords(new String[]{ "A1", "A2" });
        games.get(1).setWords(new String[]{ "B1", "B2" });
        games.get(2).setWords(new String[]{ null, "C2" });
        games.get(3).setWords(new String[]{ "D1", "D2" });

        // when
        StopUtils.setOtherPLayersWordsResponse(games.get(2), 1, games, response);

        // then

        Assertions.assertEquals(4, response.getOtherPlayersWords().size());

        Assertions.assertEquals("A2", response.getOtherPlayersWords().get(0));
        Assertions.assertEquals("B2", response.getOtherPlayersWords().get(1));
        Assertions.assertEquals("C2", response.getOtherPlayersWords().get(2));
        Assertions.assertEquals("D2", response.getOtherPlayersWords().get(3));
    }

    @Test
    void setOtherPLayersWordsResponse4() {
        // given
        List<StopGame> games = List.of(
                this.buildGame("A", 0),
                this.buildGame("B", 1),
                this.buildGame("C", 2),
                this.buildGame("D", 3),
                this.buildGame("E", 4),
                this.buildGame("F", 5),
                this.buildGame("G", 6),
                this.buildGame("H", 7),
                this.buildGame("I", 8),
                this.buildGame("J", 9),
                this.buildGame("K", 10)
        );
        StopPlayerGameResponse response = new StopPlayerGameResponse();

        games.get(0).setWords(new String[]{ "A1", "A2" });
        games.get(1).setWords(new String[]{ "B1", "B2" });
        games.get(2).setWords(new String[]{ "C1", "C2" });
        games.get(3).setWords(new String[]{ "D1", "D2" });
        games.get(4).setWords(new String[]{ "E1", "E2" });
        games.get(5).setWords(new String[]{ "F1", null });
        games.get(6).setWords(new String[]{ null, "G2" });
        games.get(7).setWords(new String[]{ "H1", "H2" });
        games.get(8).setWords(new String[]{ "I1", "I2" });
        games.get(9).setWords(new String[]{ "J1", "J2" });
        games.get(10).setWords(new String[]{ "K1", "K2" });

        // when
        StopUtils.setOtherPLayersWordsResponse(games.get(0), 0, games, response);

        // then

        Assertions.assertEquals(10, response.getOtherPlayersWords().size());

        Assertions.assertEquals("A1", response.getOtherPlayersWords().get(0));
        Assertions.assertEquals("B1", response.getOtherPlayersWords().get(1));
        Assertions.assertEquals("C1", response.getOtherPlayersWords().get(2));
        Assertions.assertEquals("D1", response.getOtherPlayersWords().get(3));
        Assertions.assertEquals("E1", response.getOtherPlayersWords().get(4));
        Assertions.assertEquals("F1", response.getOtherPlayersWords().get(5));
        Assertions.assertNull(response.getOtherPlayersWords().get(6));
        Assertions.assertEquals("H1", response.getOtherPlayersWords().get(7));
        Assertions.assertEquals("I1", response.getOtherPlayersWords().get(8));
        Assertions.assertEquals("J1", response.getOtherPlayersWords().get(9));
    }

    @Test
    void setOtherPLayersWordsResponse5() {
        // given
        List<StopGame> games = List.of(
                this.buildGame("A", 0),
                this.buildGame("B", 1),
                this.buildGame("C", 2),
                this.buildGame("D", 3),
                this.buildGame("E", 4),
                this.buildGame("F", 5),
                this.buildGame("G", 6),
                this.buildGame("H", 7),
                this.buildGame("I", 8),
                this.buildGame("J", 9),
                this.buildGame("K", 10)
        );
        StopPlayerGameResponse response = new StopPlayerGameResponse();

        games.get(0).setWords(new String[]{ "A1", "A2" });
        games.get(1).setWords(new String[]{ "B1", "B2" });
        games.get(2).setWords(new String[]{ "C1", "C2" });
        games.get(3).setWords(new String[]{ "D1", "D2" });
        games.get(4).setWords(new String[]{ "E1", "E2" });
        games.get(5).setWords(new String[]{ "F1", null });
        games.get(6).setWords(new String[]{ null, "G2" });
        games.get(7).setWords(new String[]{ "H1", "H2" });
        games.get(8).setWords(new String[]{ "I1", "I2" });
        games.get(9).setWords(new String[]{ "J1", "J2" });
        games.get(10).setWords(new String[]{ "K1", "K2" });

        // when
        StopUtils.setOtherPLayersWordsResponse(games.get(1), 0, games, response);

        // then

        Assertions.assertEquals(10, response.getOtherPlayersWords().size());

        Assertions.assertEquals("A1", response.getOtherPlayersWords().get(0));
        Assertions.assertEquals("B1", response.getOtherPlayersWords().get(1));
        Assertions.assertEquals("C1", response.getOtherPlayersWords().get(2));
        Assertions.assertEquals("D1", response.getOtherPlayersWords().get(3));
        Assertions.assertEquals("E1", response.getOtherPlayersWords().get(4));
        Assertions.assertEquals("F1", response.getOtherPlayersWords().get(5));
        Assertions.assertNull(response.getOtherPlayersWords().get(6));
        Assertions.assertEquals("H1", response.getOtherPlayersWords().get(7));
        Assertions.assertEquals("I1", response.getOtherPlayersWords().get(8));
        Assertions.assertEquals("J1", response.getOtherPlayersWords().get(9));
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