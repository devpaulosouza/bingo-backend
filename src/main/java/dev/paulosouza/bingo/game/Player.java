package dev.paulosouza.bingo.game;

import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Data
public class Player {

    private UUID id;

    private String name;

    private String username;

    private SseEmitter emitter;

    public Player() {
        this.id = UUID.randomUUID();
    }

}
