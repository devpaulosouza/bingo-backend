package dev.paulosouza.bingo.controller;

import dev.paulosouza.bingo.dto.bingo.request.PlayerRequest;
import dev.paulosouza.bingo.dto.bingo.response.StartStopResponse;
import dev.paulosouza.bingo.dto.stop.request.StopConfigRequest;
import dev.paulosouza.bingo.dto.stop.request.StopValidateWordRequest;
import dev.paulosouza.bingo.game.stop.StopGame;
import dev.paulosouza.bingo.game.stop.StopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> stop(@PathVariable("playerId") UUID playerId) {
        this.stopService.stop(playerId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{playerId}/validate-word")
    public ResponseEntity<Void> validateWord(@RequestBody StopValidateWordRequest request) {
        this.stopService.validateWord(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/config")
    public ResponseEntity<Void> setConfig(@RequestBody StopConfigRequest request) {
        this.stopService.setConfig(request);

        return ResponseEntity.noContent().build();
    }

}
