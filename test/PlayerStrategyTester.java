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


    AggressivePlayerStrategy strategy;
    ArrayList<Card> deckOfCards;
    Card sixOfHearts;

    @Before
    public void setUp() {
        strategy = new AggressivePlayerStrategy();
        deckOfCards = new ArrayList<>(Card.getAllCards());
        sixOfHearts = retrieveSixOfHearts();
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

}
