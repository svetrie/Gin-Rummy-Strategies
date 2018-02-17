package com.example;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirstPlayerStrategy implements PlayerStrategy {

    private static final int CARDS_PER_SUIT = 13;

    private List<Meld> playerMelds;

    private List<Card> currentHand;
    private List<Card> spadesInHand;
    private List<Card> clubsInHand;
    private List<Card> heartsInHand;
    private List<Card> diamondsInHand;
    private int[] cardRanks;

    private List<Card> discardPile;
    private List<Card> opponentsCards;

    public FirstPlayerStrategy() {
        currentHand = new ArrayList<Card>();
        spadesInHand = new ArrayList<Card>();
        clubsInHand = new ArrayList<Card>();
        heartsInHand = new ArrayList<Card>();
        diamondsInHand = new ArrayList<Card>();
        cardRanks = new int[CARDS_PER_SUIT];
    }

    public void addToSuit(Card card) {

        if (card.getSuit().equals(Card.CardSuit.CLUBS)) {
            clubsInHand.add(card);
        } else if (card.getSuit().equals(Card.CardSuit.SPADES)) {
            spadesInHand.add(card);
        } else if (card.getSuit().equals(Card.CardSuit.DIAMONDS)) {
            diamondsInHand.add(card);
        } else if (card.getSuit().equals(Card.CardSuit.HEARTS)) {
            heartsInHand.add(card);
        }
    }

    public void addToCardRank(Card card) {
        cardRanks[card.getRankValue()]++;
    }


    @Override
    public void receiveInitialHand(List<Card> hand) {
        currentHand = hand;

        for (Card card : hand) {
            addToSuit(card);
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
}
