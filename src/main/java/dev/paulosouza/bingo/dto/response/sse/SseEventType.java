package dev.paulosouza.bingo.dto.response.sse;

public enum SseEventType {
    START,
    DRAWN_NUMBER,
    CLEAN,
    MARK,
    WINNER, JOIN, GAME_MODE,
}
