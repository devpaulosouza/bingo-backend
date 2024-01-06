package dev.paulosouza.bingo.game;

import dev.paulosouza.bingo.dto.request.GameConfigRequest;
import dev.paulosouza.bingo.dto.request.GameType;
import dev.paulosouza.bingo.dto.response.GameConfigResponse;
import dev.paulosouza.bingo.utils.SseUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class GameService {

    private final List<SseEmitter> watchers = new ArrayList<>();

    private ScheduledExecutorService pingScheduler;

    private GameType type = GameType.BINGO;

    public SseEmitter addListener() {
        SseEmitter emitter = new SseEmitter(0L);

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
        this.notifyGameTypeChanged(type);
    }

    private void startPing() {
        if (this.pingScheduler != null) {
            this.pingScheduler.shutdown();
        }

        this.pingScheduler = Executors.newSingleThreadScheduledExecutor();
        this.pingScheduler.scheduleWithFixedDelay(this::notifyPing, 0, 60, TimeUnit.SECONDS);
    }

    private void notifyPing() {
        SseUtils.broadcastPing(
                SseUtils.mapEmitters(new ArrayList<>(), this.watchers)
        );
    }

    private void notifyGameTypeChanged(GameType type) {
        SseUtils.broadcastGameType(
                SseUtils.mapEmitters(new ArrayList<>(), this.watchers),
                type
        );
    }
}
