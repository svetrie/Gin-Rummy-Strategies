package com.example;

import java.util.*;

public class GameEngine {
    private PlayerStrategy player1;
    private ArrayList<Card> player1Hand;
    private int player1Wins;

    private PlayerStrategy player2;
    private ArrayList<Card> player2Hand;
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

        player1Hand = new ArrayList<>();
        player1Wins = 0;
        player2Hand = new ArrayList<>();
        player2Wins = 0;

        deck = new ArrayList<>(Card.getAllCards());
        discardPile = new ArrayList<>();
    }

    public void startGame() {
        ArrayList<Card> player1InitialHand = getinitialPlayerHand();
        ArrayList<Card> player2InitialHand = getinitialPlayerHand();

        player1.receiveInitialHand(player1InitialHand);
        player2.receiveInitialHand(player2InitialHand);

        player1Hand.addAll(player1InitialHand);
        player2Hand.addAll(player2InitialHand);

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

    public PlayerStrategy playGame() {
        int player1Points = 0;
        int player2Points = 0;

        while(player1Points < 50 && player2Points < 50) {
            PlayerStrategy playerWhoKnocked = playRound();

            if (playerWhoKnocked == null) {
                deck.addAll(discardPile);
                discardPile.clear();
                Collections.shuffle(deck);
                discardPile.add(deck.remove(0));

            } else if (playerWhoKnocked == player1) {
                int winnerPoints = getWinnersPoints(player1, player1Hand, player2, player2Hand);

                if (winnerPoints >= 0) {
                    player1Points += winnerPoints;
                } else {
                    player2Points += -(winnerPoints);
                }

            } else {
                int winnerPoints = getWinnersPoints(player2, player2Hand, player1, player1Hand);

                if (winnerPoints > 0) {
                    player2Points += winnerPoints;
                } else {
                    player1Points += -(winnerPoints);
                }
            }
        }

        if (player1Points >= 50) {
            return player1;
        } else {
            return player2;
        }
    }

    public int getWinnersPoints(PlayerStrategy knocker, List<Card> knockersHand,
                                PlayerStrategy opponent, List<Card> opponentsHand) {

        int knockerDeadWood = getPlayerTotalDeadwood(knocker, knockersHand);
        int opponentDeadWood = getPlayerTotalDeadwood(opponent, opponentsHand);

        if(knockerDeadWood == 0) {
            return 25 + opponentDeadWood - knockerDeadWood;
        } else if (knockerDeadWood <= opponentDeadWood) {
            return opponentDeadWood - knockerDeadWood;
        } else {
            return -(25 + knockerDeadWood - opponentDeadWood);
        }
    }

    public int getPlayerTotalDeadwood(PlayerStrategy player, List<Card> playerHand) {
        int totalDeadwood = 0;

        for (Card card : playerHand) {
            boolean isInMeld = false;

            for (Meld meld : player.getMelds()) {
                if (meld.containsCard(card)) {
                    isInMeld = true;
                }
            }

            if (!isInMeld) {
                totalDeadwood += card.getPointValue();
            }
        }

        return totalDeadwood;
    }


    public void player1Turn() {
        Card discardedByPlayer;
        boolean takeFromDiscardPile = false;
        Card topOfDiscardPile = discardPile.get(0);

        if (player1.willTakeTopDiscard(topOfDiscardPile)) {
            takeFromDiscardPile= true;

            player1Hand.add(topOfDiscardPile);

            discardedByPlayer = player1.drawAndDiscard(discardPile.remove(0));

            player1Hand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);
        } else {
            player1Hand.add(deck.remove(0));

            discardedByPlayer = player1.drawAndDiscard(deck.remove(0));

            player1Hand.remove(discardedByPlayer);
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

            player2Hand.add(topOfDiscardPile);

            discardedByPlayer =  player2.drawAndDiscard(discardPile.remove(0));

            player2Hand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);
        } else {
            player2Hand.add(deck.remove(0));

            discardedByPlayer = player2.drawAndDiscard(deck.remove(0));

            player2Hand.remove(discardedByPlayer);
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
