package dev.paulosouza.bingo.controller.admin;

import dev.paulosouza.bingo.dto.request.GameConfigRequest;
import dev.paulosouza.bingo.game.GameService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games/config/admin")
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
public class AdminConfig {

    private final GameService gameService;

    @PostMapping("/config")
    public ResponseEntity<Void> setConfig(GameConfigRequest request) {
        this.gameService.setConfig(request);

        return ResponseEntity.noContent().build();
    }


}
