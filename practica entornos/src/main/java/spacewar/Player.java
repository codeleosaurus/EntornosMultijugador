package spacewar;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Player extends Spaceship {
	
	////////////////////////////
	//DECLARACIÓN DE ATRIBUTOS//
	////////////////////////////

	private final int DEFAULT_LIVES = 3;
	private final WebSocketSession session;
	private Room room;
	
	//ATRIBUTOS DEL JUGADOR
	private AtomicInteger playerId;
	private String name;
	
	//ATRIBUTOS DE LA NAVE
	private final String shipType;
	private AtomicInteger hp;
	private AtomicInteger points;
	private AtomicBoolean alive;
	
	///////////////
	//CONSTRUCTOR//
	///////////////
	
	public Player(String name, WebSocketSession session) {
		
		this.session = session;
		
		this.name = name;
		this.playerId = new AtomicInteger();
		
		this.shipType = this.getRandomShipType();
		this.hp = new AtomicInteger(DEFAULT_LIVES);
		this.points = new AtomicInteger(0);
		this.alive = new AtomicBoolean(true);
	}
	
	public void resetStats() {
		this.hp = new AtomicInteger(DEFAULT_LIVES);
		this.alive = new AtomicBoolean(true);
	}
	
	//GET Y SET DEL ID DEL JUGADOR

	public int getPlayerId() {
		return this.playerId.get();
	}
	
	public void setPlayerId(int id) {
		this.playerId.set(id);
	}

	public WebSocketSession getSession() {
		return this.session;
	}

	public void sendMessage(String msg) throws Exception {
		synchronized(this.session) {
		this.session.sendMessage(new TextMessage(msg));
		}
	}
	
	private String getRandomShipType() {
		String[] randomShips = { "blue", "darkgrey", "green", "metalic", "orange", "purple", "red" };
		String ship = (randomShips[new Random().nextInt(randomShips.length)]);
		ship += "_0" + (new Random().nextInt(5) + 1) + ".png";
		return ship;
	}

	public String getShipType() {
		return shipType;
	}
	
	public int getLives() {
		return hp.get();
	}
	
	 public void setLives(int liv) {
	    this.hp.set(liv);
	 }
	 
	 public int getPoints() {
		return points.get();
	 }
	 
	 public void setPoints(int po) {
	    this.points.set(po);
	 }
	 
	 public void addPoints(int po) {
		int newPoints = getPoints() + po;
		this.points.set(newPoints);
	 }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
