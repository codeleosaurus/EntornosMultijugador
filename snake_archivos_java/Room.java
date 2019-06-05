package es.codeurjc.em.snake;

import java.util.Collection;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

enum Difficulty {
	EASY, MEDIUM, HARD
}

public class Room {
	public static final int MAX_PLAYERS = 4;
	public static final int MIN_PLAYERS = 2;

	private boolean started;
	// nombre del administrador de la sala
	//private final String admin;

	private final String name;
	private ConcurrentHashMap<String, Snake> snakes;
	private Difficulty diff;
	private AtomicInteger n_players;
	private SnakeGame snakeGame;
	private Stack<String> colors;

	public Room(String name, Difficulty diff, String admin) {
		this.name = name;
		this.diff = diff;
		//this.admin = admin;
		this.snakes = new ConcurrentHashMap<>();
		this.n_players = new AtomicInteger(0);
		this.started = false;
		this.snakeGame = new SnakeGame(diff);
		this.colors = new Stack<String>();
		colors.add("#ff5151");
		colors.add("#ffbf51");
		colors.add("#51ffb9");
		colors.add("#7651ff");
	}
	
	public synchronized boolean hasStarted() {
		return started;
	}

	public boolean addSnake(Snake s) {
		if (n_players.get() == MAX_PLAYERS || snakes.containsKey(s.getName()))
			return false;

		snakes.put(s.getName(), s);
		s.changeColor(colors.pop());
		// Si hay una partida en juego, hay que introducir la snake en snakeGame

		if(started) {			
			snakeGame.addSnake(s);	
			joinMessage();
		}
					
		if (n_players.incrementAndGet() == MAX_PLAYERS && !started) {
			startGame();
		}
		return true;
	}
	
	public synchronized boolean isFull() {
		return n_players.get() == MAX_PLAYERS;
	}

	public boolean startGame() {

		if (n_players.get() >= MIN_PLAYERS && !started) {
			started = true;
			
			for (Snake snake : snakes.values()) {				
				snakeGame.addSnake(snake);
			}			
			joinMessage();		
			
			return true;
		}
		else {
			return false;
		}

	}
	
	private void joinMessage() {
		
		StringBuilder sb = new StringBuilder();
		
		for (Snake snake : snakes.values()) {
			
			sb.append(String.format("{\"name\": \"%s\", \"color\": \"%s\"}", snake.getName(), snake.getHexColor()));
			sb.append(',');
		}
		if(sb.length()>0)
			sb.deleteCharAt(sb.length() - 1);
		String msg = String.format("{\"type\": \"join\",\"data\":[%s]}", sb.toString());

		broadcast(msg);
	}

	public int getNumberOfPlayers() {
		return this.n_players.get();
	}

	public Collection<Snake> getSnakes() {
		return snakes.values();
	}

	public void removeSnake(Snake s) {
		// Si hay una partida en juego, hay que eliminar al jugardor de snakeGame
		snakes.remove(String.valueOf(s.getName()));
		snakeGame.removeSnake(s);
		colors.push(s.getHexColor());
		s.resetState();
		// Falta una condicion que diga si hay una partida en juego
		if (n_players.decrementAndGet() < MIN_PLAYERS && started) { // Si nos quedamos sin jugadores en la partida, pero si la
															// partida se esta creando entonces no acabar
			finishGame();
		}
	}

	
	private void finishGame() {
		for(Snake s : snakes.values()) {
			snakeGame.removeSnake(s);
			snakes.remove(s.getName());
		}
		// Hay que echar al otro jugador que quede en la sala
		snakeGame.stopTimer();
	}

	public String getName() {
		return this.name;
	}

	public Difficulty getDifficulty() {
		return this.diff;
	}
	
	public boolean contains(Snake s) {
			return snakes.contains(s);
	}

	public void broadcast(String msg) {
		for (Snake snake : getSnakes()) {
			try {
				// System.out.println("Sending message " + message + " to " + snake.getId());
				//System.out.println("Sending message " + msg + " to " + snake.getName());
				snake.sendMessage(msg);

			} catch (Throwable ex) {
				System.err.println("Execption sending message to snake " + snake.getName());
				ex.printStackTrace(System.err);
				removeSnake(snake);
			}
		}
	}
	
	public ConcurrentHashMap<String, Integer> getScores() {
		return snakeGame.getScores();
	}

}
