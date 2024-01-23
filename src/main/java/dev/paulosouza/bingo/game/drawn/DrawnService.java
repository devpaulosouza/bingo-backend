package dev.paulosouza.bingo.game.drawn;

import dev.paulosouza.bingo.dto.drawn.request.DrawnRequest;
import dev.paulosouza.bingo.dto.drawn.response.DrawnResponse;
import dev.paulosouza.bingo.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrawnService {

    private final List<SseEmitter> admins = new ArrayList<>();


    private final NotifyService notifyService;

    private static final SecureRandom secureRandom = new SecureRandom();

    private ScheduledExecutorService pingScheduler;
    private Integer drawnNumber;
    public DrawnResponse getGame() {
        DrawnResponse response = new DrawnResponse();

        response.setNumber(this.drawnNumber);

        return response;
    }

    public void drawn(DrawnRequest request) {
        this.drawnNumber = secureRandom.nextInt(request.getMin(), request.getMax() + 1);

        this.notifyService.notifyPing(this.admins);
    }

    public void clean() {
        this.drawnNumber = null;

        this.notifyService.notifyPing(this.admins);
    }

    public synchronized void addListener(boolean isAdmin, SseEmitter emitter) {
        if (isAdmin) {
            this.admins.add(emitter);
            emitter.onCompletion(() -> this.removeListener(emitter));
        }

        this.startPing();
    }

    private void removeListener(SseEmitter emitter) {
        synchronized (this.admins) {
            this.admins.remove(emitter);
        }
    }

    private void startPing() {
        if (this.pingScheduler != null) {
            this.pingScheduler.shutdown();
        }

        this.pingScheduler = Executors.newSingleThreadScheduledExecutor();
        this.pingScheduler.scheduleWithFixedDelay(() -> this.notifyService.notifyPing(this.admins), 0, 10, TimeUnit.SECONDS);
    }
}
