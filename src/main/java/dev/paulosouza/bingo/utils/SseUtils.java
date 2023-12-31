package dev.paulosouza.bingo.utils;

import dev.paulosouza.bingo.dto.response.sse.DrawnNumberResponse;
import dev.paulosouza.bingo.dto.response.sse.StartedResponse;
import dev.paulosouza.bingo.dto.response.sse.WinnerResponse;
import dev.paulosouza.bingo.game.Card;
import dev.paulosouza.bingo.game.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class SseUtils {

    private SseUtils() {

    }

    public static List<SseEmitter> mapEmitters(List<Card> cards) {
        return cards
                .stream()
                .map(Card::getPlayer)
                .map(Player::getEmitter)
                .toList();
    }

    public static void broadcastDrawnNumberMessage(List<SseEmitter> emitters, DrawnNumberResponse response) {
        emitters.forEach(emitter -> SseUtils.sendDrawnNumberMessage(emitter, response));
    }

    public static void broadcastStartMessage(List<SseEmitter> emitters) {
        emitters.forEach(SseUtils::sendStartedMessage);
    }

    public static void broadcastWinner(List<SseEmitter> emitters, Player player) {
        emitters.forEach(emitter -> SseUtils.sendWinnerMessage(emitter, player));
    }

    private static void sendStartedMessage(SseEmitter emitter) {
        try {
            emitter.send(new StartedResponse(true));
        } catch (IOException e) {
            log.error("Error sending start message: {}", e.getMessage());
        }
    }

    private static void sendDrawnNumberMessage(SseEmitter emitter, DrawnNumberResponse response) {
        try {
            emitter.send(response);
        } catch (IOException e) {
            log.error("Error sending drawn number message: {}", e.getMessage());
        }
    }

    private static void sendWinnerMessage(SseEmitter emitter, Player player) {
        try {
            emitter.send(new WinnerResponse(player.getId(), player.getName()));
        } catch (IOException e) {
            log.error("Error sending winner message: {}", e.getMessage());
        }
    }
}
