package spacewar;

import java.util.Random;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Player extends Spaceship {

	private final WebSocketSession session;
	private final int playerId;
	private final String shipType;
	private int lives;
	private int points;
	//pasar la vida a SpaceWar.java
	
	public Player(int playerId, WebSocketSession session, int lives, int points) {
		this.playerId = playerId;
		this.session = session;
		this.shipType = this.getRandomShipType();
		this.lives = lives;
		this.points= points;
	}
	
	public int getPlayerId() {
		return this.playerId;
	}

	public WebSocketSession getSession() {
		return this.session;
	}

	public void sendMessage(String msg) throws Exception {
		synchronized(this.session) {
		this.session.sendMessage(new TextMessage(msg));
		}
	}

	public String getShipType() {
		return shipType;
	}
	public int getLives() {
		return lives;
	}
	 public void setLives(int liv) {
	        this.lives = liv;
	    }
	 public int getPoints() {
			return points;
		}
	 public void setPoints(int po) {
	        this.points = po;
	    }
	private String getRandomShipType() {
		String[] randomShips = { "blue", "darkgrey", "green", "metalic", "orange", "purple", "red" };
		String ship = (randomShips[new Random().nextInt(randomShips.length)]);
		ship += "_0" + (new Random().nextInt(5) + 1) + ".png";
		return ship;
	}
}
