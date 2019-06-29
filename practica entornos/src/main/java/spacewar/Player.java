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
	
	////////////////////
	//RESETEAR JUGADOR//
	////////////////////
	
	public void resetStats() {
		this.hp = new AtomicInteger(DEFAULT_LIVES);
		this.alive = new AtomicBoolean(true);
	}
	
	////////////////////////
	//GENERAR TIPO DE NAVE//
	////////////////////////
	
	private String getRandomShipType() {
		String[] randomShips = { "blue", "darkgrey", "green", "metalic", "orange", "purple", "red" };
		String ship = (randomShips[new Random().nextInt(randomShips.length)]);
		ship += "_0" + (new Random().nextInt(5) + 1) + ".png";
		return ship;
	}
	
	/////////////////////
	//ENVÍO DE MENSAJES//
	/////////////////////
	
	public void sendMessage(String msg) throws Exception {
		synchronized(this.session) {
		this.session.sendMessage(new TextMessage(msg));
		}
	}

	//////////////////////////
	//GET Y SET DE ATRIBUTOS//
	//////////////////////////
	
	//SESSION
	
	public WebSocketSession getSession() {
		return this.session;
	}
	
	//ID
	
	public int getPlayerId() {
		return this.playerId.get();
	}
	
	public void setPlayerId(int id) {
		this.playerId.set(id);
	}

	//NAME

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	//HP
	
	public int getLives() {
		return hp.get();
	}
	
	public int decreaseHP() {
		return this.hp.decrementAndGet();
	 }

	//POINTS
	
	public int getPoints() {
		return points.get();
	}
	 
	public void increasePoints(int points) {
		this.points.addAndGet(points);
	}
	
	//SHIP TYPE
	
	public String getShipType() {
		return shipType;
	}
	
	//ALIVE STATUS
	
	public boolean isAlive() {
		return this.alive.get();
	}
	
	public void setAlive(boolean state) {
		this.alive.set(state);
	}
}
