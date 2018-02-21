package com.example;

public class RunGameEngine {
    public static void main(String[] args) {
        PlayerStrategy player1 = new AggressivePlayerStrategy();
        PlayerStrategy player2 = new CautiousPlayerStrategy();
        PlayerStrategy player3 = new BalancedPlayerStrategy();

        GameEngine gameEngine = new GameEngine(player1, player2);
        gameEngine.simulateGames(100);

        gameEngine = new GameEngine(player2, player3);
        gameEngine.simulateGames(100);

        gameEngine = new GameEngine(player1, player2);
        gameEngine.simulateGames(100);
    }
}
