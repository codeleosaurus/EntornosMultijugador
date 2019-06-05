package es.codeurjc.em.snake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SnakeHandler extends TextWebSocketHandler {

	private static final String SNAKE_ATT = "snake";
	private static final String ROOM_ATT = "room";
	private static final String ROOM_CREATOR = "roomCreator";

	// private AtomicInteger snakeIds = new AtomicInteger(0);

	private ConcurrentHashMap<String, WebSocketSession> openSessions = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
	private List<Score> globalScoresList = new ArrayList<Score>();
	private Set<Score> scoresInList = new HashSet<Score>();
	private ReentrantLock mapAccess = new ReentrantLock();
	private ReentrantLock roomsAccess = new ReentrantLock();

	// private SnakeGame snakeGame = new SnakeGame();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		//updateChatConnection(session);

		/*
		 * if (rooms.mappingCount() > 0) { StringBuilder sb = new StringBuilder(); for
		 * (Room r : rooms.values()) { sb.append(String.
		 * format("{\"name\": \"%s\", 				\"diff\": \"%s\", \"players\": %d}",
		 * r.getName(), r.getDifficulty(), r.getNumberOfPlayers())); sb.append(','); }
		 * sb.deleteCharAt(sb.length() - 1); String msg =
		 * String.format("{\"type\": \"allRooms\", \"data\":[%s]}", sb.toString());
		 * session.sendMessage(new TextMessage(msg)); }
		 */

		// updateRooms(session);

		/*
		 * int id = snakeIds.getAndIncrement();
		 * 
		 * Snake s = new Snake(id, session);
		 * 
		 * session.getAttributes().put(SNAKE_ATT, s);
		 * 
		 * snakeGame.addSnake(s);
		 * 
		 * StringBuilder sb = new StringBuilder(); for (Snake snake :
		 * snakeGame.getSnakes()) {
		 * sb.append(String.format("{\"id\": %d, \"color\": \"%s\"}", snake.getId(),
		 * snake.getHexColor())); sb.append(','); } sb.deleteCharAt(sb.length()-1);
		 * String msg = String.format("{\"type\": \"join\",\"data\":[%s]}",
		 * sb.toString());
		 * 
		 * snakeGame.broadcast(msg);
		 */
	}

	/*
	private void updateChatDisconnection() {
		StringBuilder sb = new StringBuilder();
		synchronized (openSessions) {
			for (WebSocketSession s : openSessions.values()) {
				// if (session.equals(s)) {
				System.out.print("disconnectionFOR: " + ((Snake) s.getAttributes().get(SNAKE_ATT)).getName());
				String r = (String) s.getAttributes().get(ROOM_ATT);
				if (r.equals("")) {
					sb.append(String.format("{\"name\":\"%s\", \"status\":false}",
							((Snake) s.getAttributes().get(SNAKE_ATT)).getName()));
					sb.append(',');
				} else {
					sb.append(String.format("{\"name\":\"%s\", \"status\":true}",
							((Snake) s.getAttributes().get(SNAKE_ATT)).getName()));
					sb.append(',');
				}
				// }
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
			String msg = String.format("{\"type\": \"connectedPlayers\", \"data\":[%s]}", sb.toString());
			// session.sendMessage(new TextMessage(msg));
			sendMsg(msg);
		}
	}
	*/
	
	private void updateChatConnection() throws Exception {

		StringBuilder sb = new StringBuilder();
		synchronized (openSessions) {
			for (WebSocketSession s : openSessions.values()) {
				String r = (String) s.getAttributes().get(ROOM_ATT);
				if (r.equals("")) {
					sb.append(String.format("{\"name\":\"%s\", \"status\":false}",
							((Snake) s.getAttributes().get(SNAKE_ATT)).getName()));
					sb.append(',');
				} else {
					sb.append(String.format("{\"name\":\"%s\", \"status\":true}",
							((Snake) s.getAttributes().get(SNAKE_ATT)).getName()));
					sb.append(',');
				}
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
			String msg = String.format("{\"type\": \"connectedPlayers\", \"data\":[%s]}", sb.toString());
			// session.sendMessage(new TextMessage(msg));
			sendMsg(msg);
		}

	}

	private void updateRooms(WebSocketSession session) throws Exception {
		if (rooms.mappingCount() > 0) {
			StringBuilder sb = new StringBuilder();
			for (Room r : rooms.values()) {
				sb.append(String.format("{\"name\": \"%s\",\"diff\": \"%s\", \"players\": %d}", r.getName(),
						r.getDifficulty(), r.getNumberOfPlayers()));
				sb.append(',');
			}
			if (sb.length() > 0)
				sb.deleteCharAt(sb.length() - 1);
			String msg = String.format("{\"type\": \"allRooms\", \"data\":[%s]}", sb.toString());
			
			//session.sendMessage(new TextMessage(msg));
			
			
			sendMsg(msg);
		}
	}

	private void createRoom(WebSocketSession session, String roomName, String diff) throws Exception {
		roomsAccess.lock();

		// Si el jugador no es administrador de ninguna sala ni está jugando en ninguna
		// sala
		// podemos crear la sala
		boolean isAdmin = (boolean) session.getAttributes().get(ROOM_CREATOR);
		String sala = (String) session.getAttributes().get(ROOM_ATT);

		if (!rooms.containsKey(roomName) && !isAdmin && sala.equals("")) {

			Snake s = (Snake) session.getAttributes().get(SNAKE_ATT);
			Room r = new Room(roomName, Difficulty.valueOf(diff), s.getName());
			r.addSnake(s);
			rooms.put(roomName, r);

			roomsAccess.unlock();

			synchronized (session) {
				session.getAttributes().put(ROOM_ATT, roomName);
				session.getAttributes().put(ROOM_CREATOR, true);
			}

			session.sendMessage(new TextMessage("{\"type\": \"createRoom\", \"created\": true}"));
			sendMsg("{\"type\": \"updateRoom\", \"name\": \"" + roomName + "\", \"diff\": \"" + diff
					+ "\", \"players\": " + r.getNumberOfPlayers() + "}");
			updateChatConnection();
		} else {
			roomsAccess.unlock();
			session.sendMessage(new TextMessage("{\"type\": \"createRoom\", \"created\": false}"));
			// Mandar mensaje de que el nombre no es valido
		}
	}

	private void startMatch(WebSocketSession session, String roomName) throws Exception {
		Room r = rooms.get(roomName);
		if (r.startGame()) {
			updateChatConnection();
		}
	}

	private void joinRoom(WebSocketSession session, String roomName) throws Exception {
		Snake s = (Snake) session.getAttributes().get(SNAKE_ATT);
		Room currentRoom = rooms.get(roomName);

		if (currentRoom != null && currentRoom.addSnake(s)) {

			session.sendMessage(new TextMessage("{\"type\": \"room\", \"joined\": true}"));
			sendMsg("{\"type\": \"updateRoom\", \"name\": \"" + currentRoom.getName() + "\", \"diff\": \""
					+ currentRoom.getDifficulty().toString() + "\", \"players\": \"" + currentRoom.getNumberOfPlayers()
					+ "\"}");

			synchronized (session) {
				session.getAttributes().put(ROOM_ATT, roomName);
				session.getAttributes().put(ROOM_CREATOR, false);
			}

		} else {
			session.sendMessage(new TextMessage("{\"type\": \"room\", \"joined\": false}"));
		}

		updateRooms(session);
		updateChatConnection();
	}

	private void initSession(WebSocketSession session, String name) throws Exception {

		mapAccess.lock();
		if (!openSessions.containsKey(name)) {
			Snake s = new Snake(name, session);

			synchronized (session) {
				session.getAttributes().put(SNAKE_ATT, s);
				session.getAttributes().put(ROOM_ATT, "");
				session.getAttributes().put(ROOM_CREATOR, false);
			}
			openSessions.put(name, session);
			updateRooms(session);
			mapAccess.unlock();

			session.sendMessage(new TextMessage("{\"type\": \"name\", \"data\": true}"));
			sendMsg("{\"type\":\"connect\", \"name\":\"" + name + "\", \"status\":false}");
		} else {
			mapAccess.unlock();
			// Mandar mensaje de que el nombre no es valido
			synchronized(session) {
				session.sendMessage(new TextMessage("{\"type\": \"name\", \"data\": false}"));
			}
			
		}

		updateChatConnection();
		sendRankingList(session);

	}

	private void sendMsg(String msg) {
		for (WebSocketSession s : openSessions.values()) {
			try {
				synchronized(s) {
					s.sendMessage(new TextMessage(msg));
				}				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendChatMsg(String msg, String sender) {
		for (WebSocketSession s : openSessions.values()) {
			try {
				synchronized(s) {
					s.sendMessage(new TextMessage(
							"{\"type\": \"chat\", \"msg\": \"" + msg + "\", \"author\": \"" + sender + "\"}"));
				}				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

		try {
			String payload = message.getPayload();
			//System.out.println("Message received: " + payload);

			if (payload.equals("ping")) {
				return;
			}

			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(payload);

			String functionName = node.get("type").asText();

			switch (functionName) {
			case "direction":
				Snake s = (Snake) session.getAttributes().get(SNAKE_ATT);
				Direction d = Direction.valueOf(node.get("params").asText().toUpperCase());
				s.setDirection(d);
				break;
			case "name":
				initSession(session, node.get("params").asText());
				break;
			case "chat":
				sendChatMsg(node.get("params").asText(), node.get("author").asText());
				break;
			case "createRoom":
				createRoom(session, node.get("roomName").asText(), node.get("diff").asText().toUpperCase());
				break;
			case "joinRoom":
				joinRoom(session, node.get("params").asText());
				break;
			case "joinAny":
				joinRoom(session, joinAny(session));
				break;
			case "start":
				startMatch(session, node.get("params").asText());
				break;
			case "exit":
				exitRoom(session, false);
				break;
			}

		} catch (Exception e) {
			System.err.println("Exception processing message " + message.getPayload());
			e.printStackTrace(System.err);
		}

		/*
		 * try {
		 * 
		 * String payload = message.getPayload();
		 * 
		 * if (payload.equals("ping")) { return; }
		 * 
		 * Snake s = (Snake) session.getAttributes().get(SNAKE_ATT);
		 * 
		 * Direction d = Direction.valueOf(payload.toUpperCase()); s.setDirection(d);
		 * 
		 * } catch (Exception e) { System.err.println("Exception processing message " +
		 * message.getPayload()); e.printStackTrace(System.err); }
		 */
	}
	
	public String joinAny(WebSocketSession s)  {
		roomsAccess.lock();
		for(Room r : rooms.values()) {
			if(!r.isFull()) {
				try {
					synchronized(s) {
						s.sendMessage(new TextMessage("{\"type\":\"roomParams\", \"name\":\""+ r.getName()+ "\", \"diff\": \""+ r.getDifficulty().toString()
								+ "\",\"players\": \"" + r.getNumberOfPlayers() + "\" }"));
					}					
				}
				catch(IOException e) {
					roomsAccess.unlock();
					System.out.println(e);
					e.printStackTrace();
				}
				roomsAccess.unlock();
				return r.getName();
			}
		}
		roomsAccess.unlock();
		return "";		
	}

	public void exitRoom(WebSocketSession session, boolean disconnected) throws Exception {

		// borramos la serpiente de la partida
		Snake sn = (Snake) session.getAttributes().get(SNAKE_ATT);
		Room currentRoom = rooms.get(session.getAttributes().get(ROOM_ATT));

		if (currentRoom != null && sn != null) {

			currentRoom.removeSnake(sn);

			// borramos rooms si no quedan jugadores
			if ((currentRoom.getNumberOfPlayers() < Room.MIN_PLAYERS && currentRoom.hasStarted()) || currentRoom.getNumberOfPlayers() == 0) {
				// Le comunicamos a todos que abandonamos la partida
				updateRanking(currentRoom);
				rooms.remove(currentRoom.getName());
				System.out.println("sala: " + currentRoom.getName() + " borrada");
				if (currentRoom != null) {
					sendMsg("{\"type\": \"removeRoom\", \"name\": \"" + currentRoom.getName() + "\"}");
					currentRoom.broadcast("{\"type\": \"leave\", \"id\": \"" + sn.getName() + "\"}");
				}
			} else {
				// Si no se borra la sala se actualiza la tabla de Rooms
				if (!disconnected) {
					session.sendMessage(new TextMessage("{\"type\": \"room\", \"joined\": false}"));
				}

				sendMsg("{\"type\": \"updateRoom\", \"name\": \"" + currentRoom.getName() + "\", \"diff\": \""
						+ currentRoom.getDifficulty().toString() + "\", \"players\": \""
						+ currentRoom.getNumberOfPlayers() + "\"}");
				// Le decimos a todas los jugadores de la sala que borren al jugador que se ha
				// ido para que no lo pinten
				currentRoom.broadcast("{\"type\": \"updateSnakes\", \"id\": \"" + sn.getName() + "\"}");
			}
		}

		// los atributos room_att que especifica en que sala esta jugando se inicializa
		// y room_creator tb
		if (!disconnected) {
			synchronized (session) {
				session.getAttributes().put(ROOM_ATT, "");
				session.getAttributes().put(ROOM_CREATOR, false);
			}

			// Le decimos que hemos abandonado la sala y que borre todos los jugadores que
			// tenemos
			// Lo hacemos dos veces porque como primero hemos eliminado la snake de la Room
			// en que estaba pues
			// hay que decirle explicitamente que hemos abandonado
			synchronized(session) {
				session.sendMessage(new TextMessage("{\"type\": \"leave\", \"id\": \"" + sn.getName() + "\"}"));
			}
			

			// actualizamos las salas y notificamos a todos las conexiones
			updateRooms(session);
			updateChatConnection();

			System.out.println("snake: " + sn.getName() + " borrada");
		}

	}

	private void updateRanking(Room r) {
		synchronized (globalScoresList) {
			for (Map.Entry<String, Integer> m : r.getScores().entrySet()) {
				Score s = new Score(m.getKey(), m.getValue());
				if(scoresInList.contains(s)) {
					int i = 0;
					while(i < globalScoresList.size() && !s.equals(globalScoresList.get(i))) {
						i++;
					}
					if(s.getScore() > globalScoresList.get(i).getScore()) {
						globalScoresList.remove(i);
						i = 0;
						while (i < globalScoresList.size() && s.getScore() < globalScoresList.get(i).getScore()) {
							i++;
						}
						
						globalScoresList.add(i, s);
					}
				}
				else {
					int i = 0;
					while (i < globalScoresList.size() && s.getScore() < globalScoresList.get(i).getScore()) {
						i++;
					}
					if(i < 10) {
						scoresInList.add(s);
					}
					
					globalScoresList.add(i, s);
				}			
				
				
				if (globalScoresList.size() > 10) {
					globalScoresList.remove(10);
				}
			}
		}
		sendRankingList(null);
		
	}
	
	private void sendRankingList(WebSocketSession session) {
		StringBuilder sb = new StringBuilder();
		for (Score s : globalScoresList) {
			sb.append(String.format("{\"name\": \"%s\", \"score\": %d}", s.getName(), s.getScore()));
			sb.append(",");
		}
		if (sb.length()>0) {
			sb.deleteCharAt(sb.length()-1);
		}
		String msg = String.format("{\"type\": \"rankinglist\", \"data\": [%s]}", sb.toString());
		if(session != null) {
			try {
				synchronized(session) {
					session.sendMessage(new TextMessage(msg));
				}				
			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		else {
			sendMsg(msg);
		}
		
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

		System.out.println("Connection closed. Session " + session.getId());

		Snake s = (Snake) session.getAttributes().get(SNAKE_ATT);
		if(s != null) {
			openSessions.remove(s.getName());
			exitRoom(session, true);
			sendMsg("{\"type\": \"disconnectedPlayer\", \"name\": \"" + s.getName() + "\"}");
			updateChatConnection();
			updateRooms(session);
		}
		
		/*
		 * // Hay que tener en cuenta que pasa si: el jugador se desconecta y no estaba
		 * en // ninguna room o si si que lo estaba // snakeGame.removeSnake(s); String
		 * temp = (String) session.getAttributes().get(ROOM_ATT);
		 * 
		 * if (!temp.equals("")) { Room r = rooms.get(temp); r.removeSnake(s); if
		 * (r.getNumberOfPlayers() < Room.MIN_PLAYERS) { rooms.remove(temp);
		 * System.out.println("sala: " + temp + " borrada"); } }
		 */
		// exitRoom(session);

		// String msg = String.format("{\"type\": \"leave\", \"id\": %d}", s.getId());
		// String msg = String.format("{\"type\": \"leave\", \"name\": \"%s\"}",
		// s.getName());

		// snakeGame.broadcast(msg);
	}

}
