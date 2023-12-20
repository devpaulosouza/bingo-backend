package dev.paulosouza.bingo.game;

import lombok.Data;

import java.util.UUID;

@Data
public class Player {

    private UUID id;

    public Player() {
        this.id = UUID.randomUUID();
    }

}
