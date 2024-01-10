package dev.paulosouza.bingo.controller;

import dev.paulosouza.bingo.dto.bingo.response.AdminGameResponse;
import dev.paulosouza.bingo.game.bingo.BingoService;
import dev.paulosouza.bingo.game.stop.StopService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/games/admin/bingo")
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
public class AdminBingoController {

    private final BingoService bingoService;

    private final StopService stopService;


    @GetMapping
    public ResponseEntity<AdminGameResponse> getGame() {
        AdminGameResponse response = this.bingoService.getGame();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    public ResponseEntity<Void> start() {
        this.bingoService.startGame();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clean")
    public ResponseEntity<Void> clean() {
        this.bingoService.clean();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/kick-all")
    public ResponseEntity<Void> kickAll() {
        this.bingoService.kickAll();

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value="/connect/players/{playerId}")
    public SseEmitter connect(@PathVariable("playerId") UUID playerId) {
        SseEmitter emitter = new SseEmitter(0L);

        this.bingoService.addListener(playerId, true, emitter);

        return emitter;
    }

}
