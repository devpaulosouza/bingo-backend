package dev.paulosouza.bingo.dto.stop.request;

import lombok.Data;

import java.util.List;

@Data
public class StopConfigRequest {

    private List<String> allowList;

    private String password;

    private Boolean kickWinner;

}
