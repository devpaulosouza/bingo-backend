package dev.paulosouza.bingo.game;

public class GameSingleton {

    private GameSingleton() {

    }

    private static Game instance;

    public static Game getInstance() {
        if (GameSingleton.instance == null) {
            GameSingleton.instance = new Game();
        }
        return GameSingleton.instance;
    }

}
