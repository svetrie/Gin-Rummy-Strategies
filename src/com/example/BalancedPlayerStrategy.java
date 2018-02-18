package com.example;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BalancedPlayerStrategy implements PlayerStrategy {

    private static final int CARDS_PER_SUIT = 13;

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

    public void addToSuit(Card card) {

        if (card.getSuit().equals(Card.CardSuit.CLUBS)) {
            clubsInHand[card.getRankValue()] = card;
        } else if (card.getSuit().equals(Card.CardSuit.SPADES)) {
            spadesInHand[card.getRankValue()] = card;
        } else if (card.getSuit().equals(Card.CardSuit.DIAMONDS)) {
            diamondsInHand[card.getRankValue()] = card;
        } else if (card.getSuit().equals(Card.CardSuit.HEARTS)) {
            heartsInHand[card.getRankValue()] = card;
        }
    }

    public void addToCardsByRank(Card card) {
        cardsByRank[card.getRankValue()]++;
    }


    @Override
    public void receiveInitialHand(List<Card> hand) {
        currentHand = hand;

        for (Card card : hand) {
            addToSuit(card);
            addToCardsByRank(card);
        }
    }

    @Override
    public boolean willTakeTopDiscard(Card card) {
        return false;
    }

    @Override
    public Card drawAndDiscard(Card drawnCard) {

    }

    @Override
    public boolean knock() {
        return false;
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

    public List<Card> getPotentialRunMeld(Card[] suitInHand) {
        int consecutiveRanks = 0;
        List<Card> potentialMeld = new ArrayList<>();

        for (int i = 1; i < CARDS_PER_SUIT; i++) {

            if (suitInHand[i] != null && suitInHand[i]
                    .getRankValue() == suitInHand[i - 1].getRankValue() - 1) {

                if (consecutiveRanks == 0) {
                    consecutiveRanks += 2;
                } else {
                    consecutiveRanks++;
                }

                if (consecutiveRanks == 3) {
                    potentialMeld.add(suitInHand[i - 2]);
                    potentialMeld.add(suitInHand[i - 1]);
                }

                potentialMeld.add(suitInHand[i]);

            } else {
                consecutiveRanks = 0;
            }
        }

        return potentialMeld;
    }

    public List<Card> getPotentialSetMeld() {
        for (int i : cardsByRank) {
            if (i >= 3) {
                return getCardsByRank(i);
            }
        }

        return null;
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
}
