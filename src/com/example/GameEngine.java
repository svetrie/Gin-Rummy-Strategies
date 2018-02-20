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

    public static void main(String args[]) {
        GameEngine gameEngine = new GameEngine(new BalancedPlayerStrategy(), new BalancedPlayerStrategy());

       // gameEngine.simulateGames(1);

        if (gameEngine.playGame() == null) {
            System.out.println("null");
        } else  {
            System.out.println("player");
        }

        //System.out.println("Player1 wins : " + gameEngine.getPlayer1Wins());
        //System.out.println("player2 Wins: " + gameEngine.getPlayer2Wins());
    }


    public GameEngine(PlayerStrategy firstPlayer, PlayerStrategy secondPlayer) {
       /*
        if (Math.random() >= .5) {
            player1 = firstPlayer;
            player2 = secondPlayer;
        } else {
            player1 = secondPlayer;
            player2 = firstPlayer;
        }*/

       player1 = firstPlayer;
       player2 = secondPlayer;

        player1Hand = new ArrayList<>();
        player1Wins = 0;
        player2Hand = new ArrayList<>();
        player2Wins = 0;

        deck = new ArrayList<>(Card.getAllCards());
        Collections.shuffle(deck);
        discardPile = new ArrayList<>();
    }

    public int getPlayer1Wins() {
        return player1Wins;
    }

    public int getPlayer2Wins() {
        return player2Wins;
    }

    public void simulateGames(int numOfGames) {
        while(numOfGames > 0) {
            PlayerStrategy winner = playGame();

            if (winner == player1) {
                player1Wins++;
            } else {
                player2Wins++;
            }

            player1.reset();
            player2.reset();

            numOfGames--;
        }
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
        System.out.println("Beginning of round");

        while (deck.size() > 0) {
            playerTurn(player1, player1Hand, player2);

            System.out.println("Player 1 turn just ended");

            if (player1.knock()) {
                return player1;
            } else if(deck.size() <= 0) {
                return null;
            }

            playerTurn(player2, player2Hand, player1);

            System.out.println("player 2 turn just ended");

            if (player2.knock()) {
                return player2;
            } else if (deck.size() <= 0){
                return null;
            }
        }

        return null;
    }

    public void playerTurn(PlayerStrategy currentPlayer, List<Card> currentPlayerHand,
                           PlayerStrategy opponent) {
        Card discardedByPlayer;
        boolean takeFromDiscardPile = false;
        Card topOfDiscardPile;

        if (discardPile.size() > 0) {
            topOfDiscardPile = discardPile.get(0);
        } else {
            topOfDiscardPile = null;
        }

        if (topOfDiscardPile != null && currentPlayer.willTakeTopDiscard(topOfDiscardPile)) {
            takeFromDiscardPile= true;

            System.out.println("discard pile size:" + discardPile.size());

            currentPlayerHand.add(topOfDiscardPile);

            discardedByPlayer = currentPlayer.drawAndDiscard(discardPile.remove(0));

            currentPlayerHand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);
        } else {
            Card cardFromDeck = deck.remove(0);

            System.out.println("size of deck:" + deck.size());

            currentPlayerHand.add(cardFromDeck);

            discardedByPlayer = currentPlayer.drawAndDiscard(cardFromDeck);

            currentPlayerHand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);
        }

        opponent.opponentEndTurnFeedback(takeFromDiscardPile, topOfDiscardPile, discardedByPlayer);

        System.out.println(takeFromDiscardPile);
    }

    public PlayerStrategy playGame() {
        int player1Points = 0;
        int player2Points = 0;

        startGame();

        while(player1Points < 50 && player2Points < 50) {
            PlayerStrategy playerWhoKnocked = playRound();

            if (playerWhoKnocked == null) {
                deck.addAll(discardPile);
                discardPile.clear();
                Collections.shuffle(deck);
                discardPile.add(deck.remove(0));

                System.out.println("Ran out of cards, restart round");

            } else if (playerWhoKnocked == player1) {
                int winnerPoints = getWinnersPoints(player1, player1Hand, player2, player2Hand);

                if (winnerPoints >= 0) {
                    player1Points += winnerPoints;
                } else {
                    player2Points += -(winnerPoints);
                }

                System.out.println("Player knocked");

            } else {
                int winnerPoints = getWinnersPoints(player2, player2Hand, player1, player1Hand);

                if (winnerPoints > 0) {
                    player2Points += winnerPoints;
                } else {
                    player1Points += -(winnerPoints);
                }

                System.out.println("player knocked");
            }
        }

        System.out.println("Player 1 melds:" + player1.getMelds().size());
        System.out.println("Player 2 melds: " + player2.getMelds().size());

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

    public ArrayList<Card> getinitialPlayerHand() {
        ArrayList<Card> initialPlayerHand = new ArrayList<Card>();

        for (int i = 0; i < 10 && i < deck.size(); i++) {
            initialPlayerHand.add(deck.get(i));
            deck.remove(i);
        }

        return initialPlayerHand;
    }


}
