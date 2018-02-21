package com.example;

import java.util.ArrayList;
import java.util.List;

public class AggressivePlayerStrategy implements PlayerStrategy {

    private static final int CARDS_PER_SUIT = 13;
    private static final int MIN_CARDS_PER_MELD = 3;
    private static final int MAX_DEADWOOD_TOTAL_TO_KNOCK = 10;

    private List<Meld> playerMelds;

    private List<Card> currentHand;
    private Card[] spadesInHand;
    private Card[] clubsInHand;
    private Card[] heartsInHand;
    private Card[] diamondsInHand;
    private int[] cardsByRank;
    private AggressivePlayerStrategy opponent;


    public void loadOpponent() {
        opponent = new AggressivePlayerStrategy();
    }

    public Card[] getHeartsInHand() {
        return heartsInHand;
    }

    public int[] getCardsByRank() {
        return cardsByRank;
    }

    public List<Card> getCurrentHand() {
        return currentHand;
    }

    public AggressivePlayerStrategy() {
        playerMelds = new ArrayList<>();
        currentHand = new ArrayList<>();
        spadesInHand = new Card[CARDS_PER_SUIT];
        clubsInHand = new Card[CARDS_PER_SUIT];
        heartsInHand = new Card[CARDS_PER_SUIT];
        diamondsInHand = new Card[CARDS_PER_SUIT];
        cardsByRank = new int[CARDS_PER_SUIT];
    }

    /**
     * Removes the card from its suit array and decrements its index in cardsByRank
     * @param card is the card to be removed
     */
    public void removeCard(Card card) {
        getSuitArrayOfCard(card)[card.getRankValue()] = null;
        cardsByRank[card.getRankValue()]--;
    }

    /**
     * Removes a list of cards using removeCard()
     * @param cards is a list of cards to be removed
     */
    public void removeListOfCards(List<Card> cards) {
        for (Card card : cards) {
            removeCard(card);
        }
    }

    /**
     * Adds a card to its suit array and increments its index in cardsByRanks
     * @param card is the card to be added
     */
    public void addCard(Card card) {
        getSuitArrayOfCard(card)[card.getRankValue()] = card;
        cardsByRank[card.getRankValue()]++;
    }

    /**
     * @param card is the card who suit array the player strategy needs
     * @return the suit array of a crd
     */
    public Card[] getSuitArrayOfCard(Card card) {

        if (card.getSuit().equals(Card.CardSuit.CLUBS)) {
            return clubsInHand;
        } else if (card.getSuit().equals(Card.CardSuit.SPADES)) {
            return spadesInHand;
        } else if (card.getSuit().equals(Card.CardSuit.DIAMONDS)) {
            return diamondsInHand;
        } else {
            return heartsInHand;
        }
    }

    /**
     * @param card is a card that the player wants to append to an existing meld
     * @return the meld the card can be appended to or null if no such meld exists
     */
    public Meld canAppendToExistingMelds(Card card) {
        for (Meld meld : playerMelds) {
            if (meld.canAppendCard(card)) {
                return meld;
            }
        }
        return null;
    }

    /**
     * @param card player strategy wants to check if thise card is in a meld
     * @return true if the card is in an existing meld or false otherwise
     */
    public boolean isInMeld(Card card) {
        for (Meld meld : playerMelds) {
            if (meld.containsCard(card)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void receiveInitialHand(List<Card> hand) {
        currentHand = new ArrayList<>(hand);
        loadOpponent();

        for (Card card : hand) {
            addCard(card);
        }

        makeInitialMelds();
    }

    @Override
    public boolean willTakeTopDiscard(Card card) {
        addCard(card);

        boolean takeCard = (Meld.buildRunMeld(getPotentialRunMeld(getSuitArrayOfCard(card))) != null
                || Meld.buildSetMeld(getPotentialSetMeld()) != null || canAppendToExistingMelds(card) != null);

        removeCard(card);

        return takeCard;
    }

    @Override
    public Card drawAndDiscard(Card drawnCard) {
        currentHand.add(drawnCard);
        addCard(drawnCard);

        if(!makePotentialMelds(drawnCard) && canAppendToExistingMelds(drawnCard) != null) {
            canAppendToExistingMelds(drawnCard).appendCard(drawnCard);
            removeCard(drawnCard);
        }

        Card discard = getDiscard();

        currentHand.remove(discard);
        removeCard(discard);

        return discard;
    }

    @Override
    public boolean knock() {
        return (getTotalDeadwood() <= MAX_DEADWOOD_TOTAL_TO_KNOCK);
    }

    @Override
    public List<Meld> getMelds() {
        return playerMelds;
    }

    @Override
    public void opponentEndTurnFeedback(boolean drewDiscard, Card previousDiscardTop, Card opponentDiscarded) {
        if (drewDiscard) {
            opponent.getCurrentHand().add(previousDiscardTop);
            opponent.addCard(previousDiscardTop);
        }

        if (opponent.getCurrentHand().contains(opponentDiscarded)) {
            opponent.getCurrentHand().remove(opponentDiscarded);
            opponent.removeCard(opponentDiscarded);
        }
    }

    @Override
    public void opponentEndRoundFeedback(List<Card> opponentHand, List<Meld> opponentMelds) {
        opponent.getCurrentHand().clear();
        opponent.getCurrentHand().addAll(opponentHand);

        opponent.getMelds().clear();
        opponent.getMelds().addAll(opponentMelds);
    }

    @Override
    public void reset() {
        playerMelds.clear();
        currentHand.clear();
        spadesInHand = new Card[CARDS_PER_SUIT];
        clubsInHand = new Card[CARDS_PER_SUIT];
        heartsInHand = new Card[CARDS_PER_SUIT];
        diamondsInHand = new Card[CARDS_PER_SUIT];
        cardsByRank = new int[CARDS_PER_SUIT];
        opponent = new AggressivePlayerStrategy();
    }

    /**
     * @return the total deadwood that the player strategy currently has
     */
    public int getTotalDeadwood() {
        int totalDeadwood =  0;

        for (Card card : currentHand) {
            if (!isInMeld(card)) {
                totalDeadwood += card.getPointValue();
            }
        }

        return totalDeadwood;
    }

    /**
     * Chooses which card the player strategy should discard by factoring in the
     * card's deadwood value and its potential usefulness to its opponent
     * @return the card that player strategy should discard
     */
    public Card getDiscard() {
        ArrayList<Card> deadwoodsUsefulToOpponent = new ArrayList<>();

        Card deadwood = getHighestDeadwood();

        while(deadwood != null && isCardUsefulToOpponent(deadwood) && this.currentHand.size() > 0) {

            deadwoodsUsefulToOpponent.add(deadwood);
            this.currentHand.remove(deadwood);
            deadwood = getHighestDeadwood();
        }

        if (currentHand.size() <= 0 || deadwood == null) {
            currentHand.addAll(deadwoodsUsefulToOpponent);
            return getHighestDeadwood();
        } else  {
            currentHand.addAll(deadwoodsUsefulToOpponent);
            return deadwood;
        }

    }

    /**
     * @return the card with the highest deadwood value not in a meld. If all
     * cards are in a meld, will return a card that is removable from a meld
     */
    public Card getHighestDeadwood() {
        Card deadwood = null;

        for (Card card : currentHand) {
            if (!isInMeld(card)) {
                deadwood = card;
                break;
            }
        }

        for (Card card : currentHand) {
            if (!isInMeld(card) && deadwood.getPointValue() < card.getPointValue()) {
                deadwood = card;
            }
        }

        if (deadwood == null) {
            return getRemovableCardInMeld();
        } else {
            return deadwood;
        }
    }

    /**
     * @return a card that is removable from a meld
     * Used to calculate getHighestDeadwood() when all cards are in a meld
     */
    public Card getRemovableCardInMeld() {
        for (Card card : currentHand) {

            for (Meld meld : getMelds()) {

                if (meld.canRemoveCard(card)) {
                    meld.removeCard(card);
                    return card;
                }
            }
        }

        return null;
    }

    /**
     * Checks a suit to see if it contains three or more consecutive cards. If it does,
     * returns the consecutive cards
     * @param suit that the player strategy is checking for a run meld
     * @return a potential list of cards that could form a run meld
     */
    public List<Card> getPotentialRunMeld(Card[] suit) {
        int consecutiveCards = 0;
        List<Card> potentialMeld = new ArrayList<>();

        for (int i = 1; i < CARDS_PER_SUIT; i++) {

            if (suit[i] != null && suit[i - 1] != null && !isInMeld(suit[i]) && !isInMeld(suit[i - 1])) {

                if (consecutiveCards == 0) {
                    consecutiveCards += 2;
                } else {
                    consecutiveCards++;
                }

                if (consecutiveCards == MIN_CARDS_PER_MELD) {
                    potentialMeld.add(suit[i - 2]);
                    potentialMeld.add(suit[i - 1]);
                    potentialMeld.add(suit[i]);
                } else if (consecutiveCards > MIN_CARDS_PER_MELD) {
                    potentialMeld.add(suit[i]);
                }

            } else {
                consecutiveCards = 0;
            }
        }

        return potentialMeld;
    }

    /**
     * @param rankValue is the rank value of the cards the player strategy needs
     * @return all cards whose rank value matches the parameter
     */
    public ArrayList<Card> getCardsByRank(int rankValue) {
        ArrayList<Card> cards = new ArrayList<Card>();

        for (Card card : currentHand) {
            if (!isInMeld(card) && card.getRankValue() == rankValue) {
                cards.add(card);
            }
        }

        return cards;
    }

    /**
     * Checks to see if there are multiple cards with the same rank value to
     * form a potential set meld
     * @return a list of cards with the same rank value or an empty list if
     * no such cards exist
     */
    public List<Card> getPotentialSetMeld() {
        for (int i = 0; i < CARDS_PER_SUIT; i++) {
            if (cardsByRank[i] >= MIN_CARDS_PER_MELD) {
                return getCardsByRank(i);
            }
        }

        return null;
    }

    /**
     * Tries to make a run meld or set meld with the card passed in as a parameter
     * @param card the card the player strategy wants to form melds with
     * @return true if a meld can be formed with this card, false otherwise
     */
    public boolean makePotentialMelds(Card card) {
        if (Meld.buildRunMeld(getPotentialRunMeld(getSuitArrayOfCard(card))) != null) {
            playerMelds.add((Meld.buildRunMeld(getPotentialRunMeld(getSuitArrayOfCard(card)))));
            removeListOfCards(getPotentialRunMeld(getSuitArrayOfCard(card)));
            return true;
        } else if (Meld.buildSetMeld(getPotentialSetMeld()) != null) {
            playerMelds.add(Meld.buildSetMeld(getPotentialSetMeld()));
            removeListOfCards(getPotentialSetMeld());
            return true;
        }

        return false;
    }

    /**
     * Tries to form melds with the player's initial hand across all suits
     */
    public void makeInitialMelds() {

        List<Card> cardsInPotentialMeld = getPotentialRunMeld(spadesInHand);

        if (Meld.buildRunMeld(cardsInPotentialMeld) != null) {
            playerMelds.add(Meld.buildRunMeld(cardsInPotentialMeld));
            removeListOfCards(cardsInPotentialMeld);
        }

        cardsInPotentialMeld = getPotentialRunMeld(clubsInHand);

        if (Meld.buildRunMeld(cardsInPotentialMeld) != null) {
            playerMelds.add(Meld.buildRunMeld(cardsInPotentialMeld));
            removeListOfCards(cardsInPotentialMeld);
        }

        cardsInPotentialMeld = getPotentialRunMeld(heartsInHand);

        if (Meld.buildRunMeld(cardsInPotentialMeld) != null) {
            playerMelds.add(Meld.buildRunMeld(cardsInPotentialMeld));
            removeListOfCards(cardsInPotentialMeld);
        }

        cardsInPotentialMeld = getPotentialRunMeld(diamondsInHand);

        if (Meld.buildRunMeld(cardsInPotentialMeld) != null) {
            playerMelds.add(Meld.buildRunMeld(cardsInPotentialMeld));
            removeListOfCards(cardsInPotentialMeld);
        }

        cardsInPotentialMeld = getPotentialSetMeld();

        if (Meld.buildSetMeld(getPotentialSetMeld()) != null) {
            playerMelds.add(Meld.buildSetMeld(getPotentialSetMeld()));
            removeListOfCards(cardsInPotentialMeld);
        }
    }

    /**
     * Checks whether a potential discard can be used by the opponent to form a new run or set meld
     * or can be appended to an existing meld
     * @param card that could potentially be discarded
     * @return true if the card can be used to the opponent's benefit, false otherwise
     */
    public boolean isCardUsefulToOpponent(Card card) {
        opponent.addCard(card);

        boolean isRunMeld = Meld.buildRunMeld(opponent.getPotentialRunMeld(getSuitArrayOfCard(card))) != null;
        boolean isSetMeld = Meld.buildSetMeld(opponent.getPotentialSetMeld()) != null;

        opponent.removeCard(card);

        return (isRunMeld || isSetMeld || opponent.canAppendToExistingMelds(card) != null);
    }
}
