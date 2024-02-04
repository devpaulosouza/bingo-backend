package dev.paulosouza.bingo.controller.admin;

import dev.paulosouza.bingo.dto.shuffle.request.ShuffleConfigRequest;
import dev.paulosouza.bingo.dto.shuffle.request.ShuffleStartRequest;
import dev.paulosouza.bingo.dto.shuffle.response.ShuffleGameResponse;
import dev.paulosouza.bingo.game.shuffle.ShuffleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/games/admin/shuffle")
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
public class AdminShuffleController {

    private final ShuffleService service;

    @PostMapping("/config")
    public ResponseEntity<Void> config(@RequestBody ShuffleConfigRequest request) {
        this.service.setConfig(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/start")
    public ResponseEntity<Void> start(@RequestBody ShuffleStartRequest request) {
        this.service.start(request);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ShuffleGameResponse> getGame() {
        ShuffleGameResponse response = this.service.getGame();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/kick-all")
    public ResponseEntity<Void> kickAll() {
        this.service.kickAll();

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value="/connect/players/{playerId}")
    public SseEmitter connect(@PathVariable("playerId") UUID playerId) {
        SseEmitter emitter = new SseEmitter(0L);

        this.service.addListener(playerId, true, emitter);

        return emitter;
    }

}
