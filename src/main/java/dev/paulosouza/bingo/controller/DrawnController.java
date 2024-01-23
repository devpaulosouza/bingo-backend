package dev.paulosouza.bingo.controller;

import dev.paulosouza.bingo.dto.drawn.response.DrawnResponse;
import dev.paulosouza.bingo.game.drawn.DrawnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/games/drawn")
@RequiredArgsConstructor
public class DrawnController {

    private final DrawnService drawnService;

    @GetMapping
    public ResponseEntity<DrawnResponse> getGame() {
        DrawnResponse response = this.drawnService.getGame();

        return ResponseEntity.ok(response);
    }

    @GetMapping(value="/connect/players/{playerId}")
    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(0L);

        this.drawnService.addListener(true, emitter);

        return emitter;
    }

}
