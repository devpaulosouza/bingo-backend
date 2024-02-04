package dev.paulosouza.bingo.controller;

import dev.paulosouza.bingo.dto.response.GameConfigResponse;
import dev.paulosouza.bingo.game.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/watch")
    public SseEmitter watch() {
        return this.gameService.addListener();
    }

    @GetMapping("/config")
    public ResponseEntity<GameConfigResponse> getConfig() {
        GameConfigResponse response = this.gameService.getConfig();

        return ResponseEntity.ok(response);
    }

}
