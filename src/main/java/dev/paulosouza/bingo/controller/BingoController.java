package dev.paulosouza.bingo.controller;

import dev.paulosouza.bingo.dto.bingo.request.BingoMode;
import dev.paulosouza.bingo.dto.bingo.request.MarkRequest;
import dev.paulosouza.bingo.dto.bingo.request.PasswordRequest;
import dev.paulosouza.bingo.dto.bingo.request.PlayerRequest;
import dev.paulosouza.bingo.dto.bingo.response.*;
import dev.paulosouza.bingo.game.bingo.BingoService;
import dev.paulosouza.bingo.game.bingo.Card;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/games/bingo")
public class BingoController {

    @Autowired
    private BingoService bingoService;

    @PostMapping("join")
    public ResponseEntity<Card> join(@RequestBody PlayerRequest player) {
        Card card = this.bingoService.join(player);

        return ResponseEntity.ok(card);
    }

    @PostMapping("start")
    public ResponseEntity<Void> start() {
        this.bingoService.startGame();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("clean")
    public ResponseEntity<Void> clean() {
        this.bingoService.clean();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("mark")
    public ResponseEntity<MarkResponse> mark(@RequestBody MarkRequest request) {
        MarkResponse response = this.bingoService.mark(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{playerId}/bingo")
    public ResponseEntity<BingoResponse> bingo(@PathVariable("playerId") UUID playerId) {
        BingoResponse response = this.bingoService.bingo(playerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{playerId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable("playerId") UUID playerId) {
        GameResponse response = this.bingoService.getGame(playerId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/admin")
    public ResponseEntity<AdminGameResponse> getGame() {
        AdminGameResponse response = this.bingoService.getGame();

        return ResponseEntity.ok(response);
    }

    @GetMapping(value="/connect/players/{playerId}")
    public SseEmitter connect(
            @PathVariable("playerId") UUID playerId,
            @RequestParam(value = "isAdmin", required = false) boolean isAdmin
    ) {
        SseEmitter emitter = new SseEmitter(0L);

        this.bingoService.addListener(playerId, isAdmin, emitter);

        return emitter;
    }

    @PostMapping("/allow-list")
    public ResponseEntity<Void> allowList(@RequestBody List<String> usernames) {
        this.bingoService.setAllowList(usernames);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/game-mode")
    public ResponseEntity<Void> setGameMode(@RequestBody BingoMode bingoMode) {
        this.bingoService.setGameMode(bingoMode);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password")
    public ResponseEntity<Void> setPassword(@RequestBody PasswordRequest request) {
        this.bingoService.setPassword(request.getPassword());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/has-password")
    public ResponseEntity<HasPasswordResponse> getHasPassword() {
        HasPasswordResponse response = this.bingoService.hasPassword();

        return ResponseEntity.ok(response);
    }

}
