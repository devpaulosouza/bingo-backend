package dev.paulosouza.bingo.game;

import dev.paulosouza.bingo.dto.request.MarkRequest;
import dev.paulosouza.bingo.dto.response.BingoResponse;
import dev.paulosouza.bingo.dto.response.MarkResponse;
import dev.paulosouza.bingo.dto.response.sse.DrawnNumberResponse;
import dev.paulosouza.bingo.exception.UnprocessableEntityException;
import dev.paulosouza.bingo.utils.GameUtils;
import dev.paulosouza.bingo.utils.ListUtils;
import dev.paulosouza.bingo.utils.SseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
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

    private List<Integer> possibleNumbers;

    private final List<Integer> drawnNumbers = new ArrayList<>();

    private ScheduledExecutorService scheduledExecutorService;

    public Card join(Player player) {
        this.validateJoin();

        Card card = Card.builder()
                .id(UUID.randomUUID())
                .player(player)
                .build();

        do {
            card.setNumbers(GameUtils.drawCardNumbers());
        } while (this.cardAlreadyExists(card));

        this.cards.add(card);

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

        if (GameUtils.checkWinner(card.getMarkedNumbers(), card.getNumbers(), this.drawnNumbers)) {
            response.setWinner(true);
        }

        return response;
    }

    public void startGame() {
        this.isAcceptingNewPlayers = false;
        this.isGameRunning = true;
        this.possibleNumbers = ListUtils.buildList(1, 75);
        this.drawnNumbers.clear();

        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        this.scheduledExecutorService.scheduleWithFixedDelay(this::drawNumber, 0, 5, TimeUnit.SECONDS);

        SseUtils.broadcastStartMessage(SseUtils.mapEmitters(this.cards));
    }

    public BingoResponse bingo(UUID playerId) {
        Card card = this.cards.stream()
                .filter(c -> c.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException("Player not found"));

        boolean isWinner = GameUtils.checkWinner(card.getMarkedNumbers(), card.getNumbers(), this.drawnNumbers);

        if (isWinner) {
            this.notifyWinner(card.getPlayer());
            this.isGameRunning = false;
            this.scheduledExecutorService.shutdown();
        }

        return BingoResponse.builder()
                .winner(isWinner)
                .build();
    }

    public void clean() {
        this.cards.clear();
        this.isAcceptingNewPlayers = true;
        this.isGameRunning = false;
    }

    public void addListener(UUID playerId, SseEmitter emitter) {
        Player player = this.cards.stream()
                .filter(card -> card.getPlayer().getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new UnprocessableEntityException("Player was not found"))
                .getPlayer();

        player.setEmitter(emitter);
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

    private void notifyWinner(Player player) {
        SseUtils.broadcastWinner(SseUtils.mapEmitters(this.cards), player);
    }

    private void notifyNumber(int number) {
        SseUtils.broadcastDrawnNumberMessage(
                SseUtils.mapEmitters(this.cards),
                new DrawnNumberResponse(number, this.drawnNumbers)
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

    private void validateJoin() {
        this.validateAcceptingNewPlayers();
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