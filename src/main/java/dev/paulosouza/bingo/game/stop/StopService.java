package dev.paulosouza.bingo.game.stop;

import dev.paulosouza.bingo.dto.bingo.request.PlayerRequest;
import dev.paulosouza.bingo.dto.bingo.response.StartStopResponse;
import dev.paulosouza.bingo.dto.stop.request.StopConfigRequest;
import dev.paulosouza.bingo.dto.stop.request.StopSetWordRequest;
import dev.paulosouza.bingo.dto.stop.request.StopValidateWordRequest;
import dev.paulosouza.bingo.dto.stop.response.StopPlayerGameResponse;
import dev.paulosouza.bingo.exception.UnprocessableEntityException;
import dev.paulosouza.bingo.game.Player;
import dev.paulosouza.bingo.mapper.PlayerMapper;
import dev.paulosouza.bingo.utils.ListUtils;
import dev.paulosouza.bingo.utils.SseUtils;
import dev.paulosouza.bingo.utils.StopUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class StopService {

    public static final String PLAYER_WAS_NOT_FOUND = "Player was not found";
    private int wordsCount = 7;

    private static final int CAN_STOP_SECONDS = 20;

    private static final int STOP_SECONDS = 90;

    private static final int INCREMENT_VALIDATE_WORD_SECONDS = 20;

    private List<StopGame> games = new ArrayList<>();

    private List<SseEmitter> admins = new ArrayList<>();

    private final List<String> allowList = new ArrayList<>();

    private boolean kickWinner = true;
    private boolean hasPassword = true;

    private boolean isAcceptingNewPlayers = true;

    private boolean isGameRunning = true;

    private boolean canStop = false;

    private boolean isStopped = false;

    private String password = "sapa1";

    private Character letter = null;

    private final Random random = new SecureRandom();

    private ScheduledExecutorService schedulerCanStop;

    private ScheduledExecutorService schedulerStop;

    private ScheduledExecutorService schedulerValidateWord;

    private ScheduledExecutorService schedulerRestart;

    private ScheduledExecutorService schedulerPing;

    private int validateWordCount = -1;

    private List<String> possibleWords = new ArrayList<>();

    private List<String> drawnWords = new ArrayList<>();


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

        game.setWords(new String[wordsCount]);

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
        this.validateWordCount = -1;
        this.drawnWords.clear();

        this.possibleWords.addAll(List.of(
                "Programa de TV",
                "Comida",
                "Profissão",
                "País",
                "Animal",
                "Personagem",
                "Ator/Atriz",
                "Esporte",
                "Cor",
                "App ou Site",
                "Carro",
                "Nome",
                "Profissão"
        ));

        this.drawnWords();

        response.setCanStopAt(LocalDateTime.now().plusSeconds(CAN_STOP_SECONDS));
        response.setEndAt(LocalDateTime.now().plusSeconds(STOP_SECONDS));

        if (this.schedulerCanStop != null) {
            this.schedulerCanStop.shutdown();
        }
        if (this.schedulerStop != null) {
            this.schedulerStop.shutdown();
        }
        if (this.schedulerRestart != null) {
            this.schedulerRestart.shutdown();
        }

        this.schedulerCanStop = Executors.newSingleThreadScheduledExecutor();
        this.schedulerStop = Executors.newSingleThreadScheduledExecutor();

        this.schedulerCanStop.scheduleWithFixedDelay(this::setCanStop, CAN_STOP_SECONDS, CAN_STOP_SECONDS, TimeUnit.SECONDS);
        this.schedulerStop.scheduleWithFixedDelay(this::stop, STOP_SECONDS, STOP_SECONDS, TimeUnit.SECONDS);

        this.notifyStart();

        return response;
    }

    public StopPlayerGameResponse getGame(UUID playerId) {
        StopGame game = this.games.stream()
                .filter(g -> g.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException(PLAYER_WAS_NOT_FOUND));

        StopPlayerGameResponse response = new StopPlayerGameResponse();

        response.setLetter(this.letter);
        response.setWords(game.getWords());
        response.setDrawnWords(this.drawnWords);

        return response;
    }

    public SseEmitter addListener(UUID playerId, boolean isAdmin) {
        SseEmitter emitter = new SseEmitter(0L);

        if (isAdmin) {
            this.admins.add(emitter);
        } else {
            Player player = this.games.stream()
                    .filter(g -> g.getPlayer().getId().equals(playerId))
                    .findFirst()
                    .orElseThrow(() -> new UnprocessableEntityException(PLAYER_WAS_NOT_FOUND))
                    .getPlayer();

            player.setEmitter(emitter);
        }
        this.startPing();

        return emitter;
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
        if (request.getWordsCount() != null) {
            this.wordsCount = request.getWordsCount();
        }
    }

    public synchronized void stop(UUID playerId) {
        log.info("stopped by = {}", playerId);
        this.validateGameIsRunning();
        this.validateIsNotStopped();
        this.stop();


        if (this.schedulerValidateWord != null) {
            this.schedulerValidateWord.shutdown();
        }

        this.schedulerValidateWord = Executors.newSingleThreadScheduledExecutor();
        this.schedulerValidateWord.scheduleWithFixedDelay(this::incrementValidateWordCount, INCREMENT_VALIDATE_WORD_SECONDS, INCREMENT_VALIDATE_WORD_SECONDS, TimeUnit.SECONDS);

        this.notifyStopped(playerId);
    }

    public synchronized void setValidWord(StopValidateWordRequest request) {
        this.validateGameIsRunning();
        this.validateIsStopped();

        StopGame game = this.games.stream().filter(g -> g.getPlayer().getId().equals(request.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException(PLAYER_WAS_NOT_FOUND));

        int position = request.getPosition();

        if (request.isValid()) {
            game.getValidWords()[position] = Math.min(10, game.getValidWords()[position] + 1);
        } else {
            game.getValidWords()[position] = Math.max(0, game.getValidWords()[position] - 1);
        }
    }

    public void setWord(StopSetWordRequest request) {
        this.validateSetWord(request);

        StopGame game = this.games.stream().filter(g -> g.getPlayer().getId().equals(request.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException(PLAYER_WAS_NOT_FOUND));

        game.getWords()[request.getPosition()] = request.getWord();
    }

    private void startPing() {
        if (this.schedulerPing != null) {
            this.schedulerPing.shutdown();
        }

        this.schedulerPing = Executors.newSingleThreadScheduledExecutor();
        this.schedulerPing.scheduleWithFixedDelay(this::notifyPing, 0, 10, TimeUnit.SECONDS);
    }

    private void validateSetWord(StopSetWordRequest request) {
        this.validateGameIsRunning();
        this.validateIsNotStopped();
        this.validateWordPosition(request.getPosition());
        this.validateWord(request.getWord());
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

    private void validateWordPosition(int position) {
        if (position >= wordsCount) {
            throw new UnprocessableEntityException("Invalid word position");
        }
    }

    private void validateWord(String word) {
        if (!word.startsWith(this.letter + "")) {
            throw new UnprocessableEntityException("Word must start with the drawn letter");
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

    private void notifyStart() {
        SseUtils.broadcastStartStopMessage(
                SseUtils.mapStopEmitters(this.games, this.admins)
        );
    }

    private void notifyStopped(UUID playerId) {

    }

    private void notifyStopped() {

    }

    private void notifyValidateWord(int validateWordCount) {

    }

    private void notifyWinner(Player player) {

    }

    private void notifyRestart() {

    }

    private void notifyPing() {
        SseUtils.broadcastPing(
                SseUtils.mapStopEmitters(this.games, this.admins)
        );
    }

    private void drawnWords() {
        for (int i = 0; i < this.wordsCount; i++) {
            String word = ListUtils.chooseWord(this.possibleWords);
            this.drawnWords.add(word);
        }
    }

    private void stop() {
        log.info("stopped");
        this.isStopped = true;
        if (this.schedulerStop != null) {
            this.schedulerStop.shutdown();
        }
    }

    private void restart() {
        if (this.schedulerRestart != null) {
            this.schedulerRestart.shutdown();
        }

        this.notifyRestart();

        this.schedulerRestart = Executors.newSingleThreadScheduledExecutor();
        this.schedulerRestart.scheduleWithFixedDelay(this::start, INCREMENT_VALIDATE_WORD_SECONDS, INCREMENT_VALIDATE_WORD_SECONDS, TimeUnit.SECONDS);

    }

    private void incrementValidateWordCount() {
        this.validateWordCount++;

        if (this.validateWordCount >= wordsCount) {
            this.schedulerValidateWord.shutdown();
            this.finish();
            return;
        }

        log.info("incrementing validate word count = {}", this.validateWordCount);
        this.notifyValidateWord(this.validateWordCount);
    }

    private void finish() {
        List<StopGame> winners = StopUtils.checkWinner(this.games);

        log.info("Winners size = {}", winners.size());

        if (winners.size() == 1) {
            this.notifyWinner(winners.get(0).getPlayer());
            winners.forEach(game -> log.info("Winner = {}", game.getPlayer().getUsername()));
        } else {
            this.games.removeIf(game -> !winners.contains(game));
            this.restart();
            winners.forEach(game -> log.info("Draw = {}", game.getPlayer().getUsername()));
        }


    }
}
