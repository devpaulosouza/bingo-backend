package dev.paulosouza.bingo.dto.bingo.request;

import lombok.Data;

import java.util.List;

@Data
public class BingoConfigRequest {

    private BingoMode mode;

    private List<String> allowList;

    private String password;

    private Boolean kickWinner;

}
