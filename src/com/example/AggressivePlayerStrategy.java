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

    private List<Card> discardPile;

    public AggressivePlayerStrategy() {
        playerMelds = new ArrayList<>();
        currentHand = new ArrayList<>();
        spadesInHand = new Card[CARDS_PER_SUIT];
        clubsInHand = new Card[CARDS_PER_SUIT];
        heartsInHand = new Card[CARDS_PER_SUIT];
        diamondsInHand = new Card[CARDS_PER_SUIT];
        cardsByRank = new int[CARDS_PER_SUIT];
    }

    public void removeCard(Card card) {
        if (card == null) {
            System.out.println("card is null");
        }

        getSuitArrayOfCard(card)[card.getRankValue()] = null;
        cardsByRank[card.getRankValue()]--;
    }

    public void removeListOfCards(List<Card> cards) {
        for (Card card : cards) {
            removeCard(card);
        }
    }

    public void addCard(Card card) {
        getSuitArrayOfCard(card)[card.getRankValue()] = card;
        cardsByRank[card.getRankValue()]++;
    }

    public Card[] getSuitArrayOfCard(Card card) {
        if (card == null) {
            System.out.println("Card is null");
        }

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

    public Meld canAppendToExistingMelds(Card card) {
        for (Meld meld : playerMelds) {
            if (meld.canAppendCard(card)) {
                return meld;
            }
        }
        return null;
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
        currentHand = new ArrayList<>(hand);

        if (currentHand.size() > 10) {
            System.out.print("Initial Hand exceeds max length:" + currentHand.size());
        }

        for (Card card : hand) {
            addCard(card);
        }

        makeInitialMelds();
    }

    @Override
    public boolean willTakeTopDiscard(Card card) {
        addCard(card);

        if (currentHand.size() > 10) {
            System.out.println("Hand exceeds max length:" + currentHand.size());
        }

        boolean takeCard = (Meld.buildRunMeld(getPotentialRunMeld(getSuitArrayOfCard(card))) != null
                || Meld.buildSetMeld(getPotentialSetMeld()) != null || canAppendToExistingMelds(card) != null);

        removeCard(card);

      /*  if (takeCard) {
            System.out.println("Not a meld yet");
        } */

        return takeCard;
    }

    @Override
    public Card drawAndDiscard(Card drawnCard) {
        currentHand.add(drawnCard);
        addCard(drawnCard);

        if (currentHand.size() > 10) {
            System.out.println("Hand exceeds max length:" + currentHand.size());
        }

        if(!makePotentialMelds(drawnCard) && canAppendToExistingMelds(drawnCard) != null) {
            canAppendToExistingMelds(drawnCard).appendCard(drawnCard);
            removeCard(drawnCard);
        }

        Card discard = getHighestDeadwood();

        if (discard == null) {
            System.out.println("No highest deadwood");

            for (Card card : currentHand) {
                System.out.println(isInMeld(card));
            }
        }

        //System.out.println(drawnCard.getSuit() + "-" + drawnCard.getRank());
/*
        for (Card card : spadesInHand) {
            System.out.println(card.getSuit() + "-" + card.getRank() + ", ");
        }

        for (Card card : clubsInHand) {
            System.out.println(card.getSuit() + "-" + card.getRank() + ", ");
        }

        for (Card card : diamondsInHand) {
            System.out.print(card.getSuit() + "-" + card.getRank() + ", ");
        }

        for (Card card : heartsInHand) {
            System.out.print(card.getSuit() + "-" + card.getRank() + ", ");
        }

        for (int i : cardsByRank) {
            System.out.print(i + ", ");
        }
*/
        currentHand.remove(discard);
        removeCard(discard);

        if (currentHand.size() <= 10) {
            System.out.println("Hand doesn't exceed max length:" + currentHand.size());
        }

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

    }

    @Override
    public void opponentEndRoundFeedback(List<Card> opponentHand, List<Meld> opponentMelds) {

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


    public Card getRemovableCardInMeld() {
        for (Card card : currentHand) {

            for (Meld meld : getMelds()) {

                if (meld.canRemoveCard(card)) {
                    meld.removeCard(card);
                    return card;
                }
            }
        }

        for (Meld meld : getMelds()) {
            for (Card card: meld.getCards()) {
                System.out.print(card.getSuit() + "-" + card.getRank() + " ,");
            }

            System.out.println();
        }

        return null;
    }


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

                /*
                if (potentialMeld.size() >= 3) {
                    System.out.println("Potential run meld");
                    for (Card card : potentialMeld) {
                        System.out.println(card.getSuit());
                        System.out.println(card.getRank());
                    }
                }*/


                if (potentialMeld.size() >= 3 && Meld.buildRunMeld(potentialMeld) != null) {
                    System.out.println("Can make run meld!");
                }


            } else {
                consecutiveCards = 0;
            }
        }

        return potentialMeld;
    }

    public ArrayList<Card> getCardsByRank(int rankValue) {
        ArrayList<Card> cards = new ArrayList<Card>();

        for (Card card : currentHand) {
            if (!isInMeld(card) && card.getRankValue() == rankValue) {
                cards.add(card);
            }
        }

        return cards;
    }

    public List<Card> getPotentialSetMeld() {
        for (int i = 0; i < CARDS_PER_SUIT; i++) {
            if (cardsByRank[i] >= MIN_CARDS_PER_MELD) {
               if (Meld.buildSetMeld(getCardsByRank(i)) != null) {
                    System.out.println("Can make set meld");
                }
                return getCardsByRank(i);
            }
        }

        return null;
    }

    // tries to make new run meld or set meld with card. Will return true if a meld was possible
    public boolean makePotentialMelds(Card card) {
        if (Meld.buildRunMeld(getPotentialRunMeld(getSuitArrayOfCard(card))) != null) {
            //System.out.println("Trying to add run meld");
            playerMelds.add((Meld.buildRunMeld(getPotentialRunMeld(getSuitArrayOfCard(card)))));
            System.out.println("Made run meld");
            removeListOfCards(getPotentialRunMeld(getSuitArrayOfCard(card)));
            return true;
        } else if (Meld.buildSetMeld(getPotentialSetMeld()) != null) {
            //System.out.println("Trying to add set meld");
            playerMelds.add(Meld.buildSetMeld(getPotentialSetMeld()));
            System.out.println("Made set meld");
            removeListOfCards(getPotentialSetMeld());
            return true;
        }
        return false;
    }

    // checks to see if any melds can be made with the initial hand dealt to the player
    public void makeInitialMelds() {

        List<Card> cardsInPotentialMeld = getPotentialRunMeld(spadesInHand);

        if (Meld.buildRunMeld(cardsInPotentialMeld) != null) {
            playerMelds.add(Meld.buildRunMeld(cardsInPotentialMeld));
            removeListOfCards(cardsInPotentialMeld);
            System.out.println("Made run meld");
        }

        cardsInPotentialMeld = getPotentialRunMeld(clubsInHand);

        if (Meld.buildRunMeld(cardsInPotentialMeld) != null) {
            playerMelds.add(Meld.buildRunMeld(cardsInPotentialMeld));
            removeListOfCards(cardsInPotentialMeld);
            System.out.println("Made run meld");
        }

        cardsInPotentialMeld = getPotentialRunMeld(heartsInHand);

        if (Meld.buildRunMeld(cardsInPotentialMeld) != null) {
            playerMelds.add(Meld.buildRunMeld(cardsInPotentialMeld));
            removeListOfCards(cardsInPotentialMeld);
            System.out.println("Made run meld");
        }

        cardsInPotentialMeld = getPotentialRunMeld(diamondsInHand);

        if (Meld.buildRunMeld(cardsInPotentialMeld) != null) {
            playerMelds.add(Meld.buildRunMeld(cardsInPotentialMeld));
            removeListOfCards(cardsInPotentialMeld);
            System.out.println("Made run meld");
        }

        cardsInPotentialMeld = getPotentialSetMeld();

        if (Meld.buildSetMeld(getPotentialSetMeld()) != null) {
            playerMelds.add(Meld.buildSetMeld(getPotentialSetMeld()));
            removeListOfCards(cardsInPotentialMeld);
            System.out.println("Made set meld");
        }
    }
}
