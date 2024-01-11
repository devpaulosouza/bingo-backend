package dev.paulosouza.bingo.game.bingo;

import dev.paulosouza.bingo.dto.bingo.request.BingoConfigRequest;
import dev.paulosouza.bingo.dto.bingo.request.BingoMode;
import dev.paulosouza.bingo.dto.bingo.request.MarkRequest;
import dev.paulosouza.bingo.dto.bingo.request.PlayerRequest;
import dev.paulosouza.bingo.dto.bingo.response.*;
import dev.paulosouza.bingo.dto.bingo.response.sse.*;
import dev.paulosouza.bingo.exception.UnprocessableEntityException;
import dev.paulosouza.bingo.game.Player;
import dev.paulosouza.bingo.mapper.PlayerMapper;
import dev.paulosouza.bingo.service.NotifyService;
import dev.paulosouza.bingo.utils.GameUtils;
import dev.paulosouza.bingo.utils.ListUtils;
import dev.paulosouza.bingo.utils.SseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BingoService {

    private boolean isAcceptingNewPlayers = true;

    private boolean isGameRunning = false;

    private final List<BingoCard> cards = new ArrayList<>();

    private final List<Player> winners = new ArrayList<>();

    private final List<SseEmitter> admins = new ArrayList<>();

    private List<Integer> possibleNumbers;

    private final List<Integer> drawnNumbers = new ArrayList<>();

    private final List<String> allowList = new ArrayList<>();

    private String password = "sapa1";

    private boolean hasPassword = true;

    private boolean kickWinner = true;

    private BingoMode mode = BingoMode.STANDARD;

    private ScheduledExecutorService scheduledExecutorService;

    private ScheduledExecutorService pingScheduler;

    private final NotifyService notifyService;

    public synchronized BingoCard join(PlayerRequest request) {
        this.validatePassword(request.getPassword());

        Player player = PlayerMapper.toEntity(request);

        Optional<BingoCard> existent = this.cards.stream()
                .filter(card -> card.getPlayer().getUsername().equals(request.getUsername()))
                .findFirst();

        if (existent.isPresent()) {
            return existent.get();
        }

        this.validateJoin(request);

        BingoCard card = BingoCard.builder()
                .id(UUID.randomUUID())
                .player(player)
                .build();

        do {
            card.setNumbers(GameUtils.drawCardNumbers());
        } while (this.cardAlreadyExists(card));

        this.cards.add(card);
        this.notifyService.notifyJoin(this.admins, card);

        return card;
    }

    public MarkResponse mark(MarkRequest request) {
        MarkResponse response = new MarkResponse();

        this.validateMark(request);

        BingoCard card = this.cards.stream()
                .filter(c -> c.getPlayer().getId().equals(request.getPlayerId()))
                .findFirst()
                .orElseThrow();

        card.getMarkedNumbers()[request.getI()][request.getJ()] = request.isMarked();

        this.notifyService.notifyMarked(this.admins, new MarkedResponse(request.getPlayerId(), request.getI(), request.getJ(), request.isMarked()));

        if (GameUtils.checkStandardWinner(card.getMarkedNumbers(), card.getNumbers(), this.drawnNumbers)) {
            response.setWinner(true);
        }

        return response;
    }

    public void startGame() {
        this.isAcceptingNewPlayers = false;
        this.isGameRunning = true;
        this.possibleNumbers = ListUtils.buildList(1, 75);
        this.drawnNumbers.clear();

        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdown();
        }

        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        this.scheduledExecutorService.scheduleWithFixedDelay(this::drawNumber, 0, 12, TimeUnit.SECONDS);

        try {
            SseUtils.broadcast(SseUtils.mapEmitters(this.cards, this.admins), new StartedResponse(true));
        } catch (Exception ignored) {

        }
    }

    public synchronized BingoResponse bingo(UUID playerId) {
        this.validateGameIsRunning();

        BingoCard card = this.cards.stream()
                .filter(c -> c.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException("Player not found"));

        boolean isWinner = BingoMode.STANDARD.equals(this.mode) ?
                GameUtils.checkStandardWinner(card.getMarkedNumbers(), card.getNumbers(), this.drawnNumbers)
                : GameUtils.checkBlackoutWinner(card.getMarkedNumbers(), card.getNumbers(), this.drawnNumbers);

        if (isWinner) {
            this.notifyService.notifyWinner(SseUtils.mapEmitters(this.cards, this.admins), card.getPlayer());
            this.isGameRunning = false;
            this.scheduledExecutorService.shutdown();
            this.winners.add(card.getPlayer());

            if (this.kickWinner) {
                this.cards.remove(card);
            }
        }

        return BingoResponse.builder()
                .winner(isWinner)
                .build();
    }

    public void clean() {
        this.isAcceptingNewPlayers = true;
        this.isGameRunning = false;
        this.cards.forEach(card -> {
            do {
                card.setNumbers(GameUtils.drawCardNumbers());
            } while (this.cardAlreadyExists(card));
        });
        this.notifyService.notifyClean(SseUtils.mapEmitters(this.cards, this.admins));
        this.drawnNumbers.clear();
        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdown();
        }
    }

    public void kickAll() {
        this.notifyService.notifyKickAll(SseUtils.mapEmitters(this.cards, this.admins));

        this.cards.clear();
        this.drawnNumbers.clear();

        this.isAcceptingNewPlayers = true;
        this.isGameRunning = false;


        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdown();
        }
    }

    public BingoGameResponse getGame(UUID playerId) {
        BingoGameResponse response = new BingoGameResponse();

        BingoCard card = this.cards.stream()
                .filter(c -> c.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException("Player was not found"));

        response.setCard(card);

        if (!this.drawnNumbers.isEmpty()) {
            response.setNumber(this.drawnNumbers.get(this.drawnNumbers.size() - 1));
            response.setDrawnNumbers(this.drawnNumbers);
        }
        response.setGameRunning(this.isGameRunning);
        response.setMode(this.mode);

        return response;
    }

    public AdminGameResponse getGame() {
        AdminGameResponse response = new AdminGameResponse();

        response.setCards(this.cards);

        if (!this.drawnNumbers.isEmpty()) {
            response.setNumber(this.drawnNumbers.get(this.drawnNumbers.size() - 1));
            response.setDrawnNumbers(this.drawnNumbers);
        }
        response.setGameRunning(this.isGameRunning);
        response.setWinners(this.winners);
        response.setMode(this.mode);

        return response;
    }

    public synchronized void addListener(UUID playerId, boolean isAdmin, SseEmitter emitter) {
        if (isAdmin) {
            this.admins.add(emitter);
            emitter.onCompletion(() -> this.removeListener(emitter));
        } else {
            Player player = this.cards.stream()
                    .filter(card -> card.getPlayer().getId().equals(playerId))
                    .findFirst()
                    .orElseThrow(() -> new UnprocessableEntityException("Player was not found"))
                    .getPlayer();

            player.setEmitter(emitter);
            emitter.onCompletion(() -> player.setEmitter(null));
        }
        this.startPing();
    }

    public void setConfig(BingoConfigRequest request) {
        if (request.getAllowList() != null) {
            this.setAllowList(request.getAllowList());
        }
        if (request.getMode() != null) {
            this.setGameMode(request.getMode());
        }
        if (request.getKickWinner() != null) {
            this.setKickWinner(request.getKickWinner());
        }
        this.setPassword(request.getPassword());
    }

    private void removeListener(SseEmitter emitter) {
        synchronized (this.admins) {
            this.admins.remove(emitter);
        }
    }
    private void setAllowList(List<String> usernames) {
        this.allowList.clear();
        this.allowList.addAll(usernames);
    }

    private void setGameMode(BingoMode mode) {
        this.mode = mode;
        this.notifyService.notifyGameMode(SseUtils.mapEmitters(this.cards, this.admins), mode);
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

    public HasPasswordResponse hasPassword() {
        HasPasswordResponse response = new HasPasswordResponse();

        response.setHasPassword(this.hasPassword);

        return response;
    }

    private void drawNumber() {
        if (possibleNumbers.isEmpty()) {
            this.scheduledExecutorService.shutdown();
            throw new UnprocessableEntityException("No more numbers to draw");
        }

        int number = ListUtils.chooseNumber(this.possibleNumbers);
        this.drawnNumbers.add(number);
        this.notifyService.notifyNumber(SseUtils.mapEmitters(this.cards, this.admins), this.drawnNumbers, number);

        log.info("drawn number = {}", number);
    }

    private void startPing() {
        if (this.pingScheduler != null) {
            this.pingScheduler.shutdown();
        }

        this.pingScheduler = Executors.newSingleThreadScheduledExecutor();
        this.pingScheduler.scheduleWithFixedDelay(() -> this.notifyService.notifyPing(SseUtils.mapEmitters(this.cards, this.admins)), 0, 10, TimeUnit.SECONDS);
    }

    private void validateMark(MarkRequest request) {
        this.validateGameIsRunning();

        if (!this.isPositionIsValid(request)) {
            throw new UnprocessableEntityException("Invalid position to mark");
        }
        if (!this.playerExists(request.getPlayerId())) {
            throw new UnprocessableEntityException("Player does not exists");
        }
    }

    private boolean playerExists(UUID playerId) {
        return this.cards.stream()
                .map(BingoCard::getPlayer)
                .map(Player::getId)
                .anyMatch(playerId::equals);
    }

    private boolean isPositionIsValid(MarkRequest request) {
        return !(request.getI() == 2 && request.getJ() == 2);
    }

    private void validateJoin(PlayerRequest player) {
        this.validateAcceptingNewPlayers();
        this.validateAllowList(player.getUsername());
        this.validateMaximumPlayers();
    }

    private void validateMaximumPlayers() {
        if (this.cards.size() >= 100) {
            throw new UnprocessableEntityException("Max players reached");
        }
    }

    private void validatePassword(String password) {
        if (this.hasPassword && !this.password.equalsIgnoreCase(password)) {
            throw new UnprocessableEntityException("Password does not match");
        }
    }

    private void validateAllowList(String username) {
        if (!this.allowList.isEmpty() && !this.allowList.stream().map(String::toLowerCase).toList().contains(username.toLowerCase())) {
            throw new UnprocessableEntityException("Username is not allowed to play in this session");
        }
    }

    private void validateAcceptingNewPlayers() {
        if (!isAcceptingNewPlayers) {
            throw new UnprocessableEntityException("The game is not accepting new players");
        }
    }

    private void validateGameIsRunning() {
        if (!isGameRunning) {
            throw new UnprocessableEntityException("The game is not started");
        }
    }


    private boolean cardAlreadyExists(BingoCard card) {
        return this.cards.stream()
                .filter(c -> !c.getId().equals(card.getId()))
                .anyMatch(c -> this.cardIsEquals(c.getNumbersList(), card.getNumbersList()));
    }

    private boolean cardIsEquals(List<Integer> playerNumbers, List<Integer> otherPlayerNumbers) {
        return playerNumbers.stream().sorted().toList().equals(otherPlayerNumbers.stream().sorted().toList());
    }
}
