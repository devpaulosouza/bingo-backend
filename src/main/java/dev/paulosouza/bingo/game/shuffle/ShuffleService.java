package dev.paulosouza.bingo.game.shuffle;

import dev.paulosouza.bingo.dto.bingo.request.PlayerRequest;
import dev.paulosouza.bingo.dto.bingo.response.HasPasswordResponse;
import dev.paulosouza.bingo.dto.shuffle.request.ShuffleConfigRequest;
import dev.paulosouza.bingo.dto.shuffle.request.ShuffleRequest;
import dev.paulosouza.bingo.dto.shuffle.request.ShuffleStartRequest;
import dev.paulosouza.bingo.dto.shuffle.response.ShuffleGamePlayerResponse;
import dev.paulosouza.bingo.dto.shuffle.response.ShuffleGameResponse;
import dev.paulosouza.bingo.exception.UnprocessableEntityException;
import dev.paulosouza.bingo.service.NotifyService;
import dev.paulosouza.bingo.utils.SseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ShuffleService {

    public static final String PLAYER_WAS_NOT_FOUND = "Player was not found";
    private final List<ShufflePlayer> players = new ArrayList<>();

    private final List<ShufflePlayer> winners = new ArrayList<>();

    private final List<SseEmitter> admins = new ArrayList<>();

    private final NotifyService notifyService;

    private String[] words;

    private String[] shuffledWords;

    private int totalWinners;

    private boolean hasPassword = false;

    private String password = "";

    private boolean isGameRunning = false;

    private boolean isAcceptingNewPlayers = true;

    private boolean isStoppedByWinner = false;

    private LocalDateTime startAt;

    private ScheduledExecutorService pingScheduler;

    public synchronized ShuffleGamePlayerResponse join(PlayerRequest request) {
        this.validatePassword(request.getPassword());

        Optional<ShufflePlayer> existent = this.players.stream()
                .filter(p -> p.getUsername().equals(request.getUsername()))
                .findFirst();

        if (existent.isPresent()) {
            existent.get().setId(request.getId());
            return this.getGame(request.getId());
        }

        this.validateJoin();

        ShufflePlayer card = ShufflePlayer.builder()
                .id(request.getId())
                .username(request.getUsername())
                .name(request.getName())
                .focused(true)
                .words(new String[this.shuffledWords.length])
                .build();

        this.players.add(card);
        this.notifyService.notifyJoin(SseUtils.mapShuffleEmitters(this.players, this.admins), null);

        return this.getGame(request.getId());
    }

    public void setUnfocused(UUID playerId) {
        if (!this.isGameRunning) {
            return;
        }

        ShufflePlayer player = this.players.stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException(PLAYER_WAS_NOT_FOUND));

        player.setFocused(false);

        this.notifyService.notifyUnfocused(this.admins, player.getId());
    }

    public synchronized void addListener(UUID playerId, boolean isAdmin, SseEmitter emitter) {
        if (isAdmin) {
            this.admins.add(emitter);
            emitter.onCompletion(() -> this.removeListener(emitter));
        } else {
            ShufflePlayer player = this.players.stream()
                    .filter(p -> p.getId().equals(playerId))
                    .findFirst()
                    .orElseThrow(() -> new UnprocessableEntityException(PLAYER_WAS_NOT_FOUND));

            player.setEmitter(emitter);
            emitter.onCompletion(() -> player.setEmitter(null));
        }
        this.startPing();
    }

    public void start(ShuffleStartRequest request) {
        this.isAcceptingNewPlayers = true;
        this.isGameRunning = true;
        this.totalWinners = request.getTotalWinners();
        this.words = request.getWords();
        this.shuffledWords = new String[request.getWords().length];
        this.winners.clear();
        this.isStoppedByWinner = false;
        this.startAt = LocalDateTime.now().plusMinutes(request.getStartAtMinutes());

        this.players.forEach(p -> p.setFocused(true));
        this.players.forEach(p -> p.setWords(new String[request.getWords().length]));

        for (int i = 0; i < request.getWords().length; ++i) {
            this.shuffledWords[i] = this.shuffle(request.getWords()[i]);
        }

        this.notifyService.notifyStart(SseUtils.mapShuffleEmitters(this.players, this.admins));
    }

    public void kickAll() {
        this.isAcceptingNewPlayers = false;
        this.isGameRunning = false;
        this.totalWinners = 0;
        this.words = new String[0];
        this.shuffledWords = new String[0];
        this.winners.clear();
        this.players.clear();

        this.notifyService.notifyKickAll(SseUtils.mapShuffleEmitters(this.players, this.admins));
    }

    public synchronized ShuffleGamePlayerResponse setWords(UUID playerId, ShuffleRequest request) {
        if (!this.isGameRunning) {
            throw new UnprocessableEntityException("Game is not running");
        }

        ShufflePlayer player = this.players.stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException(PLAYER_WAS_NOT_FOUND));

        if (!player.isFocused()) {
            throw new UnprocessableEntityException("Player lost the game because is unfocused");
        }

        player.setWords(request.getWords());

        boolean isWinner = Stream.of(request.getWords()).map(String::toLowerCase).toList().equals(Stream.of(this.words).map(String::toLowerCase).toList());

        if (isWinner) {
            this.winners.add(player);
            this.notifyService.notifyWinner(SseUtils.mapShuffleEmitters(this.players, this.admins));
        }

        if (isWinner && this.totalWinners == this.winners.size()) {
            this.isGameRunning = false;
            this.isStoppedByWinner = true;
        }

        boolean[] validWords = new boolean[request.getWords().length];

        for (int i = 0; i < request.getWords().length; ++i) {
            validWords[i] = request.getWords()[i].equalsIgnoreCase(this.words[i]);
        }

        return ShuffleGamePlayerResponse.builder()
                .isGameRunning(this.isGameRunning)
                .winners(this.winners)
                .words(player.getWords())
                .validWords(validWords)
                .isWinner(this.winners.stream().anyMatch(p -> p.getId().equals(playerId)))
                .build();
    }

    public ShuffleGameResponse getGame() {
        return ShuffleGameResponse.builder()
                .players(this.players)
                .winners(this.winners)
                .words(this.words)
                .isGameRunning(this.isGameRunning)
                .shuffledWords(this.shuffledWords)
                .playersCount(this.players.size())
                .startAt(this.startAt)
                .isStarted(Objects.nonNull(this.startAt) && startAt.isBefore(LocalDateTime.now()))
                .build();
    }

    public ShuffleGamePlayerResponse getGame(UUID playerId) {
        ShufflePlayer player = this.players.stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException(PLAYER_WAS_NOT_FOUND));

        return ShuffleGamePlayerResponse.builder()
                .winners(this.winners)
                .words(player.getWords())
                .shuffledWords(this.shuffledWords)
                .isGameRunning(this.isGameRunning)
                .isWinner(this.winners.stream().anyMatch(p -> p.getId().equals(playerId)))
                .focused(player.isFocused())
                .playersCount(this.players.size())
                .startAt(this.startAt)
                .isStarted(Objects.nonNull(this.startAt) && startAt.isBefore(LocalDateTime.now()))
                .build();
    }

    public void setConfig(ShuffleConfigRequest request) {
        this.setPassword(request.getPassword());
    }

    public HasPasswordResponse hasPassword() {
        HasPasswordResponse response = new HasPasswordResponse();

        response.setHasPassword(this.hasPassword);

        return response;
    }

    private void validateJoin() {
        if (this.isStoppedByWinner) {
            throw new UnprocessableEntityException("The game already has winners");
        }
        if (!this.isAcceptingNewPlayers) {
            throw new UnprocessableEntityException("Game is not accepting players");
        }
        if(!this.isGameRunning) {
            throw new UnprocessableEntityException("Game is not running");
        }
    }


    private void validatePassword(String password) {
        if (this.hasPassword && !this.password.equalsIgnoreCase(password)) {
            throw new UnprocessableEntityException("Password does not match");
        }
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
        this.pingScheduler.scheduleWithFixedDelay(() -> this.notifyService.notifyPing(SseUtils.mapShuffleEmitters(this.players, this.admins)), 0, 10, TimeUnit.SECONDS);
    }

    private String shuffle(String word) {
        List<Character> l = new ArrayList<>();

        for(char c :  word.toCharArray()) {
            l.add(c);
        }

        Collections.shuffle(l);

        StringBuilder sb = new StringBuilder();

        for(char c : l) {
            sb.append(c);
        }

        return sb.toString();
    }

}
