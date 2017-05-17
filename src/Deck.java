public class Deck {
	/**
	 * An array of 52 cards
	 */
	private static Card[] deck = new Card[52];

	private int cardsUsed;

	static {
		int count = 0;
		String arr[] = new String[] { Card.ACE, Card.TWO, Card.THREE, Card.FOUR, Card.FIVE, Card.SIX, Card.SEVEN,
				Card.EIGHT, Card.NINE, Card.TEN, Card.JACK, Card.QUEEN, Card.KING };

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < 4; j++) {
				deck[count] = new Card(arr[i]);
				if (j == 0)
					deck[count].setSuit(Suit.CLUBS);
				else if (j == 1) {
					deck[count].setSuit(Suit.DIAMONDS);
				} else if (j == 2) {
					deck[count].setSuit(Suit.HEARTS);
				} else {
					deck[count].setSuit(Suit.SPADES);
				}
				count++;
			}
		}
	}

	/**
	 * Put all the used cards back into the deck (if any), and shuffle the deck
	 * into a random order.
	 */
	public void shuffle() {
		for (int i = deck.length - 1; i > 0; i--) {
			int rand = (int) (Math.random() * (i + 1));
			Card temp = deck[i];
			deck[i] = deck[rand];
			deck[rand] = temp;
		}
		cardsUsed = 0;
	}

	/**
	 * Removes the next card from the deck and return it
	 * 
	 * @return
	 */
	public Card deal() {
		if (cardsUsed == deck.length)
			throw new IllegalStateException("No cards are left in the deck.");
		cardsUsed++;
		return deck[cardsUsed - 1];

	}

	/**
	 * get two card from deck
	 * 
	 * @return
	 */
	public Card[] dealTwoCard() {
		Card[] cards = new Card[2];
		cards[0] = deal();
		cards[1] = deal();
		return cards;
	}

}
