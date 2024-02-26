package dev.paulosouza.bingo.service;

import dev.paulosouza.bingo.dto.bingo.request.BingoMode;
import dev.paulosouza.bingo.dto.bingo.response.sse.*;
import dev.paulosouza.bingo.dto.request.GameType;
import dev.paulosouza.bingo.dto.shuffle.response.sse.UnfocusedResponse;
import dev.paulosouza.bingo.dto.stop.sse.StopCanStopMessage;
import dev.paulosouza.bingo.dto.stop.sse.response.StopRestartMessage;
import dev.paulosouza.bingo.dto.stop.sse.response.StopStoppedMessage;
import dev.paulosouza.bingo.dto.stop.sse.response.StopValidateWordMessage;
import dev.paulosouza.bingo.game.Player;
import dev.paulosouza.bingo.game.bingo.BingoCard;
import dev.paulosouza.bingo.utils.SseUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@Service
public class NotifyService {

    public void notifyPing(List<SseEmitter> emitters) {
        SseUtils.broadcast(emitters, new PingResponse());
    }

    public void notifyWinner(List<SseEmitter> emitters, Player player) {
        SseUtils.broadcast(emitters, new WinnerResponse(player.getId(), player.getName()));
    }

    public void notifyWinner(List<SseEmitter> emitters) {
        SseUtils.broadcast(emitters, new WinnerResponse());
    }

    public void notifyNumber(List<SseEmitter> emitters, List<Integer> drawnNumbers, int number) {
        SseUtils.broadcast(emitters, new DrawnNumberResponse(number, drawnNumbers));
    }

    public void notifyClean(List<SseEmitter> emitters) {
        SseUtils.broadcast(emitters, new CleanResponse());
    }

    public void notifyKickAll(List<SseEmitter> emitters) {
        SseUtils.broadcast(emitters, new KickResponse());
    }

    public void notifyMarked(List<SseEmitter> emitters, MarkedResponse markedResponse) {
        SseUtils.broadcast(emitters, markedResponse);
    }

    public void notifyJoin(List<SseEmitter> emitters, BingoCard card, int playersCount) {
        SseUtils.broadcast(emitters, new JoinResponse(card, playersCount));
    }

    public void notifyGameMode(List<SseEmitter> emitters, BingoMode mode) {
        SseUtils.broadcast(emitters, new GameModeResponse(mode));
    }

    public void notifyStart(List<SseEmitter> emitters) {
        SseUtils.broadcast(emitters, new StartedResponse(true));
    }

    public void notifyStopped(List<SseEmitter> emitters, String playerName) {
        SseUtils.broadcast(emitters, new StopStoppedMessage(playerName));
    }

    public void notifyCanStop(List<SseEmitter> emitters) {
        SseUtils.broadcast(
                emitters,
                new StopCanStopMessage()
        );
    }

    public void notifyValidateWord(List<SseEmitter> emitters, int validateWordCount) {
        SseUtils.broadcast(emitters, new StopValidateWordMessage(validateWordCount));
    }


    public void notifyRestart(List<SseEmitter> emitters) {
        SseUtils.broadcast(emitters, new StopRestartMessage());
    }

    public void notifyGameTypeChanged(List<SseEmitter> emitters, GameType type) {
        SseUtils.broadcast(
                emitters,
                type
        );
    }

    public void notifyUnfocused(List<SseEmitter> admins, UUID playerId) {
        SseUtils.broadcast(
                admins,
                new UnfocusedResponse(playerId)
        );

    }
}
