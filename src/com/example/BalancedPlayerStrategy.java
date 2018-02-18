package com.example;

import java.util.ArrayList;
import java.util.List;

public class BalancedPlayerStrategy implements PlayerStrategy {

    private static final int CARDS_PER_SUIT = 13;
    private static final int MIN_CARDS_PER_MELD = 3;
    private static final int MAX_DEADWOOD_TOTAL_TO_KNOCK = 5;

    private List<Meld> playerMelds;

    private List<Card> currentHand;
    private Card[] spadesInHand;
    private Card[] clubsInHand;
    private Card[] heartsInHand;
    private Card[] diamondsInHand;
    private int[] cardsByRank;

    private List<Card> discardPile;
    private List<Card> opponentsCards;

    public BalancedPlayerStrategy() {
        currentHand = new ArrayList<>();
        spadesInHand = new Card[CARDS_PER_SUIT];
        clubsInHand = new Card[CARDS_PER_SUIT];
        heartsInHand = new Card[CARDS_PER_SUIT];
        diamondsInHand = new Card[CARDS_PER_SUIT];
        cardsByRank = new int[CARDS_PER_SUIT];
    }

    public void removeCard(Card card) {
        getSuitArrayOfCard(card)[card.getRankValue()] = null;
        cardsByRank[card.getRankValue()]--;
    }

    public void addCard(Card card) {
        getSuitArrayOfCard(card)[card.getRankValue()] = card;
        cardsByRank[card.getRankValue()]++;
    }

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

    //might want to make this method return the meld the card can be appended to
    public boolean canAppendToExistingMelds(Card card) {
        for (Meld meld : playerMelds) {
            if (meld.canAppendCard(card)) {
                return true;
            }
        }
        return false;
    }

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
        currentHand = hand;

        for (Card card : hand) {
            addCard(card);
        }
    }

    @Override
    public boolean willTakeTopDiscard(Card card) {
        addCard(card);

        boolean takeCard = (getPotentialRunMeld(getSuitArrayOfCard(card)) != null
                || getPotentialSetMeld() != null || canAppendToExistingMelds(card));

        removeCard(card);
        return takeCard;
    }

    @Override
    public Card drawAndDiscard(Card drawnCard) {
        currentHand.add(drawnCard);
        addCard(drawnCard);

        // check if card can be used to form new melds or appended to existing melds
    }

    @Override
    public boolean knock() {
        return (getTotalDeadwood() <= MAX_DEADWOOD_TOTAL_TO_KNOCK);
    }

    @Override
    public List<Meld> getMelds() {
        return null;
    }

    @Override
    public void opponentEndTurnFeedback(boolean drewDiscard, Card previousDiscardTop, Card opponentDiscarded) {

    }

    @Override
    public void opponentEndRoundFeedback(List<Card> opponentHand, List<Meld> opponentMelds) {

    }

    @Override
    public void reset() {

    }

    public int getTotalDeadwood() {
        int totalDeadwood =  0;

        for (Card card : currentHand) {
            if (!isInMeld(card)) {
                totalDeadwood += card.getPointValue();
            }
        }

        return totalDeadwood;
    }

    public Card getHighestDeadwood() {
        Card deadwood = null;

        for (Card card : currentHand) {

            if(deadwood == null && !isInMeld(card)) {
                deadwood = card;
            }

            if (deadwood.getPointValue() < card.getPointValue()) {
                deadwood = card;
            }
        }

        return deadwood;
    }

    public List<Card> getPotentialRunMeld(Card[] suit) {
        int consecutiveRanks = 0;
        List<Card> potentialMeld = new ArrayList<>();

        for (int i = 1; i < CARDS_PER_SUIT; i++) {

            if (suit[i] != null && suit[i]
                    .getRankValue() == suit[i - 1].getRankValue() - 1) {

                if (consecutiveRanks == 0) {
                    consecutiveRanks += 2;
                } else {
                    consecutiveRanks++;
                }

                if (consecutiveRanks == MIN_CARDS_PER_MELD) {
                    potentialMeld.add(suit[i - 2]);
                    potentialMeld.add(suit[i - 1]);
                }

                potentialMeld.add(suit[i]);

            } else {
                consecutiveRanks = 0;
            }
        }

        return potentialMeld;
    }

    public ArrayList<Card> getCardsByRank(int rankValue) {
        ArrayList<Card> cards = new ArrayList<Card>();

        for (Card card : currentHand) {
            if (card.getRankValue() == rankValue) {
                cards.add(card);
            }
        }

        return cards;
    }

    public List<Card> getPotentialSetMeld() {
        for (int i : cardsByRank) {
            if (i >= MIN_CARDS_PER_MELD) {
                return getCardsByRank(i);
            }
        }

        return null;
    }

    // try to make run meld or set meld with card
    public void makePotentialMelds(Card card) {

    }

    // can change to boolean later to see if taking a single card can help create any melds
    public void makeInitialMelds() {

        if (Meld.buildRunMeld(getPotentialRunMeld(spadesInHand)) != null) {
            playerMelds.add(Meld.buildRunMeld(getPotentialRunMeld(spadesInHand)));
        } else if (Meld.buildRunMeld(getPotentialRunMeld(clubsInHand)) != null) {
            playerMelds.add(Meld.buildRunMeld(getPotentialRunMeld(clubsInHand)));
        } else if (Meld.buildRunMeld(getPotentialRunMeld(heartsInHand)) != null) {
            playerMelds.add(Meld.buildRunMeld(getPotentialRunMeld(heartsInHand)));
        } else if (Meld.buildRunMeld(getPotentialRunMeld(diamondsInHand)) != null) {
            playerMelds.add(Meld.buildRunMeld(diamondsInHand));
        } else if (Meld.buildSetMeld(getPotentialSetMeld()) != null) {
            playerMelds.add(Meld.buildSetMeld(getPotentialSetMeld()));
        }
    }
}
