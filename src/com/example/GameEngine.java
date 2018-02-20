package com.example;

import java.util.*;

public class GameEngine {
    private PlayerStrategy player1;
    private ArrayList<Card> player1Hand;

    private PlayerStrategy player2;
    private ArrayList<Card> player2Hand;

    private ArrayList<Card> deck;
    private ArrayList<Card> discardPile;

    public static void main(String args[]) {
        GameEngine gameEngine = new GameEngine(new AggressivePlayerStrategy(), new AggressivePlayerStrategy());

        gameEngine.simulateGames(100);
    }


    public GameEngine(PlayerStrategy firstPlayer, PlayerStrategy secondPlayer) {
       player1 = firstPlayer;
       player2 = secondPlayer;

       player1Hand = new ArrayList<>();
       player2Hand = new ArrayList<>();

       deck = new ArrayList<>();
       discardPile = new ArrayList<>();
    }



    public void simulateGames(int numOfGames) {
        int player1Wins = 0;
        int player2Wins = 0;

        while(numOfGames > 0) {
            PlayerStrategy winner = playGame();

            if (winner == player1) {
                System.out.println("PLAYER 1 WON");
                player1Wins++;
            } else {
                System.out.println("PLAYER 2 WON");
                player2Wins++;
            }

            System.out.println("Trying to reset");
            reset();

            numOfGames--;
        }

        System.out.println("Player1 wins: " + player1Wins);
        System.out.println("Player2 wins: " + player2Wins);
    }

    public void startGame() {
        deck.addAll(Card.getAllCards());
        Collections.shuffle(deck);

        ArrayList<Card> player1InitialHand = getinitialPlayerHand();
        ArrayList<Card> player2InitialHand = getinitialPlayerHand();

        player1.receiveInitialHand(player1InitialHand);
        player2.receiveInitialHand(player2InitialHand);

        player1Hand.addAll(player1InitialHand);
        player2Hand.addAll(player2InitialHand);

        System.out.println("initial player1hand:" + player1Hand.size());
        System.out.println("initial player2hand:" + player2Hand.size());

        discardPile.add(deck.remove(0));
    }

    public void reset() {
        deck.clear();
        discardPile.clear();
        player1Hand.clear();
        player2Hand.clear();

        player1.reset();
        player2.reset();
    }

    public PlayerStrategy playGame() {
        int player1Points = 0;
        int player2Points = 0;

        startGame();

        while(player1Points < 50 && player2Points < 50) {
            PlayerStrategy playerWhoKnocked = playRound();

            if (playerWhoKnocked == null) {

              //  System.out.println("Trying to add discard pile to deck");
                deck.addAll(discardPile);
              // System.out.println("deck size:" + deck.size());
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

    public PlayerStrategy playRound() {
        System.out.println("Beginning of round");

        while (deck.size() > 0) {
            playerTurn(player1, player1Hand, player2);

            System.out.println("Player 1 turn just ended");

            //System.out.println("player1hand:" + player1Hand.size());
            //System.out.println("player2hand:" + player2Hand.size());

            if (player1.knock()) {
               // System.out.println("Player kncocked");
                return player1;
            } else if(deck.size() <= 0) {
                return null;
            }

            playerTurn(player2, player2Hand, player1);

            System.out.println("player 2 turn just ended");

            //System.out.println("player1hand:" + player1Hand.size());
            //System.out.println("player2hand:" + player2Hand.size());

            if (player2.knock()) {
               // System.out.println("Player knocked");
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

           // System.out.println("discard pile size:" + discardPile.size());

            currentPlayerHand.add(topOfDiscardPile);

            System.out.println("Card taken from discard pile: " + topOfDiscardPile.getSuit() + "-" + topOfDiscardPile.getRank());

            discardedByPlayer = currentPlayer.drawAndDiscard(discardPile.remove(0));

            currentPlayerHand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);

            System.out.println("Card discarded: " + discardedByPlayer.getSuit() + "-" + discardedByPlayer.getRank());

        } else {
            Card cardFromDeck = deck.remove(0);

           // System.out.println("size of deck:" + deck.size());

            currentPlayerHand.add(cardFromDeck);

            System.out.println("Card taken from deck: " + cardFromDeck.getSuit() + "-" + cardFromDeck.getRank());

            discardedByPlayer = currentPlayer.drawAndDiscard(cardFromDeck);

            System.out.println("Card discarded: " + discardedByPlayer.getSuit() + "-" + discardedByPlayer.getRank());

            currentPlayerHand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);
        }

        opponent.opponentEndTurnFeedback(takeFromDiscardPile, topOfDiscardPile, discardedByPlayer);

        //System.out.println(takeFromDiscardPile);
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
