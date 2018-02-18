package com.example;

import java.util.ArrayList;
import java.util.Set;

public class GameEngine {
    private PlayerStrategy player1;
    private int player1Wins;
    private PlayerStrategy player2;
    private int player2Wins;
    private ArrayList<Card> deck;
    private ArrayList<Card> discardPile;

    public GameEngine(PlayerStrategy firstPlayer, PlayerStrategy secondPlayer) {
        if (Math.random() >= .5) {
            player1 = firstPlayer;
            player2 = secondPlayer;
        } else {
            player1 = secondPlayer;
            player2 = firstPlayer;
        }

        player1Wins = 0;
        player2Wins = 0;

        deck = new ArrayList<>(Card.getAllCards());
        discardPile = new ArrayList<>();
    }

    public void startGame() {
        player1.receiveInitialHand(getinitialPlayerHand());
        player2.receiveInitialHand(getinitialPlayerHand());

        discardPile.add(deck.remove(0));
    }

    public ArrayList<Card> getinitialPlayerHand() {
        ArrayList<Card> initialPlayerHand = new ArrayList<Card>();

        for (int i = 0; i < 10 && i < deck.size(); i++) {
            initialPlayerHand.add(deck.get(i));
            deck.remove(i);
        }

        return initialPlayerHand;
    }


}
