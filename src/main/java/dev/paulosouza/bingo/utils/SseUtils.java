package dev.paulosouza.bingo.utils;

import dev.paulosouza.bingo.dto.bingo.response.sse.*;
import dev.paulosouza.bingo.dto.bingo.request.BingoMode;
import dev.paulosouza.bingo.game.bingo.Card;
import dev.paulosouza.bingo.game.bingo.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class SseUtils {

    private SseUtils() {

    }

    public static List<SseEmitter> mapEmitters(List<Card> cards, List<SseEmitter> admins) {
        return Stream.concat(
                        cards
                                .stream()
                                .map(Card::getPlayer)
                                .map(Player::getEmitter),
                        admins.stream()
                )
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

    public static void broadcastClean(List<SseEmitter> emitters) {
        emitters.forEach(SseUtils::sendCleanMessage);
    }

    public static void broadcastMarked(List<SseEmitter> emitters, MarkedResponse markedResponse) {
        emitters.forEach(emitter -> SseUtils.sendMarkedMessage(emitter, markedResponse));
    }

    public static void broadcastJoin(List<SseEmitter> emitters, Card card) {
        emitters.forEach(emitter -> SseUtils.sendJoinMessage(emitter, card));
    }

    public static void broadcastGameMode(List<SseEmitter> emitters, BingoMode mode) {
        emitters.forEach(emitter -> SseUtils.sendGameModeMessage(emitter, mode));
    }

    public static void broadcastPing(List<SseEmitter> emitters) {
        emitters.forEach(SseUtils::sendPingMessage);
    }

    private static void sendStartedMessage(SseEmitter emitter) {
        try {
            emitter.send(new StartedResponse(true));
        } catch (Exception e) {
            log.error("Error sending start message: {}", e.getMessage());
        }
    }

    private static void sendDrawnNumberMessage(SseEmitter emitter, DrawnNumberResponse response) {
        try {
            emitter.send(response);
        } catch (Exception e) {
            log.error("Error sending drawn number message: {}", e.getMessage());
        }
    }

    private static void sendWinnerMessage(SseEmitter emitter, Player player) {
        try {
            emitter.send(new WinnerResponse(player.getId(), player.getName()));
        } catch (Exception e) {
            log.error("Error sending winner message: {}", e.getMessage());
        }
    }

    private static void sendCleanMessage(SseEmitter emitter) {
        try {
            emitter.send(new CleanResponse());
        } catch (Exception e) {
            log.error("Error sending clean message: {}", e.getMessage());
        }
    }

    private static void sendMarkedMessage(SseEmitter emitter, MarkedResponse response) {
        try {
            emitter.send(response);
        } catch (Exception e) {
            log.error("Error sending marked message: {}", e.getMessage());
        }
    }

    private static void sendJoinMessage(SseEmitter emitter, Card card) {
        try {
            emitter.send(card);
        } catch (Exception e) {
            log.error("Error sending join message: {}", e.getMessage());
        }
    }

    private static void sendGameModeMessage(SseEmitter emitter, BingoMode mode) {
        try {
            emitter.send(new GameModeResponse(mode));
        } catch (Exception e) {
            log.error("Error sending game mode message: {}", e.getMessage());
        }
    }

    private static void sendPingMessage(SseEmitter emitter) {
        try {
            emitter.send(new PingResponse());
        } catch (Exception e) {
            log.error("Error sending ping message: {}", e.getMessage());
        }
    }

}
