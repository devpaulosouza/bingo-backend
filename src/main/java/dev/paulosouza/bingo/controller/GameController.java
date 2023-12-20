package dev.paulosouza.bingo.controller;

import dev.paulosouza.bingo.dto.request.MarkRequest;
import dev.paulosouza.bingo.dto.response.BingoResponse;
import dev.paulosouza.bingo.dto.response.MarkResponse;
import dev.paulosouza.bingo.game.Card;
import dev.paulosouza.bingo.game.GameSingleton;
import dev.paulosouza.bingo.game.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("game")
public class GameController {

    @PostMapping("join")
    public ResponseEntity<Card> join(@RequestBody Player player) {
        Card card = GameSingleton.getInstance().join(player);

        return ResponseEntity.ok(card);
    }

    @PostMapping("start")
    public ResponseEntity<Void> start() {
        GameSingleton.getInstance().startGame();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("mark")
    public ResponseEntity<MarkResponse> mark(@RequestBody MarkRequest request) {
        MarkResponse response = GameSingleton.getInstance().mark(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{playerId}/bingo")
    public ResponseEntity<BingoResponse> bingo(@PathVariable("playerId") UUID playerId) {
        BingoResponse response = GameSingleton.getInstance().bingo(playerId);

        return ResponseEntity.ok(response);
    }

}
