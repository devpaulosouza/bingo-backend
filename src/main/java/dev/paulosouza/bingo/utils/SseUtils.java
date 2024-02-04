package dev.paulosouza.bingo.utils;

import dev.paulosouza.bingo.game.Player;
import dev.paulosouza.bingo.game.bingo.BingoCard;
import dev.paulosouza.bingo.game.shuffle.ShufflePlayer;
import dev.paulosouza.bingo.game.stop.StopGame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class SseUtils {

    private SseUtils() {

    }

    public static List<SseEmitter> mapShuffleEmitters(List<ShufflePlayer> players, List<SseEmitter> admins) {
        return Stream.concat(
                        players
                                .stream()
                                .map(ShufflePlayer::getEmitter)
                                .filter(Objects::nonNull),
                        admins.stream()
                )
                .toList();
    }

    public static List<SseEmitter> mapEmitters(List<BingoCard> cards, List<SseEmitter> admins) {
        return Stream.concat(
                        cards
                                .stream()
                                .map(BingoCard::getPlayer)
                                .map(Player::getEmitter)
                                .filter(Objects::nonNull),
                        admins.stream()
                )
                .toList();
    }

    public static List<SseEmitter> mapStopEmitters(List<StopGame> games, List<SseEmitter> admins) {
        return Stream.concat(
                        games
                                .stream()
                                .map(StopGame::getPlayer)
                                .map(Player::getEmitter)
                                .filter(Objects::nonNull),
                        admins.stream()
                )
                .toList();
    }

    public static List<SseEmitter> mapStopEmitters(List<StopGame> games) {
        return SseUtils.mapStopEmitters(games, new ArrayList<>());
    }

    public static List<SseEmitter> mapEmitters(List<BingoCard> cards) {
        return cards
                .stream()
                .map(BingoCard::getPlayer)
                .map(Player::getEmitter)
                .toList();
    }

    public static void broadcast(List<SseEmitter> emitters, Object message) {
        for (SseEmitter emitter: emitters) {
            try {
                SseUtils.sendMessage(emitter, message);
            } catch (Exception ignored) {

            }
        }
    }

    private static void sendMessage(SseEmitter emitter, Object object) {
        try {
            emitter.send(object);
        } catch (IOException ignored) {
        }
    }

}
