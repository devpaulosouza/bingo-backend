package dev.paulosouza.bingo.game;

import dev.paulosouza.bingo.dto.request.MarkRequest;
import dev.paulosouza.bingo.dto.response.BingoResponse;
import dev.paulosouza.bingo.dto.response.MarkResponse;
import dev.paulosouza.bingo.exception.UnprocessableEntityException;
import dev.paulosouza.bingo.utils.GameUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Game {

    private boolean isAcceptingNewPlayers = true;

    private boolean isGameRunning = false;

    private final List<Card> cards = new ArrayList<>();

    private final List<Integer> possibleNumbers = this.buildList(1, 75);

    private final List<Integer> drawnNumbers = new ArrayList<>();

    private final Random random = new Random();

    public Card join(Player player) {
        this.validateJoin();

        Card card = Card.builder()
                .id(UUID.randomUUID())
                .player(player)
                .build();

        do {
            card.setNumbers(this.drawCardNumbers());
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
            this.isGameRunning = false;
            response.setWinner(true);
            this.notifyWinner(card.getPlayer());
        }

        return response;
    }

    public void startGame() {
        this.isAcceptingNewPlayers = false;
        this.isGameRunning = true;
    }

    public void drawNumber() {
        int number = this.chooseNumber(this.possibleNumbers);
        this.drawnNumbers.add(number);
        this.notifyNumber(number);
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
        }

        return BingoResponse.builder()
                .winner(isWinner)
                .build();
    }

    private void notifyWinner(Player player) {

    }

    private void notifyNumber(int number) {

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
        return request.getI() != 2 && request.getJ() != 2;
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

    private int[][] drawCardNumbers() {
        int[][] cardNumbers = new int[5][5];

        List<Integer> possibleB = this.buildList(1, 15);
        List<Integer> possibleI = this.buildList(16, 30);
        List<Integer> possibleN = this.buildList(31, 45);
        List<Integer> possibleG = this.buildList(46, 60);
        List<Integer> possibleO = this.buildList(61, 75);

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                switch (j) {
                    case 0 -> cardNumbers[i][j] = this.chooseNumber(possibleB);
                    case 1 -> cardNumbers[i][j] = this.chooseNumber(possibleI);
                    case 2 -> {
                        if (i == 2) {
                            cardNumbers[i][j] = -1;
                        } else {
                            cardNumbers[i][j] = this.chooseNumber(possibleN);
                        }
                    }
                    case 3 -> cardNumbers[i][j] = this.chooseNumber(possibleG);
                    case 4 -> cardNumbers[i][j] = this.chooseNumber(possibleO);
                    default -> throw new UnprocessableEntityException("Unexpected range");
                }
            }
        }

        return cardNumbers;
    }

    @SuppressWarnings("java:S6204")
    private List<Integer> buildList(int startInclusive, int endInclusive) {
        return IntStream.rangeClosed(startInclusive, endInclusive)
                .boxed()
                .collect(Collectors.toList());
    }

    private int chooseNumber(List<Integer> possibleNumbers) {
        return possibleNumbers.remove(this.random.nextInt(possibleNumbers.size()));
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
