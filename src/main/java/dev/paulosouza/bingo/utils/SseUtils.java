package dev.paulosouza.bingo.utils;

import dev.paulosouza.bingo.dto.bingo.response.sse.*;
import dev.paulosouza.bingo.dto.bingo.request.BingoMode;
import dev.paulosouza.bingo.dto.request.GameType;
import dev.paulosouza.bingo.dto.sse.GameTypeResponse;
import dev.paulosouza.bingo.game.bingo.BingoCard;
import dev.paulosouza.bingo.game.Player;
import dev.paulosouza.bingo.game.stop.StopGame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class SseUtils {

    private SseUtils() {

    }

    public static List<SseEmitter> mapEmitters(List<BingoCard> cards, List<SseEmitter> admins) {
        return Stream.concat(
                        cards
                                .stream()
                                .map(BingoCard::getPlayer)
                                .map(Player::getEmitter),
                        admins.stream()
                )
                .toList();
    }

    public static List<SseEmitter> mapStopEmitters(List<StopGame> games, List<SseEmitter> admins) {
        return Stream.concat(
                        games
                                .stream()
                                .map(StopGame::getPlayer)
                                .map(Player::getEmitter),
                        admins.stream()
                )
                .toList();
    }

    public static List<SseEmitter> mapEmitters(List<BingoCard> cards) {
        return cards
                .stream()
                .map(BingoCard::getPlayer)
                .map(Player::getEmitter)
                .toList();
    }

    public static void broadcastKickAll(List<SseEmitter> emitters) {
        emitters.forEach(SseUtils::sendKickMessage);
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

    public static void broadcastJoin(List<SseEmitter> emitters, BingoCard card) {
        emitters.forEach(emitter -> SseUtils.sendJoinMessage(emitter, card));
    }

    public static void broadcastGameMode(List<SseEmitter> emitters, BingoMode mode) {
        emitters.forEach(emitter -> SseUtils.sendGameModeMessage(emitter, mode));
    }

    public static void broadcastPing(List<SseEmitter> emitters) {
        emitters.forEach(SseUtils::sendPingMessage);
    }

    public static void broadcastGameType(List<SseEmitter> emitters, GameType type) {
        emitters.forEach(emitter -> SseUtils.sendGameTypeMessage(emitter, type));
    }

    public static void broadcastStartStopMessage(List<SseEmitter> emitters) {
        emitters.forEach(SseUtils::sendStartedMessage);
    }

    private static void sendKickMessage(SseEmitter emitter) {
        try {
            emitter.send(new KickResponse());
        } catch (Exception e) {
            log.error("Error sending start message: {}", e.getMessage());
        }
    }

    private static void sendStartedMessage(SseEmitter emitter) {
        try {
            emitter.send(new StartedResponse(true));
        } catch (Exception ignored) {
        }
    }

    private static void sendDrawnNumberMessage(SseEmitter emitter, DrawnNumberResponse response) {
        try {
            emitter.send(response);
        } catch (Exception ignored) {
        }
    }

    private static void sendWinnerMessage(SseEmitter emitter, Player player) {
        try {
            emitter.send(new WinnerResponse(player.getId(), player.getName()));
        } catch (Exception ignored) {
        }
    }

    private static void sendCleanMessage(SseEmitter emitter) {
        try {
            emitter.send(new CleanResponse());
        } catch (Exception ignored) {
        }
    }

    private static void sendMarkedMessage(SseEmitter emitter, MarkedResponse response) {
        try {
            emitter.send(response);
        } catch (Exception ignored) {
        }
    }

    private static void sendJoinMessage(SseEmitter emitter, BingoCard card) {
        try {
            emitter.send(card);
        } catch (Exception ignored) {
        }
    }

    private static void sendGameModeMessage(SseEmitter emitter, BingoMode mode) {
        try {
            emitter.send(new GameModeResponse(mode));
        } catch (Exception ignored) {
        }
    }

    private static void sendPingMessage(SseEmitter emitter) {
        try {
            emitter.send(new PingResponse());
        } catch (Exception ignored) {
        }
    }

    private static void sendGameTypeMessage(SseEmitter emitter, GameType type) {
        try {
            emitter.send(new GameTypeResponse(type));
        } catch (Exception ignored) {
        }
    }
}
