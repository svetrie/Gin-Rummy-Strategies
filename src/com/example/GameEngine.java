package com.example;

import java.util.ArrayList;
import java.util.Set;

public class GameEngine {
    private PlayerStrategy player1;
    private PlayerStrategy player2;
    private ArrayList<Card> deck;
    private ArrayList<Card> discardPile;

    public GameEngine(PlayerStrategy player1, PlayerStrategy player2) {
        this.player1 = player1;
        this.player2 = player2;
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
