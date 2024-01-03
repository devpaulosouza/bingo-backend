package dev.paulosouza.bingo.game;

import dev.paulosouza.bingo.dto.request.GameMode;
import dev.paulosouza.bingo.dto.request.MarkRequest;
import dev.paulosouza.bingo.dto.request.PlayerRequest;
import dev.paulosouza.bingo.dto.response.*;
import dev.paulosouza.bingo.dto.response.sse.DrawnNumberResponse;
import dev.paulosouza.bingo.dto.response.sse.MarkedResponse;
import dev.paulosouza.bingo.exception.UnprocessableEntityException;
import dev.paulosouza.bingo.mapper.PlayerMapper;
import dev.paulosouza.bingo.utils.GameUtils;
import dev.paulosouza.bingo.utils.ListUtils;
import dev.paulosouza.bingo.utils.SseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private boolean isAcceptingNewPlayers = true;

    private boolean isGameRunning = false;

    private final List<Card> cards = new ArrayList<>();

    private final List<Player> winners = new ArrayList<>();

    private final List<SseEmitter> admins = new ArrayList<>();

    private List<Integer> possibleNumbers;

    private final List<Integer> drawnNumbers = new ArrayList<>();

    private final List<String> allowList = new ArrayList<>();

    private String password = "sapa1";

    private boolean hasPassword = true;

    private GameMode mode = GameMode.STANDARD;

    private ScheduledExecutorService scheduledExecutorService;

    private ScheduledExecutorService pingScheduler;

    public Card join(PlayerRequest request) {
        this.validatePassword(request.getPassword());

        Player player = PlayerMapper.toEntity(request);

        Optional<Card> existent = this.cards.stream()
                .filter(card -> card.getPlayer().getUsername().equals(request.getUsername()))
                .findFirst();

        if (existent.isPresent()) {
            return existent.get();
        }

        this.validateJoin(request);

        Card card = Card.builder()
                .id(UUID.randomUUID())
                .player(player)
                .build();

        do {
            card.setNumbers(GameUtils.drawCardNumbers());
        } while (this.cardAlreadyExists(card));

        this.cards.add(card);
        this.notifyJoin(card);

        return card;
    }

    public MarkResponse mark(MarkRequest request) {
        MarkResponse response = new MarkResponse();

        this.validateMark(request);

        Card card = this.cards.stream()
                .filter(c -> c.getPlayer().getId().equals(request.getPlayerId()))
                .findFirst()
                .orElseThrow();

        card.getMarkedNumbers()[request.getI()][request.getJ()] = request.isMarked();

        this.notifyMarked(new MarkedResponse(request.getPlayerId(), request.getI(), request.getJ(), request.isMarked()));

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

        this.scheduledExecutorService.scheduleWithFixedDelay(this::drawNumber, 0, 10, TimeUnit.SECONDS);

        SseUtils.broadcastStartMessage(SseUtils.mapEmitters(this.cards, this.admins));
    }

    public BingoResponse bingo(UUID playerId) {
        Card card = this.cards.stream()
                .filter(c -> c.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException("Player not found"));

        boolean isWinner = GameMode.STANDARD.equals(this.mode) ?
                GameUtils.checkStandardWinner(card.getMarkedNumbers(), card.getNumbers(), this.drawnNumbers)
                : GameUtils.checkBlackoutWinner(card.getMarkedNumbers(), card.getNumbers(), this.drawnNumbers);

        if (isWinner) {
            this.notifyWinner(card.getPlayer());
            this.isGameRunning = false;
            this.scheduledExecutorService.shutdown();
            this.winners.add(card.getPlayer());
            this.cards.remove(card);
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
        this.notifyClean();
        this.drawnNumbers.clear();
        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdown();
        }
    }

    public GameResponse getGame(UUID playerId) {
        GameResponse response = new GameResponse();

        Card card = this.cards.stream()
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

    public void addListener(UUID playerId, boolean isAdmin, SseEmitter emitter) {
        if (isAdmin) {
            this.admins.add(emitter);
        } else {
            Player player = this.cards.stream()
                    .filter(card -> card.getPlayer().getId().equals(playerId))
                    .findFirst()
                    .orElseThrow(() -> new UnprocessableEntityException("Player was not found"))
                    .getPlayer();

            player.setEmitter(emitter);
        }
        this.startPing();
    }

    public void setAllowList(List<String> usernames) {
        this.allowList.clear();
        this.allowList.addAll(usernames);
    }

    public void setGameMode(GameMode mode) {
        this.mode = mode;
        this.notifyGameMode(mode);
    }

    public void setPassword(String password) {
        if ("".equals(password) || password == null) {
            this.password = null;
            this.hasPassword = false;
        } else {
            this.password = password;
            this.hasPassword = true;
        }
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
        this.notifyNumber(number);

        log.info("drawn number = {}", number);
    }

    private void startPing() {
        if (this.pingScheduler != null) {
            this.pingScheduler.shutdown();
        }

        this.pingScheduler = Executors.newSingleThreadScheduledExecutor();
        this.pingScheduler.scheduleWithFixedDelay(this::notifyPing, 0, 10, TimeUnit.SECONDS);
    }

    private void notifyPing() {
        SseUtils.broadcastPing(
                SseUtils.mapEmitters(this.cards, this.admins)
        );
    }

    private void notifyWinner(Player player) {
        SseUtils.broadcastWinner(SseUtils.mapEmitters(this.cards, this.admins), player);
    }

    private void notifyNumber(int number) {
        SseUtils.broadcastDrawnNumberMessage(
                SseUtils.mapEmitters(this.cards, this.admins),
                new DrawnNumberResponse(number, this.drawnNumbers)
        );
    }

    private void notifyClean() {
        SseUtils.broadcastClean(
                SseUtils.mapEmitters(this.cards, this.admins)
        );
    }

    private void notifyMarked(MarkedResponse markedResponse) {
        SseUtils.broadcastMarked(
                this.admins,
                markedResponse
        );
    }

    private void notifyJoin(Card card) {
        SseUtils.broadcastJoin(
                this.admins,
                card
        );
    }

    private void notifyGameMode(GameMode mode) {
        SseUtils.broadcastGameMode(
                SseUtils.mapEmitters(this.cards, this.admins),
                mode
        );
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
                .map(Card::getPlayer)
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
        if (this.cards.size() >= 50) {
            throw new UnprocessableEntityException("Max players reached");
        }
    }

    private void validatePassword(String password) {
        if (this.hasPassword && !this.password.equals(password)) {
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


    private boolean cardAlreadyExists(Card card) {
        return this.cards.stream()
                .filter(c -> !c.getId().equals(card.getId()))
                .anyMatch(c -> this.cardIsEquals(c.getNumbersList(), card.getNumbersList()));
    }

    private boolean cardIsEquals(List<Integer> playerNumbers, List<Integer> otherPlayerNumbers) {
        return new HashSet<>(otherPlayerNumbers).containsAll(playerNumbers);
    }
}
