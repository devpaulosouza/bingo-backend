package dev.paulosouza.bingo.controller;

import dev.paulosouza.bingo.dto.bingo.request.PlayerRequest;
import dev.paulosouza.bingo.dto.bingo.response.StartStopResponse;
import dev.paulosouza.bingo.dto.stop.request.StopConfigRequest;
import dev.paulosouza.bingo.dto.stop.request.StopSetWordRequest;
import dev.paulosouza.bingo.dto.stop.request.StopValidateWordRequest;
import dev.paulosouza.bingo.dto.stop.response.StopGameResponse;
import dev.paulosouza.bingo.dto.stop.response.StopPlayerGameResponse;
import dev.paulosouza.bingo.game.stop.StopGame;
import dev.paulosouza.bingo.game.stop.StopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/games/stop")
@RequiredArgsConstructor
public class StopController {

    private final StopService stopService;

    @PostMapping("/join")
    public ResponseEntity<StopGame> join(@RequestBody PlayerRequest request) {
        StopGame response = this.stopService.join(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    public ResponseEntity<StartStopResponse> start() {
        StartStopResponse response = this.stopService.start();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{playerId}/stop")
    public ResponseEntity<Boolean> stop(@PathVariable("playerId") UUID playerId) {
        boolean stop = this.stopService.stop(playerId);

        return ResponseEntity.ok(stop);
    }

    @PostMapping("/users/{playerId}/validate-word")
    public ResponseEntity<Void> validateWord(@RequestBody StopValidateWordRequest request) {
        this.stopService.setValidWord(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{playerId}/set-word")
    public ResponseEntity<Void> validateWord(@RequestBody StopSetWordRequest request) {
        this.stopService.setWord(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/config")
    public ResponseEntity<Void> setConfig(@RequestBody StopConfigRequest request) {
        this.stopService.setConfig(request);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{playerId}")
    public ResponseEntity<StopPlayerGameResponse> getConfig(@PathVariable("playerId") UUID playerId) {
        StopPlayerGameResponse response = this.stopService.getGame(playerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<StopGameResponse> getConfig() {
        StopGameResponse response = this.stopService.getGame();

        return ResponseEntity.ok(response);
    }


    @PostMapping("kick-all")
    public ResponseEntity<Void> kickAll() {
        this.stopService.kickAll();

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value="/connect/players/{playerId}")
    public SseEmitter connect(
            @PathVariable("playerId") UUID playerId,
            @RequestParam(value = "isAdmin", required = false) boolean isAdmin
    ) {
        return this.stopService.addListener(playerId, isAdmin);
    }

}
