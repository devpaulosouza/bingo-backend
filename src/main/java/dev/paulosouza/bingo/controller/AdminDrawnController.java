package dev.paulosouza.bingo.controller;

import dev.paulosouza.bingo.dto.drawn.request.DrawnRequest;
import dev.paulosouza.bingo.game.drawn.DrawnService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games/admin/drawn")
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
public class AdminDrawnController {

    private final DrawnService drawnService;

    @PostMapping
    public ResponseEntity<Void> drawn(@RequestBody DrawnRequest request) {
        this.drawnService.drawn(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clean")
    public ResponseEntity<Void> clean() {
        this.drawnService.clean();

        return ResponseEntity.noContent().build();
    }

}
