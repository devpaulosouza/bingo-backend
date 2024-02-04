package dev.paulosouza.bingo.controller.admin;

import dev.paulosouza.bingo.dto.bingo.response.StartStopResponse;
import dev.paulosouza.bingo.dto.stop.request.StopConfigRequest;
import dev.paulosouza.bingo.dto.stop.response.StopGameResponse;
import dev.paulosouza.bingo.game.stop.StopService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/games/admin/stop")
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
public class AdminStopController {

    private final StopService stopService;

    @GetMapping("")
    public ResponseEntity<StopGameResponse> getConfig() {
        StopGameResponse response = this.stopService.getGame();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    public ResponseEntity<StartStopResponse> start() {
        StartStopResponse response = this.stopService.start();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/config")
    public ResponseEntity<Void> setConfig(@RequestBody StopConfigRequest request) {
        this.stopService.setConfig(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("kick-all")
    public ResponseEntity<Void> kickAll() {
        this.stopService.kickAll();

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value="/connect/players/{playerId}")
    public SseEmitter connect(@PathVariable("playerId") UUID playerId) {
        return this.stopService.addListener(playerId, true);
    }

}
