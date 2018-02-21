import com.example.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;

public class PlayerStrategyTester {

    public Card retrieveSixOfHearts() {
        for (Card card : deckOfCards) {
            if (card.getSuit().equals(Card.CardSuit.HEARTS)
                    && card.getRank().equals(Card.CardRank.SIX)) {
                return card;
            }
        }
        return null;
    }

    public Card retrieveEightOfHearts() {
        for (Card card : deckOfCards) {
            if (card.getSuit().equals(Card.CardSuit.HEARTS)
                    && card.getRank().equals(Card.CardRank.EIGHT)) {
                return card;
            }
        }
        return null;
    }

    public Card retrieveNineOfClubs() {
        for (Card card : deckOfCards) {
            if (card.getSuit().equals(Card.CardSuit.CLUBS)
                    && card.getRank().equals(Card.CardRank.NINE)) {
                return card;
            }
        }
        return null;
    }

    public ArrayList<Card> initializePlayersHand() {
        ArrayList<Card> playersHand = new ArrayList<>();

        for (Card card: deckOfCards) {

            if (card.getRank().equals(Card.CardRank.TWO)) {
                playersHand.add(card);
            } else if (card.getSuit().equals(Card.CardSuit.HEARTS)) {

                if(card.getRank().equals(Card.CardRank.FIVE)){
                    playersHand.add(card);
                } else if (card.getRank().equals(Card.CardRank.SEVEN)) {
                    playersHand.add(card);
                }
            }
        }

        //playersHand.add(sixOfHearts);
        return playersHand;
    }


    AggressivePlayerStrategy strategy;
    ArrayList<Card> deckOfCards;
    Card sixOfHearts;

    @Before
    public void setUp() {
        strategy = new AggressivePlayerStrategy();
        deckOfCards = new ArrayList<>(Card.getAllCards());
        sixOfHearts = retrieveSixOfHearts();
       // eightOfHearts = retrieveEightOfHearts();
    }

    @Test
    public void getSuitArrayOfCardTest() {
        assertArrayEquals(strategy.getHeartsInHand(), strategy.getSuitArrayOfCard(sixOfHearts));
    }

    @Test
    public void addCardTest() {
        strategy.addCard(sixOfHearts);

        assertEquals(sixOfHearts, strategy.getHeartsInHand()[5]);
        assertEquals(1, strategy.getCardsByRank()[5]);
    }

    @Test
    public void removeCardTest() {
        strategy.addCard(sixOfHearts);
        strategy.removeCard(sixOfHearts);

        assertEquals(null, strategy.getHeartsInHand()[5]);
        assertEquals(0, strategy.getCardsByRank()[5]);
    }

    @Test
    public void recieveInitialHandTest() {
        strategy.receiveInitialHand(initializePlayersHand());
        assertTrue(strategy.getCurrentHand().containsAll(initializePlayersHand()));
    }

    @Test
    public void makeInitialMeldsTest() {
        strategy.receiveInitialHand(initializePlayersHand());
        strategy.makeInitialMelds();

        assertTrue(strategy.getMelds().size() == 1 && strategy.getMelds().get(0) instanceof SetMeld);
    }

    @Test
    public void takeFromDiscardPileToFormMeldTest() {
        strategy.receiveInitialHand(initializePlayersHand());
        assertTrue(strategy.willTakeTopDiscard(sixOfHearts));
    }

    @Test
    public void takeFromDiscardPileToAppendToMeldTest() {
        strategy.receiveInitialHand(initializePlayersHand());
        strategy.addCard(sixOfHearts);

        assertTrue(strategy.willTakeTopDiscard(retrieveEightOfHearts()));
    }

    @Test
    public void dontTakeFRomDiscardPileTest() {
        assertTrue(!strategy.willTakeTopDiscard(retrieveNineOfClubs()));
    }

    @Test
    public void getHighestDeadwood() {
        strategy.receiveInitialHand(initializePlayersHand());
        assertEquals(7, strategy.getHighestDeadwood().getPointValue());
    }

    @Test
    public void getTotalDeadwood() {
        strategy.receiveInitialHand(initializePlayersHand());
        assertEquals(12, strategy.getTotalDeadwood());
    }


}
