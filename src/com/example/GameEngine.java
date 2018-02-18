package com.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class GameEngine {
    private PlayerStrategy player1;
    private ArrayList<Card> player1CurrentHand;
    private int player1Wins;

    private PlayerStrategy player2;
    private ArrayList<Card> player2CurrentHand;
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

        player1CurrentHand = new ArrayList<>();
        player1Wins = 0;
        player2CurrentHand = new ArrayList<>();
        player2Wins = 0;

        deck = new ArrayList<>(Card.getAllCards());
        discardPile = new ArrayList<>();
    }

    public void startGame() {
        ArrayList<Card> player1InitialHand = getinitialPlayerHand();
        ArrayList<Card> player2InitialHand = getinitialPlayerHand();

        player1.receiveInitialHand(player1InitialHand);
        player2.receiveInitialHand(player2InitialHand);

        player1CurrentHand.addAll(player1InitialHand);
        player2CurrentHand.addAll(player2InitialHand);

        discardPile.add(deck.remove(0));
    }

    public PlayerStrategy playRound() {
        while (deck.size() > 0) {
            player1Turn();

            if (player1.knock()) {
                return player1;
            } else if(deck.size() <= 0) {
                return null;
            }

            player2Turn();

            if (player2.knock()) {
                return player2;
            } else if (deck.size() <= 0){
                return null;
            }
        }
        
        return null;
    }

    public void playGame() {

    }



    public void player1Turn() {
        Card discardedByPlayer;
        boolean takeFromDiscardPile = false;
        Card topOfDiscardPile = discardPile.get(0);

        if (player1.willTakeTopDiscard(topOfDiscardPile)) {
            takeFromDiscardPile= true;

            player1CurrentHand.add(topOfDiscardPile);

            discardedByPlayer = player1.drawAndDiscard(discardPile.remove(0));

            player1CurrentHand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);
        } else {
            player1CurrentHand.add(deck.remove(0));

            discardedByPlayer = player1.drawAndDiscard(deck.remove(0));

            player1CurrentHand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);
        }

        player2.opponentEndTurnFeedback(takeFromDiscardPile, topOfDiscardPile, discardedByPlayer );
    }

    public void player2Turn() {
        Card discardedByPlayer;
        boolean takeFromDiscardPile = false;
        Card topOfDiscardPile = discardPile.get(0);

        if (player2.willTakeTopDiscard(topOfDiscardPile)) {
            takeFromDiscardPile = true;

            player2CurrentHand.add(topOfDiscardPile);

            discardedByPlayer =  player2.drawAndDiscard(discardPile.remove(0));

            player2CurrentHand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);
        } else {
            player2CurrentHand.add(deck.remove(0));

            discardedByPlayer = player2.drawAndDiscard(deck.remove(0));

            player2CurrentHand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);
        }

        player1.opponentEndTurnFeedback(takeFromDiscardPile, topOfDiscardPile, discardedByPlayer);
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
