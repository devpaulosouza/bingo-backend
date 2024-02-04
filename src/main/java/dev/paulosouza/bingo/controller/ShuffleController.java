package dev.paulosouza.bingo.controller;

import dev.paulosouza.bingo.dto.bingo.request.PlayerRequest;
import dev.paulosouza.bingo.dto.shuffle.request.ShuffleRequest;
import dev.paulosouza.bingo.dto.shuffle.response.ShuffleGamePlayerResponse;
import dev.paulosouza.bingo.game.shuffle.ShuffleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/games/shuffle")
@RequiredArgsConstructor
public class ShuffleController {

    private final ShuffleService service;


    @PostMapping("join")
    public ResponseEntity<ShuffleGamePlayerResponse> join(@RequestBody PlayerRequest player) {
        ShuffleGamePlayerResponse response = this.service.join(player);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value="/connect/players/{playerId}")
    public SseEmitter connect(@PathVariable("playerId") UUID playerId, @RequestParam(name = "isAdmin", required = false) boolean isAdmin) {
        SseEmitter emitter = new SseEmitter(0L);

        this.service.addListener(playerId, isAdmin, emitter);

        return emitter;
    }

    @PostMapping("/players/{playerId}/set-words")
    public ResponseEntity<ShuffleGamePlayerResponse> setWords(@PathVariable("playerId") UUID playerId, @RequestBody ShuffleRequest player) {
        ShuffleGamePlayerResponse response = this.service.setWords(playerId, player);

        return ResponseEntity.ok(response);
    }

}
