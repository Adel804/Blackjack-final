import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Client extends Application {

	// button play send message 'play' to server
	private Button btnPlay = new Button("Play");

	// button play send message 'split' to server (for split cards to two hand)
	private Button btnSplit = new Button("Split");

	// button play send message 'deal' to get one card
	private Button btnDeal = new Button("Deal");
	// button play send message 'deal' to get one card
	private Button btnDeal2 = new Button("Deal2");

	// button play send message 'submit' to end round and get win/loss percent
	private Button btnSubmit = new Button("Submit");

	// define max core of blackjack
	private static final int MAX_SCORE = 21;
	// define max number of card can be one hand
	private static final int MAX_NUMBER_CARD = 5;

	// store information of this client player
	private Label lb_score = new Label("");
	private Label lb_client = new Label("");
	private Label lb_winloss = new Label("");

	// computer player
	private Label lb_score_computer = new Label("");

	// store value name of card
	private String[] images_name = new String[] { "ace", "two", "three", "four", "five", "six", "seven", "eight",
			"nine", "ten", "jack", "queen", "king" };
	// store path of image all cards
	private Map<Card, String> IMAGES_CARD = new HashMap<Card, String>();

	// port connect default
	private static int PORT = 7788;

	// server
	private static String HOST = "localhost";

	// send data to server
	private DataOutputStream outputStream = null;

	// receive data from server
	private DataInputStream inputStream = null;

	private HBox hboxClient = new HBox();
	private HBox hboxServer = new HBox();

	// store cards of first hand
	private List<Card> clientCurrentCards = new ArrayList<Card>();

	// store cards of second hand (only use in case split)
	private List<Card> clientCurrentCards2 = new ArrayList<Card>();

	// store cards of server ( show when client end of round)
	private List<Card> serverCurrentCards = new ArrayList<Card>();

	/**
	 * create graphics for client
	 * 
	 * @param root
	 */
	public void initGUI(BorderPane root) {
		// init cards with images
		for (int i = 0; i < images_name.length; i++) {
			Suit[] suits = Suit.values();
			for (int j = 0; j < suits.length; j++) {
				String img = "images/" + images_name[i] + "_of_" + suits[j].name().toLowerCase() + ".png";
				Card c = new Card(images_name[i], suits[j]);
				IMAGES_CARD.put(c, img);

			}
		}

		// information of server

		hboxServer.setAlignment(Pos.CENTER);
		hboxServer.setSpacing(5);

		HBox hboxCenter = new HBox(10);
		hboxCenter.setAlignment(Pos.CENTER);

		ImageView imageView = new ImageView(new File("images/blue.jpg").toURI().toString());
		scaleImage(imageView, 0.5);

		VBox vboxButton = new VBox(5);
		vboxButton.setAlignment(Pos.CENTER);
		btnDeal.setDisable(true);
		btnDeal2.setDisable(true);

		// add buttons
		vboxButton.getChildren().addAll(btnPlay, btnDeal, btnDeal2);

		hboxCenter.getChildren().addAll(imageView, vboxButton);

		// client add two cards
		hboxClient.setSpacing(5);
		hboxClient.setAlignment(Pos.CENTER);

		// add all to root panel
		root.setTop(hboxServer);
		root.setCenter(hboxCenter);
		root.setBottom(hboxClient);
	}

	/**
	 * convert message receive from server to list of cards
	 * 
	 * @param msg
	 * @return
	 */
	public List<Card> convertToCard(String msg) {
		List<Card> lst = new ArrayList<Card>();
		// example format : three|DIAMONDS|eight|CLUBS -> two card
		if (msg != null && !msg.isEmpty()) {
			String arr[] = msg.split("\\|");

			for (int i = 0; i < arr.length; i += 2) {
				Suit s = Suit.valueOf(arr[i + 1]);
				Card c = new Card(arr[i], s);
				lst.add(c);
			}
		}
		return lst;
	}

	/**
	 * show cards of server receive from server
	 */
	public void showServerCard() {
		// computer player
		hboxServer.getChildren().clear();
		VBox vBoxServerInfo = new VBox(5);
		vBoxServerInfo.getChildren().addAll(new Label(), lb_score_computer);

		// get score from cards
		int score_server = 0;
		for (int i = 0; i < serverCurrentCards.size(); i++) {
			score_server += serverCurrentCards.get(i).getScore();
		}

		lb_score_computer.setText(score_server + "");
		VBox vboxServerInfoLabel = new VBox(5);
		Label lb_s = new Label("Computer");
		Label lb_s_score = new Label("Score Current  :");

		// add component to panel
		vboxServerInfoLabel.getChildren().addAll(lb_s, lb_s_score);

		hboxServer.getChildren().add(vboxServerInfoLabel);
		hboxServer.getChildren().add(vBoxServerInfo);

		for (int i = 0; i < serverCurrentCards.size(); i++) {
			ImageView img1 = new ImageView(new File(getImageOfCard(serverCurrentCards.get(i))).toURI().toString());
			scaleImage(img1, 0.15);
			hboxServer.getChildren().add(img1);
		}
	}

	/**
	 * Show card client received from server
	 * 
	 * @param msg
	 */
	public void showClientCard() {
		//
		// computer player
		hboxServer.getChildren().clear();
		VBox vBoxServerInfo = new VBox(5);
		vBoxServerInfo.getChildren().addAll(new Label(), lb_score_computer);

		int score_server = 0;
		for (int i = 0; i < serverCurrentCards.size(); i++) {
			score_server += serverCurrentCards.get(i).getScore();
		}

		lb_score_computer.setText(score_server + "");
		VBox vboxServerInfoLabel = new VBox(5);
		Label lb_s = new Label("Computer");
		Label lb_s_score = new Label("Score Current  :");

		vboxServerInfoLabel.getChildren().addAll(lb_s, lb_s_score);
		hboxServer.getChildren().add(vboxServerInfoLabel);
		hboxServer.getChildren().add(vBoxServerInfo);

		for (int i = 0; i < 2; i++) {
			ImageView img1 = new ImageView(new File("images/blue.jpg").toURI().toString());
			scaleImage(img1, 0.5);
			hboxServer.getChildren().add(img1);
		}

		// client player
		hboxClient.getChildren().clear();
		VBox vBoxClienInfo = new VBox(5);
		vBoxClienInfo.getChildren().addAll(lb_client, lb_winloss, lb_score);

		int score = 0;
		for (int i = 0; i < clientCurrentCards.size(); i++) {
			score += clientCurrentCards.get(i).getScore();
		}
		lb_score.setText("" + score);
		VBox vboxClientInfoLabel = new VBox(5);
		Label lb_l_client_id = new Label("Client ID      :");
		Label lb_l_winloss = new Label("Win/loss       :");
		Label lb_l_score = new Label("Score Current  :");

		vboxClientInfoLabel.getChildren().addAll(lb_l_client_id, lb_l_winloss, lb_l_score);
		hboxClient.getChildren().add(vboxClientInfoLabel);
		hboxClient.getChildren().add(vBoxClienInfo);

		// cards of first hand
		for (int i = 0; i < clientCurrentCards.size(); i++) {
			ImageView img1 = new ImageView(new File(getImageOfCard(clientCurrentCards.get(i))).toURI().toString());
			scaleImage(img1, 0.15);
			hboxClient.getChildren().add(img1);
		}

		// separate two hand by whitespace
		hboxClient.getChildren().add(new Label("             "));

		// cards of second hand
		for (int i = 0; i < clientCurrentCards2.size(); i++) {
			ImageView img1 = new ImageView(new File(getImageOfCard(clientCurrentCards2.get(i))).toURI().toString());
			scaleImage(img1, 0.15);
			hboxClient.getChildren().add(img1);
		}

		hboxClient.getChildren().add(btnSplit);
		hboxClient.getChildren().add(btnSubmit);

		// check score current with max score is 21
		if (score >= MAX_SCORE || clientCurrentCards.size() >= MAX_NUMBER_CARD) {
			btnDeal.setDisable(true);
			btnSubmit.setDisable(false);
			btnPlay.setDisable(true);
		}

	}

	/**
	 * get image based on card information
	 * 
	 * @param card
	 * @return
	 */
	private String getImageOfCard(Card card) {
		String path = "";
		for (Entry<Card, String> entry : IMAGES_CARD.entrySet()) {
			Card k = entry.getKey();
			// card matching found
			if (k.getValue().equals(card.getValue()) && k.getSuit().name().equals(card.getSuit().name())) {
				path = entry.getValue();
				break;
			}
		}
		return path;
	}

	/**
	 * set event for buttons in this program
	 */
	public void initEvent() {
		// action for button 'Play' -> get two cards
		btnPlay.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				try {
					// sent request to server get two card
					sendMessage("play");
					// receive response from server and display card on view
					String msg1 = getMessage();
					String msg2 = getMessage();
					clientCurrentCards.clear();
					clientCurrentCards.addAll(convertToCard(msg1));

					clientCurrentCards2.clear();
					clientCurrentCards2.addAll(convertToCard(msg2));

					showClientCard();

					btnPlay.setDisable(true);
					btnDeal.setDisable(false);
					btnSubmit.setDisable(false);

					boolean isCanSplit = checkCanSplit(clientCurrentCards);
					if (isCanSplit) {
						btnSplit.setDisable(false);

						// split (move one card to clientCurrentCard 2)

					} else {
						btnSplit.setDisable(true);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});
		// action for button 'Deal' -> get extends one card
		btnDeal.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					// sent request to server get one card
					sendMessage("deal");
					// receive response from server and display card on view
					String msg = getMessage();
					// cl
					clientCurrentCards.clear();
					if (msg != null && !msg.isEmpty()) {
						clientCurrentCards.addAll(convertToCard(msg));
						showClientCard();
					}
				} catch (Exception e) {

				}

			}
		});
		// action for button 'Deal' -> get extends one card
		btnDeal2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					// sent request to server get one card
					sendMessage("deal2");
					// receive response from server and display card on view
					String msg = getMessage();
					if (msg != null && !msg.isEmpty()) {
						clientCurrentCards2.clear();
						clientCurrentCards2.addAll(convertToCard(msg));
						showClientCard();
					}
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		});

		btnSubmit.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					// sent request to server for submit card
					sendMessage("submit");

					// receive response from server and display card on view
					String msg = getMessage();
					lb_winloss.setText(msg.replace('|', '/'));

					btnPlay.setDisable(false);
					btnDeal.setDisable(true);
					btnDeal2.setDisable(true);

					msg = getMessage();
					serverCurrentCards.clear();
					serverCurrentCards.addAll(convertToCard(msg));
					showServerCard();

					btnSubmit.setDisable(true);

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

			}
		});

		btnSplit.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				btnPlay.setDisable(true);
				btnDeal.setDisable(false);
				btnDeal2.setDisable(false);
			}
		});

	}

	/**
	 * scale image with ratio
	 * 
	 * @param imageView
	 * @param scale
	 */
	public void scaleImage(ImageView imageView, double scale) {

		double height = imageView.getImage().getHeight() * scale;
		double width = imageView.getImage().getWidth() * scale;

		imageView.setFitHeight(height);
		imageView.setFitWidth(width);
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
			Card c2 = currentCards.get(1);
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
	 * create new connection and create object send receive message
	 */
	public void initConnection() {
		Thread thread = new Thread() {

			@Override
			public void run() {
				Socket socket;
				try {
					socket = new Socket(HOST, PORT);

					// object send message
					outputStream = new DataOutputStream(socket.getOutputStream());
					// object receive message
					inputStream = new DataInputStream(socket.getInputStream());
					String msg = getMessage();
					lb_client.setText(msg);
					lb_winloss.setText("0/0");

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		thread.start();

	}

	/**
	 * send messsage to server
	 * 
	 * @param outputStream
	 * @param msg
	 * @throws IOException
	 */
	public void sendMessage(String msg) throws IOException {
		outputStream.writeUTF(msg);
		System.out.println("Client sent to server     : " + msg);
	}

	/**
	 * Receive message from server
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public String getMessage() throws IOException {
		String msg = inputStream.readUTF();
		System.out.println("Client receive from server: " + msg);
		return msg;

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			BorderPane root = new BorderPane();
			initGUI(root);
			initConnection();
			initEvent();
			Scene scene = new Scene(root, 650, 400);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Blackjack Client");
			primaryStage.show();

			// exit program when click close button
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent t) {
					Platform.exit();
					System.exit(0);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		launch(args);
	}
}
