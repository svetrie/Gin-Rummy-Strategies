package com.example;

import java.util.*;

public class GameEngine {
    private static final int POINTS_NEEDED_TO_WIN = 50;
    private static final int MAX_ROUNDS_PER_GAME = 5;

    private PlayerStrategy player1;
    private ArrayList<Card> player1Hand;
    private PlayerStrategy player2;
    private ArrayList<Card> player2Hand;

    private ArrayList<Card> deck;
    private ArrayList<Card> discardPile;

    public GameEngine(PlayerStrategy firstPlayer, PlayerStrategy secondPlayer) {
       player1 = firstPlayer;
       player2 = secondPlayer;

       player1Hand = new ArrayList<>();
       player2Hand = new ArrayList<>();

       deck = new ArrayList<>();
       discardPile = new ArrayList<>();
    }

    /**
     * Simulates a game of gin rummy between two player strategies several times
     * @param numOfGames is number of games that will be played
     */
    public void simulateGames(int numOfGames) {
        int player1Wins = 0;
        int player2Wins = 0;
        int tieGames = 0;

        while(numOfGames > 0) {
            PlayerStrategy winner = playGame();

            if (winner == player1) {
                player1Wins++;
            } else if (winner == player2){
                player2Wins++;
            } else {
                tieGames++;
            }

            reset();

            numOfGames--;
        }

        System.out.println("Player1 wins: " + player1Wins);
        System.out.println("Player2 wins: " + player2Wins);
        System.out.println("Tie games: " + tieGames);
    }

    /**
     * Initializes and shuffles deck, initializes discard pile, and deals a hand
     * to the player strategies before the game begins
     */
    public void startGame() {
        deck.addAll(Card.getAllCards());
        Collections.shuffle(deck);

        ArrayList<Card> player1InitialHand = getinitialPlayerHand();
        ArrayList<Card> player2InitialHand = getinitialPlayerHand();

        player1.receiveInitialHand(player1InitialHand);
        player2.receiveInitialHand(player2InitialHand);

        player1Hand.addAll(player1InitialHand);
        player2Hand.addAll(player2InitialHand);

        discardPile.add(deck.remove(0));
    }

    /**
     * Clears the deck and discard pile and resets the internal states of the player
     * strategies before starting a new game
     */
    public void reset() {
        deck.clear();
        discardPile.clear();
        player1Hand.clear();
        player2Hand.clear();

        player1.reset();
        player2.reset();
    }

    /**
     * Simulates a game between two player strategies
     * @return player who won the game or null if there is a tie between players
     */
    public PlayerStrategy playGame() {
        int player1Points = 0;
        int player2Points = 0;
        int rounds = 0;

        startGame();

        while(player1Points < POINTS_NEEDED_TO_WIN && player2Points < POINTS_NEEDED_TO_WIN
                && rounds < MAX_ROUNDS_PER_GAME) {

            PlayerStrategy playerWhoKnocked = playRound();
            rounds++;

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
        } else if (player2Points >= 50){
            return player2;
        } else {
            return null;
        }
    }

    /**
     * Simulates a round in a game between two player strategies
     * @return player who won the game or null if the deck runs out before there is winner
     */
    public PlayerStrategy playRound() {
        while (deck.size() > 0) {
            playerTurn(player1, player1Hand, player2);

            if (player1.knock()) {
                return player1;
            } else if(deck.size() <= 0) {
                return null;
            }

            playerTurn(player2, player2Hand, player1);

            if (player2.knock()) {
                return player2;
            } else if (deck.size() <= 0){
                return null;
            }
        }

        return null;
    }

    /**
     * Deals a card to the current player based on whether the player chooses to take card
     * from the deck or the discard pile. Adds card player discarded to discard pile. Relays
     * information about the current player's turn to the opponent
     * @param currentPlayer the player whose turn it currently is
     * @param currentPlayerHand the current player's hand
     * @param opponent the player strategy the current player is competing against
     */
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
            currentPlayerHand.add(topOfDiscardPile);
            discardedByPlayer = currentPlayer.drawAndDiscard(discardPile.remove(0));

            currentPlayerHand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);
        } else {
            Card cardFromDeck = deck.remove(0);
            currentPlayerHand.add(cardFromDeck);
            discardedByPlayer = currentPlayer.drawAndDiscard(cardFromDeck);

            currentPlayerHand.remove(discardedByPlayer);
            discardPile.add(0, discardedByPlayer);
        }

        opponent.opponentEndTurnFeedback(takeFromDiscardPile, topOfDiscardPile, discardedByPlayer);
    }

    /**
     *
     * @param knocker the player who knocked
     * @param knockersHand the current hand of the player who knocked
     * @param opponent the knocker's opponent
     * @param opponentsHand the opponent's current hand
     * @return an integer representing the points that the winner should recieve.
     * If the winner was the knocker, the the integer will be positive, but if the winner
     * is the opponent, the integer will be negative
     */
    public int getWinnersPoints(PlayerStrategy knocker, List<Card> knockersHand,
                                PlayerStrategy opponent, List<Card> opponentsHand) {

        int knockerDeadWood = getKnockersTotalDeadwood(knocker, knockersHand);
        int opponentDeadWood = getOpponentsTotalDeadwood(opponent, opponentsHand, knocker);

        if(knockerDeadWood == 0) {
            return 25 + opponentDeadWood - knockerDeadWood;
        } else if (knockerDeadWood <= opponentDeadWood) {
            return opponentDeadWood - knockerDeadWood;
        } else {
            return -(25 + knockerDeadWood - opponentDeadWood);
        }
    }

    /**
     * Return the total deadwood the player who knocked has
     * @param player the player who knocked
     * @param playerHand the current hand of the player who knocked
     * @return
     */
    public int getKnockersTotalDeadwood(PlayerStrategy player, List<Card> playerHand) {
        int totalDeadwood = 0;

        for (Card card : playerHand) {
            boolean isInMeld = false;

            for (Meld meld : player.getMelds()) {
                if (meld.containsCard(card)) {
                    isInMeld = true;
                    break;
                }
            }

            if (!isInMeld) {
                totalDeadwood += card.getPointValue();
            }
        }

        return totalDeadwood;
    }

    /**
     * Calculate the player's deadwood score by subtracting the deadwood that can be
     * appended to their opponent's melds from the total deadwood in their hand
     * @param player is the player whose opponent knocked
     * @param playerHand is the player's current hand
     * @param opponent is the player's opponent
     * @return the player's deadwood score
     */
    public int getOpponentsTotalDeadwood(PlayerStrategy player, List<Card> playerHand,
                                         PlayerStrategy opponent) {

        int totalDeadwood = getKnockersTotalDeadwood(player, playerHand);

        int appendableDeadwood = 0;

        for (Card card: playerHand) {
            if(canBeAppendedToOpponentsMeld(opponent, card)) {
                appendableDeadwood += card.getPointValue();
            }
        }

        return totalDeadwood - appendableDeadwood;
    }

    /**
     * Checks whether a player's deadwood card can be appended to one of their
     * opponent's melds
     * @param opponent is a Player Strategy
     * @param card is a card object
     * @return whether or not the card can be appended to one of the opponent's melds
     */
    public boolean canBeAppendedToOpponentsMeld(PlayerStrategy opponent, Card card) {
        for (Meld meld : opponent.getMelds()) {
            if (meld.canAppendCard(card)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to retrieve the ten initial cards from the deck that are dealt to the player
     * @return ten cards from the deck
     */
    public ArrayList<Card> getinitialPlayerHand() {
        ArrayList<Card> initialPlayerHand = new ArrayList<Card>();

        for (int i = 0; i < 10 && i < deck.size(); i++) {
            initialPlayerHand.add(deck.get(i));
            deck.remove(i);
        }

        return initialPlayerHand;
    }

}
