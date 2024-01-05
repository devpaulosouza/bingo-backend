package dev.paulosouza.bingo.dto.bingo.response.sse;

public enum SseEventType {
    START,
    DRAWN_NUMBER,
    CLEAN,
    MARK,
    WINNER, JOIN, GAME_MODE, PING, KICK,
}
