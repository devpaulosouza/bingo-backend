package dev.paulosouza.bingo.controller;

import dev.paulosouza.bingo.dto.request.MarkRequest;
import dev.paulosouza.bingo.dto.response.BingoResponse;
import dev.paulosouza.bingo.dto.response.MarkResponse;
import dev.paulosouza.bingo.game.Card;
import dev.paulosouza.bingo.game.GameService;
import dev.paulosouza.bingo.game.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("game")
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping("join")
    public ResponseEntity<Card> join(@RequestBody Player player) {
        Card card = this.gameService.join(player);

        return ResponseEntity.ok(card);
    }

    @PostMapping("start")
    public ResponseEntity<Void> start() {
        this.gameService.startGame();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("clean")
    public ResponseEntity<Void> clean() {
        this.gameService.clean();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("mark")
    public ResponseEntity<MarkResponse> mark(@RequestBody MarkRequest request) {
        MarkResponse response = this.gameService.mark(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{playerId}/bingo")
    public ResponseEntity<BingoResponse> bingo(@PathVariable("playerId") UUID playerId) {
        BingoResponse response = this.gameService.bingo(playerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value="/connect/players/{playerId}")
    public SseEmitter connect(@PathVariable("playerId") UUID playerId) {
        SseEmitter emitter = new SseEmitter(0L);

        this.gameService.addListener(playerId, emitter);

        return emitter;
    }

}