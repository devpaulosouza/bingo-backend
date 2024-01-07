package dev.paulosouza.bingo.game.stop;

import dev.paulosouza.bingo.dto.bingo.request.PlayerRequest;
import dev.paulosouza.bingo.dto.bingo.response.StartStopResponse;
import dev.paulosouza.bingo.dto.stop.response.StopGameResponse;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class StopService {

    public static final String PLAYER_WAS_NOT_FOUND = "Player was not found";
    private int wordsCount = 7;

    private static final int CAN_STOP_SECONDS = 30;

    private static final int STOP_SECONDS = 90;

    private static final int INCREMENT_VALIDATE_WORD_SECONDS = 20;

    private final List<StopGame> games = new ArrayList<>();

    private final List<SseEmitter> admins = new ArrayList<>();

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

    private LocalDateTime canStopAt;

    private LocalDateTime stopAt;

    private int validateWordCount = -1;

    private final List<String> possibleWords = new ArrayList<>();

    private final List<String> drawnWords = new ArrayList<>();

    private final List<Player> winners = new ArrayList<>();

    private boolean validatingWords = false;

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
        this.winners.clear();
        this.validatingWords = false;

        this.games.forEach(game -> game.setWords(new String[wordsCount]));

        this.possibleWords.clear();

        this.possibleWords.addAll(List.of(
                "Programa de TV",
                "Comida",
                "Profissão",
                "CEP (Cidade, Estado, País)",
                "Animal",
                "Personagem",
                "Famosas/Famosos",
                "Esporte",
                "Cor",
                "App ou Site",
                "Livro",
                "A Saapatona é...",
                "Nome"
        ));

        this.drawnWords();

        response.setCanStopAt(LocalDateTime.now().plusSeconds(CAN_STOP_SECONDS));
        response.setStopAt(LocalDateTime.now().plusSeconds(STOP_SECONDS));

        this.canStopAt = response.getCanStopAt();
        this.stopAt = response.getStopAt();

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

        this.canStopAt = LocalDateTime.now().plusSeconds(CAN_STOP_SECONDS);
        this.stopAt = LocalDateTime.now().plusSeconds(STOP_SECONDS);

        this.notifyStart();
        this.startPing();

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
        response.setCanStopAt(this.canStopAt);
        response.setStopAt(this.stopAt);
        response.setStopped(this.isStopped);
        response.setValidatingWords(this.validatingWords);

        int count = this.validateWordCount >= game.getWords().length ? game.getWords().length - 1 : this.validateWordCount;
        StopUtils.setOtherPLayersWordsResponse(game, count, this.games, response);

        if (this.canStopAt != null) {
            response.setCanStop(this.canStopAt.isBefore(LocalDateTime.now()));
        }

        if (this.validateWordCount != -1) {
            response.setValidateWordCount(this.validateWordCount);
        }

        return response;
    }

    public void kickAll() {
        this.notifyKickAll();

        this.games.clear();
        this.drawnWords.clear();

        this.isAcceptingNewPlayers = true;
        this.isGameRunning = false;

        this.letter = null;


        if (this.schedulerStop != null) {
            this.schedulerStop.shutdown();
        }

        if (this.schedulerRestart != null) {
            this.schedulerRestart.shutdown();
        }

        if (this.schedulerPing != null) {
            this.schedulerPing.shutdown();
        }

        if (this.schedulerCanStop != null) {
            this.schedulerCanStop.shutdown();
        }

        if (this.schedulerValidateWord != null) {
            this.schedulerValidateWord.shutdown();
        }
    }

    public StopGameResponse getGame() {
        StopGameResponse response = new StopGameResponse();

        response.setGames(this.games);
        response.setWinners(this.winners);
        response.setDrawnWords(this.drawnWords);
        response.setLetter(this.letter);

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

    public synchronized boolean stop(UUID playerId) {
        String playerName = null;

        Optional<StopGame> optionalGame = this.games
                .stream()
                .filter(game -> game.getPlayer().getId().equals(playerId))
                .findFirst();

        if (optionalGame.isPresent()) {
            StopGame game = optionalGame.get();

            if (Arrays.stream(game.getWords()).anyMatch(Objects::isNull)) {
                return false;
            }

            Player player = game.getPlayer();

            playerName = player.getName();
            log.info("stopped by = {}", player.getUsername());
        }

        this.validateGameIsRunning();
        this.validateIsNotStopped();
        this.stop();

        this.notifyStopped(playerName);

        return true;
    }

    public synchronized void setValidWord(StopValidateWordRequest request) {
        this.validateGameIsRunning();
        this.validateIsStopped();

        StopGame game = this.games.stream().filter(g -> g.getPosition() == request.getPlayerPosition())
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

        try {
            this.schedulerPing = Executors.newSingleThreadScheduledExecutor();
            this.schedulerPing.scheduleWithFixedDelay(this::notifyPing, CAN_STOP_SECONDS, CAN_STOP_SECONDS, TimeUnit.SECONDS);
            log.info("time {}", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        } catch (Exception ignored) {

        }
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
        if (
                !word
                .toUpperCase()
                .replace("Ã", "A")
                .replace("Õ", "O")
                .replace("Ç", "C")
                .replace("Á", "A")
                .replace("Ó", "O")
                .replace("Ê", "E")
                .replace("É", "E")
                .replace("Ú", "U")
                .startsWith(this.letter + "")
        ) {
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

    private void notifyStopped(String playerName) {
        SseUtils.broadcastStopStoppedMessage(
                SseUtils.mapStopEmitters(this.games, this.admins),
                playerName
        );
    }

    private void notifyKickAll() {
        SseUtils.broadcastKickAll(
                SseUtils.mapStopEmitters(this.games, this.admins)
        );
    }

    private void notifyValidateWord(int validateWordCount) {
        SseUtils.broadcastValidateWord(
                SseUtils.mapStopEmitters(this.games, this.admins),
                validateWordCount
        );
    }

    private void notifyWinner(Player player) {
        SseUtils.broadcastWinner(
                SseUtils.mapStopEmitters(this.games, this.admins),
                player
        );
    }

    private void notifyRestart() {
        SseUtils.broadcastStopRestart(
                SseUtils.mapStopEmitters(this.games, this.admins)
        );
    }

    private void notifyPing() {
        log.info("time {}", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        SseUtils.broadcastPing(
                SseUtils.mapStopEmitters(this.games, this.admins)
        );
    }

    private void drawnWords() {
        for (int i = 0; i < this.wordsCount; i++) {
            String word = ListUtils.chooseWord(this.possibleWords);
            log.info("Drawn word {}", word);
            this.drawnWords.add(word);
        }
    }

    private void stop() {
        log.info("stopped");
        this.isStopped = true;

        if (this.schedulerValidateWord != null) {
            this.schedulerValidateWord.shutdown();
        }
        if (this.schedulerStop != null) {
            this.schedulerStop.shutdown();
        }

        this.schedulerValidateWord = Executors.newSingleThreadScheduledExecutor();
        this.schedulerValidateWord.scheduleWithFixedDelay(this::incrementValidateWordCount, INCREMENT_VALIDATE_WORD_SECONDS, INCREMENT_VALIDATE_WORD_SECONDS, TimeUnit.SECONDS);

        this.notifyStopped(null);
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
        this.validatingWords = true;
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
        List<StopGame> winnerList = StopUtils.checkWinner(this.games);

        log.info("Winners size = {}", winnerList.size());

        if (winnerList.size() == 1) {
            this.notifyWinner(winnerList.get(0).getPlayer());
            winnerList.forEach(game -> log.info("Winner = {}", game.getPlayer().getUsername()));
            this.winners.addAll(winnerList.stream().map(StopGame::getPlayer).toList());
        } else {
            this.games.removeIf(game -> !winnerList.contains(game));
            this.restart();
            winnerList.forEach(game -> log.info("Draw = {}", game.getPlayer().getUsername()));
        }

    }
}
