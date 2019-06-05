package es.codeurjc.em.snake;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SnakeGame {

	private long TICK_DELAY = 100;

	//private ConcurrentHashMap<Integer, Snake> snakes = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Snake> snakes = new ConcurrentHashMap<>();
	private AtomicInteger numSnakes = new AtomicInteger();
	private ConcurrentHashMap<String, Integer> scores = new ConcurrentHashMap<>();
	
	private Location food = SnakeUtils.getRandomLocation();
	private AtomicInteger generatedFood = new AtomicInteger(1);
	private final int MAX_FOOD = 40;
	
	
	private ScheduledExecutorService scheduler;
	
	public SnakeGame(Difficulty d) {
		switch (d) {
		case EASY:
			TICK_DELAY = 100;
			break;
		case MEDIUM: 
			TICK_DELAY = 50;
			break;
		case HARD:
			TICK_DELAY = 25;
			break;
		}
	}

	public void addSnake(Snake snake) {

		//snakes.put(snake.getId(), snake);
		snakes.put(snake.getName(), snake);

		int count = numSnakes.getAndIncrement();

		if (count == 0) {
			startTimer();
		}
	}

	public Collection<Snake> getSnakes() {
		return snakes.values();
	}

	public void removeSnake(Snake snake) {

		//snakes.remove(Integer.valueOf(snake.getId()));
		snakes.remove(String.valueOf(snake.getName()));	

		/*
		int count = numSnakes.decrementAndGet();
		
		if (count == 0) {
			stopTimer();
		}
		*/
	}

	private void tick() {

		try {
			for (Snake snake : getSnakes()) {
				snake.update(getSnakes());
			}

			StringBuilder sb = new StringBuilder();
			for (Snake snake : getSnakes()) {
				sb.append(getLocationsJson(snake));
				sb.append(',');
			}
			if(sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			
			//sb.append(String.format("{\"foodx\":%d,\"foody\":%d}", food.x, food.y));
			String msg = String.format("{\"type\": \"update\", \"data\" : [%s], \"foodx\":%d,\"foody\":%d}", sb.toString(), food.x, food.y);
			//String msg = String.format("{\"type\": \"update\", \"data\" : [%s]}", sb.toString());
			//String msg = String.format("{\"type\": \"update\", \"data\" : [%s]}", sb.toString());
			broadcast(msg);
			
			handleFoodCollisions();
			
			if(checkEndGame()) {
				stopTimer();
				broadcast("{\"type\": \"endGame\"}");
			}
			
		} catch (Throwable ex) {
			System.err.println("Exception processing tick()");
			ex.printStackTrace(System.err);
		}
	}

	
	private String getLocationsJson(Snake snake) {

		synchronized (snake) {

			StringBuilder sb = new StringBuilder();
			sb.append(String.format("{\"x\": %d, \"y\": %d}", snake.getHead().x, snake.getHead().y));
			for (Location location : snake.getTail()) {
				sb.append(",");
				sb.append(String.format("{\"x\": %d, \"y\": %d}", location.x, location.y));
			}

			return String.format("{\"name\": \"%s\",\"body\":[%s], \"score\":\"%s\"}", snake.getName(), sb.toString(), snake.getScore());
		}
	}

	public void broadcast(String message) throws Exception {

		for (Snake snake : getSnakes()) {
			try {
				
				//System.out.println("Sending message " + message + " to " + snake.getName());
				snake.sendMessage(message);

			} catch (Throwable ex) {
				System.err.println("Execption sending message to snake " + snake.getName());
				ex.printStackTrace(System.err);
				removeSnake(snake);
			}
		}
	}

	public void startTimer() {
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> tick(), TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

	public void stopTimer() {
		if (scheduler != null) {
			scheduler.shutdown();
		}
	}
	
	public boolean checkEndGame() {
		if (generatedFood.get()>=MAX_FOOD)
			return true;
		
		for (Snake snake : snakes.values()) {
			if (snake.getTail().size()>=20) {
				return true;
			}
		}
		return false;
	}

	public void handleFoodCollisions() throws Exception {
		for (Snake snake : snakes.values()) {

			boolean foodCollision = snake.getHead().equals(food);

			if (foodCollision) {
				snake.reward();
				scores.put(snake.getName(), snake.getScore());
				generateNewFood();
			}
		}
	}
	
	private void generateNewFood() {
		food = SnakeUtils.getRandomLocation();
		if (generatedFood.get()<MAX_FOOD)
			generatedFood.incrementAndGet();
	}
	
	public ConcurrentHashMap<String, Integer> getScores() {
		return scores;
	}
}
