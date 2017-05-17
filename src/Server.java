import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

	// port connect default
	private static int PORT = 7788;

	public static void main(String args[]) {
		new Server();
	}

	/**
	 * default constructor
	 */
	public Server() {

		try {
			// create new socket server with listening port
			ServerSocket serverSocket = new ServerSocket(PORT);
			// number client connected
			int count = 1;
			while (true) {
				System.out.println("Waiting for connection .....");
				// accept one connect from client
				Socket socket = serverSocket.accept();
				// create new instance computer play with client player
				BlackjackPlayer run = new BlackjackPlayer(socket, count);
				// Increment number client connected
				count++;
				Thread thread = new Thread(run);
				thread.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

class BlackjackPlayer implements Runnable {

	private Socket socket;

	// client id
	private int clientNo;

	// win/loss percent
	private int win = 0;
	private int loss = 0;

	// cards first hand
	private List<Card> currentDeal1 = new ArrayList<Card>();

	// cards second hand
	private List<Card> currentDeal2 = new ArrayList<Card>();

	// cards of computer player
	private List<Card> currentServerDeal = new ArrayList<Card>();

	/**
	 * Construct with socket and client id
	 * 
	 * @param socket
	 * @param threadno
	 */
	public BlackjackPlayer(Socket socket, int threadno) {
		this.socket = socket;
		this.clientNo = threadno;
		this.win = 0;
		this.loss = 0;
	}

	@Override
	public void run() {

		try {
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
			// receive
			DataInputStream inputStream = new DataInputStream(socket.getInputStream());
			// send back client 'client id'
			sendMessage(outputStream, this.clientNo + "");

			Deck deck = null;
			boolean finished = false;
			while (!finished) {
				String msg = getMessage(inputStream);
				if (msg.equalsIgnoreCase("play")) {
					deck = new Deck();
					deck.shuffle();
					// get two cards for client
					Card[] cards = deck.dealTwoCard();
					currentDeal1.clear();
					currentDeal1.add(cards[0]);
					currentDeal1.add(cards[1]);
					sendMessage(outputStream, currentDeal1);
					sendMessage(outputStream, currentDeal2);

					// get two cards for server
					Card[] cards_server = deck.dealTwoCard();
					currentServerDeal.clear();
					currentServerDeal.add(cards_server[0]);
					currentServerDeal.add(cards_server[1]);

				} else if (msg.equalsIgnoreCase("deal")) {
					// get one card
					Card card = deck.deal();
					currentDeal1.add(card);
					sendMessage(outputStream, currentDeal1);

				} else if (msg.equalsIgnoreCase("deal2")) {
					// get one card
					Card card = deck.deal();
					currentDeal2.add(card);
					sendMessage(outputStream, currentDeal2);

				} else if (msg.equalsIgnoreCase("split")) {
					boolean cansplit = checkCanSplit(currentDeal1);
					if (cansplit) {
						Card c = currentDeal1.get(1);
						currentDeal2.add(c);
						currentDeal1.remove(1);
					}
					sendMessage(outputStream, currentDeal1);
					sendMessage(outputStream, currentDeal2);

					// check can split
				} else if (msg.equalsIgnoreCase("submit")) {
					// first check score of computer
					// if score verry low -> get more card
					boolean computer_blackjack = checkBlackjack(currentServerDeal);
					if (!computer_blackjack) {
						int score = getScoreFromListCards(currentServerDeal);
						boolean isDone = false;
						while (!isDone) {
							Card card = deck.deal();
							currentServerDeal.add(card);
							score = getScoreFromListCards(currentServerDeal);
							if ((score > 15 && score < 21) || score >= 21 || currentServerDeal.size() == 5) {
								isDone = true;
							}
						}
					}

					// check score client and server
					checkClientWinOrLoss(currentDeal1);
					checkClientWinOrLoss(currentDeal2);
					// send result back client with format win|loss
					sendMessage(outputStream, win + "|" + loss);
					sendMessage(outputStream, currentServerDeal);

				} else if (msg.equalsIgnoreCase("stop")) {
					// player stop
					finished = true;
				}
			}
			System.out.println("Client " + clientNo + " has stoped.");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * check two card can be split to two hands.
	 * 
	 * @param currentCards
	 * @return
	 */
	public boolean checkCanSplit(List<Card> currentCards) {
		boolean result = false;

		if (currentCards.size() == 2) {
			Card c1 = currentCards.get(0);
			Card c2 = currentCards.get(2);
			// A pair of aces gives the blackjack player or A pair of eight
			// gives the blackjack player
			if ((c1.getValue() == Card.EIGHT && c1.getValue().equals(c2.getValue()))
					|| (c1.getValue() == Card.ACE && c1.getValue().equals(c2.getValue()))) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * get score from list card
	 * 
	 * @param cards
	 * @return
	 */
	public int getScoreFromListCards(List<Card> cards) {
		int score = 0;
		for (int i = 0; i < cards.size(); i++) {
			score += cards.get(i).getScore();
		}
		return score;
	}

	/**
	 * check all hand or client is winner or losser
	 * 
	 * @param cards
	 */
	public void checkClientWinOrLoss(List<Card> cards) {
		// case hand empty card
		if (cards.isEmpty()) {
			return;
		}

		// check black jack client player
		boolean client1_blackjack = checkBlackjack(cards);

		// check blackjack computer player
		boolean computer_blacljack = checkBlackjack(currentServerDeal);

		// client player is blackjack
		if (client1_blackjack) {
			if (!computer_blacljack) {
				win += 1;
			}
		} else if (computer_blacljack) {
			// computer player is blackjack
			if (!client1_blackjack) {
				loss += 1;
			}
		} else {
			// not blackjack ->we should check score

			// score of client player
			int score_client = getScoreFromListCards(cards);

			// score of computer player
			int score_server = getScoreFromListCards(currentServerDeal);

			if (score_client != score_server) {
				if (score_client > 21) {
					if (score_server <= 21) {
						this.loss += 1;
					}
				} else {
					if (score_server > 21 || score_client > score_server) {
						this.win += 1;
					} else {
						this.loss += 1;
					}
				}
			}
		}

	}

	/**
	 * check list card in one hand is black jack
	 * 
	 * @param cards
	 * @return
	 */
	private boolean checkBlackjack(List<Card> cards) {

		boolean result = false;
		// only check for case two cards
		if (cards.size() == 2) {
			Card c1 = cards.get(0);
			Card c2 = cards.get(0);
			// black jack when hand has one card ace and one face card.
			if ((c1.getValue().equals(Card.ACE) && (Card.FACE_CARDS.contains(c2.getValue())))
					|| (c2.getValue().equals(Card.ACE) && (Card.FACE_CARDS.contains(c1.getValue())))) {
				result = true;
			}

		}
		// return reusult
		return result;

	}

	/**
	 * send list card to client
	 * 
	 * @param outputStream
	 * @param cards
	 * @throws IOException
	 */
	public void sendMessage(DataOutputStream outputStream, List<Card> cards) throws IOException {
		String msg = "";
		for (int i = 0; i < cards.size(); i++) {
			msg += cards.get(i).getValue() + "|" + cards.get(i).getSuitAsString() + "|";
		}
		outputStream.writeUTF(msg);
		System.out.println("Server sent to client  " + clientNo + ": " + msg);
	}

	/**
	 * send message to client
	 * 
	 * @param outputStream
	 * @param msg
	 * @throws IOException
	 */
	public void sendMessage(DataOutputStream outputStream, String msg) throws IOException {
		outputStream.writeUTF(msg);
		System.out.println("Server sent to client  " + clientNo + ": " + msg);
	}

	/**
	 * receive message from client
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public String getMessage(DataInputStream inputStream) throws IOException {
		String msg = inputStream.readUTF();
		System.out.println("Server receive from client " + clientNo + ": " + msg);
		return msg;

	}

}
