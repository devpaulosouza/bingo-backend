package dev.paulosouza.bingo.game;

import dev.paulosouza.bingo.dto.request.MarkRequest;
import dev.paulosouza.bingo.dto.response.MarkResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @InjectMocks
    private GameService gameService;

    @Test
    void join() {
        // given

        // when
        Card card = this.gameService.join(new Player());

        // then
        Assertions.assertNotNull(card);
        Assertions.assertNotNull(card.getId());
        Assertions.assertNotNull(card.getPlayer());
        Assertions.assertNotNull(card.getPlayer().getId());
        Assertions.assertNotNull(card.getNumbersList());
        Assertions.assertEquals(25, card.getNumbersList().size());
    }

    @Test
    void mark() {
        // given
        Card card = this.gameService.join(new Player());
        this.gameService.startGame();

        MarkRequest request = new MarkRequest();

        request.setI(0);
        request.setJ(0);
        request.setMarked(true);
        request.setPlayerId(card.getPlayer().getId());

        // when
        MarkResponse response = this.gameService.mark(request);

        // then
        Assertions.assertNotNull(response);
    }

}