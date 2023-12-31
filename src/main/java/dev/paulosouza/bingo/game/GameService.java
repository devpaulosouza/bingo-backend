package dev.paulosouza.bingo.game;

import dev.paulosouza.bingo.dto.request.GameConfigRequest;
import dev.paulosouza.bingo.dto.request.GameType;
import dev.paulosouza.bingo.dto.response.GameConfigResponse;
import dev.paulosouza.bingo.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GameService {

    private final List<SseEmitter> watchers = new ArrayList<>();

    private ScheduledExecutorService pingScheduler;

    private GameType type = GameType.STOP;

    private final NotifyService notifyService;

    public SseEmitter addListener() {
        SseEmitter emitter = new SseEmitter(0L);

        emitter.onCompletion(() -> this.watchers.remove(emitter));

        this.watchers.add(emitter);
        this.startPing();

        return emitter;
    }

    public void setConfig(GameConfigRequest request) {
        if (request.getType() != null) {
            this.setGameType(request.getType());
        }
    }

    public GameConfigResponse getConfig() {
        GameConfigResponse response = new GameConfigResponse();

        response.setGameType(this.type);

        return response;
    }

    private void setGameType(GameType type) {
        this.type = type;
        try {
            this.notifyService.notifyGameTypeChanged(this.watchers, type);
        } catch (Exception ignored) {

        }
    }

    private void startPing() {
        if (this.pingScheduler != null) {
            this.pingScheduler.shutdown();
        }

        this.pingScheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            this.pingScheduler.scheduleWithFixedDelay(this::notifyPing, 0, 60, TimeUnit.SECONDS);
        } catch (Exception ignored) {

        }
    }

    private void notifyPing() {
        try {
            this.notifyService.notifyPing(this.watchers);
        } catch (Exception ignored) {

        }
    }

}
