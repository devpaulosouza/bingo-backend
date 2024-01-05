package dev.paulosouza.bingo.mapper;

import dev.paulosouza.bingo.dto.bingo.request.PlayerRequest;
import dev.paulosouza.bingo.game.Player;
import org.springframework.beans.BeanUtils;

public class PlayerMapper {

    private PlayerMapper() {

    }

    public static Player toEntity(PlayerRequest request) {
        Player entity = new Player();

        BeanUtils.copyProperties(request, entity);

        return entity;
    }

}
