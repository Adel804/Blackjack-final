import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Card {

	public static final String ACE = "ace";
	public static final String TWO = "two";
	public static final String THREE = "three";
	public static final String FOUR = "four";
	public static final String FIVE = "five";
	public static final String SIX = "six";
	public static final String SEVEN = "seven";
	public static final String EIGHT = "eight";
	public static final String NINE = "nine";
	public static final String TEN = "ten";
	public static final String JACK = "jack";
	public static final String QUEEN = "queen";
	public static final String KING = "king";

	public static Map<String, Integer> MAP_SCORES = new HashMap<String, Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put(ACE, 1);
			put(TWO, 2);
			put(THREE, 3);
			put(FOUR, 4);
			put(FIVE, 5);
			put(SIX, 6);
			put(SEVEN, 7);
			put(EIGHT, 8);
			put(NINE, 9);
			put(TEN, 10);
			put(JACK, 10);
			put(QUEEN, 10);
			put(KING, 10);

		}
	};
	public static List<String> FACE_CARDS = Arrays.asList(JACK, QUEEN, KING);

	private Random generator = new Random();

	/**
	 * The card's value. For a normal cards, this is one of the values 1 through
	 * 13, with 1 representing ACE. For a JOKER, the value can be anything. The
	 * value cannot be changed after the card is constructed.
	 */
	private String value;
	/**
	 * This card's suit, one of the constants SPADES, HEARTS, DIAMONDS, CLUBS
	 * The suit cannot be changed after the card is constructed.
	 */
	private Suit suit;

	private int score;

	public Card(String value) {
		this.value = value;
		this.suit = Suit.values()[generator.nextInt(3)];
		this.score = MAP_SCORES.get(value);
	}

	public Card(String value, Suit suit) {
		this.value = value;
		this.suit = suit;
		this.score = MAP_SCORES.get(value);
	}

	public int getScore() {
		return score;
	}

	public String getSuitAsString() {
		return suit.name();
	}

	/**
	 * get suit of this card
	 * 
	 * @return
	 */
	public Suit getSuit() {
		return suit;
	}

	/**
	 * set suit for this card
	 * 
	 * @param suit
	 */
	public void setSuit(Suit suit) {
		this.suit = suit;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Card) {
			Card card = (Card) obj;
			if (card.value.equals(this.value) && card.suit == this.suit)
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Card [value=" + value + ", suit=" + suit + "]";
	}

	public String getValue() {
		return value;
	}

}

enum Suit {
	SPADES, HEARTS, DIAMONDS, CLUBS

}
