package dev.paulosouza.bingo.game;

import dev.paulosouza.bingo.dto.bingo.request.MarkRequest;
import dev.paulosouza.bingo.dto.bingo.request.PlayerRequest;
import dev.paulosouza.bingo.dto.bingo.response.MarkResponse;
import dev.paulosouza.bingo.game.bingo.BingoService;
import dev.paulosouza.bingo.game.bingo.BingoCard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BingoServiceTest {

    @InjectMocks
    private BingoService bingoService;

    @Test
    void join() {
        // given

        // when
        BingoCard card = this.bingoService.join(new PlayerRequest());

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
        BingoCard card = this.bingoService.join(new PlayerRequest());
        this.bingoService.startGame();

        MarkRequest request = new MarkRequest();

        request.setI(0);
        request.setJ(0);
        request.setMarked(true);
        request.setPlayerId(card.getPlayer().getId());

        // when
        MarkResponse response = this.bingoService.mark(request);

        // then
        Assertions.assertNotNull(response);
    }

}