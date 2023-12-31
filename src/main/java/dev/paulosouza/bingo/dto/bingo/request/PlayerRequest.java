package dev.paulosouza.bingo.dto.bingo.request;

import lombok.Data;

import java.util.UUID;

@Data
public class PlayerRequest {

    private UUID id;

    private String name;

    private String username;

    private String password;

}
