package es.codeurjc.em.snake;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Snake {

	private static final int DEFAULT_LENGTH = 5;

	//private final int id;

	private Location head;
	private final Deque<Location> tail = new ArrayDeque<>();
	private int length = DEFAULT_LENGTH;

	private String hexColor;
	private Direction direction;

	private final WebSocketSession session;
	
	private final String name;

	private int score = 0;
	
	/*
	public Snake(int id, WebSocketSession session) {
		this.id = id;
		this.session = session;
		this.hexColor = SnakeUtils.getRandomHexColor();
		resetState();
	}
	*/
	
	public Snake(String name, WebSocketSession session) {
		this.name = name;
		this.session = session;
		//this.hexColor = SnakeUtils.getRandomHexColor();
		this.hexColor = "";
		resetState();
	}
	
	public void changeColor(String color) {
		this.hexColor = color;
	}

	public void resetState() {
		this.direction = Direction.NONE;
		this.head = SnakeUtils.getRandomLocation();
		this.tail.clear();
		this.length = DEFAULT_LENGTH;
		this.score = 0;
	}

	private synchronized void kill() throws Exception {
		resetState();
		//sendMessage("{\"type\": \"dead\"}");
	}

	public synchronized void reward() throws Exception {
		this.length++;
		this.score+=10;
		//sendMessage("{\"type\": \"kill\"}");
	}
	
	protected void sendMessage(String msg) throws Exception {
		synchronized(this.session) {
			this.session.sendMessage(new TextMessage(msg));
		}		
	}

	public synchronized void update(Collection<Snake> snakes) throws Exception {

		Location nextLocation = this.head.getAdjacentLocation(this.direction);

		if (nextLocation.x >= Location.PLAYFIELD_WIDTH) {
			nextLocation.x = 0;
		}
		if (nextLocation.y >= Location.PLAYFIELD_HEIGHT) {
			nextLocation.y = 0;
		}
		if (nextLocation.x < 0) {
			nextLocation.x = Location.PLAYFIELD_WIDTH;
		}
		if (nextLocation.y < 0) {
			nextLocation.y = Location.PLAYFIELD_HEIGHT;
		}

		if (this.direction != Direction.NONE) {
			this.tail.addFirst(this.head);
			if (this.tail.size() > this.length) {
				this.tail.removeLast();
			}
			this.head = nextLocation;
		}

		handleCollisions(snakes);
	}

	/*
	private void handleCollisions(Collection<Snake> snakes) throws Exception {

		for (Snake snake : snakes) {

			boolean headCollision = this.id != snake.id && snake.getHead().equals(this.head);

			boolean tailCollision = snake.getTail().contains(this.head);

			if (headCollision || tailCollision) {
				kill();
				if (this.id != snake.id) {
					snake.reward();
				}
			}
		}
	}
	*/
	
	private void handleCollisions(Collection<Snake> snakes) throws Exception {

		for (Snake snake : snakes) {

			boolean headCollision = !this.name.equals(snake.name) && snake.getHead().equals(this.head);

			boolean tailCollision = snake.getTail().contains(this.head);

			if (headCollision || tailCollision) {
				kill();
				if (!this.name.equals(snake.name)) {
					snake.length++;
				}
			}
		}
	}

	public synchronized Location getHead() {
		return this.head;
	}

	public synchronized Collection<Location> getTail() {
		return this.tail;
	}

	public synchronized void setDirection(Direction direction) {
		this.direction = direction;
	}

	/*
	public int getId() {
		return this.id;
	}
	*/
	public synchronized void addScore(int newScore) {
		this.score+=newScore;
	}
	public String getName() {
		return this.name;
	}

	public String getHexColor() {
		return this.hexColor;
	}
	
	public int getScore() {
		return this.score;
	}
}
