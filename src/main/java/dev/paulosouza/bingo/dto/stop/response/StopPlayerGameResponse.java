package dev.paulosouza.bingo.dto.stop.response;

import lombok.Data;

import java.util.List;

@Data
public class StopPlayerGameResponse {

    private String[] words;

    private List<String> drawnWords;

    private Character letter;

}
