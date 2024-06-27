package dev.paulosouza.bingo.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class PlayerResponse {

    private UUID id;

    private String name;

    private String username;

}
