package dev.paulosouza.bingo.controller;

import dev.paulosouza.bingo.dto.request.GameMode;
import dev.paulosouza.bingo.dto.request.MarkRequest;
import dev.paulosouza.bingo.dto.request.PasswordRequest;
import dev.paulosouza.bingo.dto.request.PlayerRequest;
import dev.paulosouza.bingo.dto.response.*;
import dev.paulosouza.bingo.game.Card;
import dev.paulosouza.bingo.game.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("game")
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping("join")
    public ResponseEntity<Card> join(@RequestBody PlayerRequest player) {
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

    @GetMapping("/users/{playerId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable("playerId") UUID playerId) {
        GameResponse response = this.gameService.getGame(playerId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/admin")
    public ResponseEntity<AdminGameResponse> getGame() {
        AdminGameResponse response = this.gameService.getGame();

        return ResponseEntity.ok(response);
    }

    @GetMapping(value="/connect/players/{playerId}")
    public SseEmitter connect(
            @PathVariable("playerId") UUID playerId,
            @RequestParam(value = "isAdmin", required = false) boolean isAdmin
    ) {
        SseEmitter emitter = new SseEmitter(0L);

        this.gameService.addListener(playerId, isAdmin, emitter);

        return emitter;
    }

    @PostMapping("/allow-list")
    public ResponseEntity<Void> allowList(@RequestBody List<String> usernames) {
        this.gameService.setAllowList(usernames);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/game-mode")
    public ResponseEntity<Void> setGameMode(@RequestBody GameMode gameMode) {
        this.gameService.setGameMode(gameMode);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password")
    public ResponseEntity<Void> setPassword(@RequestBody PasswordRequest request) {
        this.gameService.setPassword(request.getPassword());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/has-password")
    public ResponseEntity<HasPasswordResponse> getHasPassword() {
        HasPasswordResponse response = this.gameService.hasPassword();

        return ResponseEntity.ok(response);
    }

}
