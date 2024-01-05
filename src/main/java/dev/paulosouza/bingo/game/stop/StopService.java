package dev.paulosouza.bingo.game.stop;

import dev.paulosouza.bingo.dto.bingo.request.PlayerRequest;
import dev.paulosouza.bingo.dto.bingo.response.StartStopResponse;
import dev.paulosouza.bingo.dto.stop.request.StopConfigRequest;
import dev.paulosouza.bingo.dto.stop.request.StopValidateWordRequest;
import dev.paulosouza.bingo.exception.UnprocessableEntityException;
import dev.paulosouza.bingo.game.Player;
import dev.paulosouza.bingo.mapper.PlayerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class StopService {

    private static final int WORDS_COUNT = 10;

    private static final int CAN_STOP_SECONDS = 20;

    private static final int STOP_SECONDS = 90;

    private List<StopGame> games = new ArrayList<>();

    private final List<String> allowList = new ArrayList<>();

    private boolean kickWinner = true;
    private boolean hasPassword = true;

    private boolean isAcceptingNewPlayers = true;

    private boolean isGameRunning = true;

    private boolean canStop = false;

    private boolean isStopped = false;

    private String password = "sapa1";

    private char letter;

    private Random random = new SecureRandom();

    private ScheduledExecutorService schedulerCanStop;

    private ScheduledExecutorService schedulerStop;


    public synchronized StopGame join(PlayerRequest request) {
        this.validatePassword(request.getPassword());

        Optional<StopGame> existent = this.games.stream()
                .filter(game -> game.getPlayer().getUsername().equals(request.getUsername()))
                .findFirst();

        if (existent.isPresent()) {
            return existent.get();
        }

        this.validateJoin(request);

        Player player = PlayerMapper.toEntity(request);

        StopGame game = StopGame.builder()
                .id(UUID.randomUUID())
                .player(player)
                .position(this.games.stream().map(StopGame::getPosition).max(Integer::compare).map(i -> i + 1).orElse(0))
                .build();

        game.setWords(new String[WORDS_COUNT]);

        this.games.add(game);

        return game;
    }

    public StartStopResponse start() {
        StartStopResponse response = new StartStopResponse();

        this.letter = (char) (random.nextInt(26) + 65);

        log.info("Drawn letter = {}", this.letter);

        this.isAcceptingNewPlayers = false;
        this.isGameRunning = true;
        this.canStop = false;
        this.isStopped = false;

        response.setCanStopAt(LocalDateTime.now().plusSeconds(CAN_STOP_SECONDS));
        response.setEndAt(LocalDateTime.now().plusSeconds(STOP_SECONDS));

        if (this.schedulerCanStop != null) {
            this.schedulerCanStop.shutdown();
        }
        if (this.schedulerStop != null) {
            this.schedulerStop.shutdown();
        }

        this.schedulerCanStop = Executors.newSingleThreadScheduledExecutor();
        this.schedulerStop = Executors.newSingleThreadScheduledExecutor();

        this.schedulerCanStop.scheduleWithFixedDelay(this::setCanStop, CAN_STOP_SECONDS, CAN_STOP_SECONDS, TimeUnit.SECONDS);
        this.schedulerStop.scheduleWithFixedDelay(this::stop, STOP_SECONDS, STOP_SECONDS, TimeUnit.SECONDS);

        return response;
    }

    public void setConfig(StopConfigRequest request) {
        if (request.getAllowList() != null) {
            this.setAllowList(request.getAllowList());
        }
        if (request.getPassword() != null) {
            this.setPassword(request.getPassword());
        }
        if (request.getKickWinner() != null) {
            this.setKickWinner(request.getKickWinner());
        }
    }

    public synchronized void stop(UUID playerId) {
        log.info("stopped by = {}", playerId);
        this.validateGameIsRunning();
        this.validateIsNotStopped();
        this.stop();
        this.notifyStopped(playerId);
    }

    public synchronized void validateWord(StopValidateWordRequest request) {
        this.validateGameIsRunning();
        this.validateIsStopped();

        StopGame game = this.games.stream().filter(g -> g.getPlayer().getId().equals(request.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException("Player was not found"));

        int playerPosition = request.getPosition() % 10;

        game.getValidWords()[playerPosition] = request.isValid();
    }

    private void validateJoin(PlayerRequest player) {
        this.validateAcceptingNewPlayers();
        this.validateAllowList(player.getUsername());
        this.validateMaximumPlayers();
    }

    private void validateMaximumPlayers() {
        if (this.games.size() >= 50) {
            throw new UnprocessableEntityException("Max players reached");
        }
    }

    private void validateAllowList(String username) {
        if (!this.allowList.isEmpty() && !this.allowList.stream().map(String::toLowerCase).toList().contains(username.toLowerCase())) {
            throw new UnprocessableEntityException("Username is not allowed to play in this session");
        }
    }

    private void validateAcceptingNewPlayers() {
        if (!this.isAcceptingNewPlayers) {
            throw new UnprocessableEntityException("The game is not accepting new players");
        }
    }

    private void validateGameIsRunning() {
        if (!this.isGameRunning) {
            throw new UnprocessableEntityException("The game is not started");
        }
    }

    private void validateIsNotStopped() {
        if (this.isStopped) {
            throw new UnprocessableEntityException("The game is already stopped");
        }
    }

    private void validateIsStopped() {
        if (!this.isStopped) {
            throw new UnprocessableEntityException("The game is not stopped");
        }
    }

    private void validatePassword(String password) {
        if (this.hasPassword && !this.password.equals(password)) {
            throw new UnprocessableEntityException("Password does not match");
        }
    }

    private void setAllowList(List<String> usernames) {
        this.allowList.clear();
        this.allowList.addAll(usernames);
    }

    private void setPassword(String password) {
        if ("".equals(password) || password == null) {
            this.password = null;
            this.hasPassword = false;
        } else {
            this.password = password;
            this.hasPassword = true;
        }
    }

    private void setKickWinner(boolean kickWinner) {
        this.kickWinner = kickWinner;
    }

    private void setCanStop() {
        this.canStop = true;
        this.schedulerCanStop.shutdown();
    }

    private void notifyStopped(UUID playerId) {

    }

    private void notifyStopped() {

    }

    private void stop() {
        log.info("stopped");
        this.isStopped = true;
        this.schedulerStop.shutdown();
    }
}
