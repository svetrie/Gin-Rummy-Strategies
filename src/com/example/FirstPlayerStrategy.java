package com.example;

import java.util.ArrayList;
import java.util.List;

public class FirstPlayerStrategy implements PlayerStrategy {

    private List<Card> currentHand;

    @Override
    public void receiveInitialHand(List<Card> hand) {
        currentHand = hand;
    }

    @Override
    public boolean willTakeTopDiscard(Card card) {
        return false;
    }

    @Override
    public Card drawAndDiscard(Card drawnCard) {
        return null;
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
