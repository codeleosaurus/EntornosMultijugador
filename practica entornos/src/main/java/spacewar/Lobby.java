package spacewar;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Lobby {
	
	//////////////////////////////////////
	//DECLARACIÓN DE ATRIBUTOS DEL LOBBY//
	//////////////////////////////////////
	
	private static final String PLAYER_ATTRIBUTE = "PLAYER";
	private static final String ROOM_ATTRIBUTE = "ROOM";
	private ObjectMapper mapper = new ObjectMapper();
	
	//SALAS
	private ConcurrentHashMap<String, Room> rooms;
	
	//JUGADORES
	private ConcurrentHashMap<String, Player> playersInLobby;
	private ConcurrentHashMap<String, Player> playersInGame;
	
	//MATCHMAKING
	private BlockingQueue<Player> easyDuelMatchmaking;
	private BlockingQueue<Player> mediumDuelMatchmaking;
	private BlockingQueue<Player> hardDuelMatchmaking;
	private BlockingQueue<Player> easyBRMatchmaking;
	private BlockingQueue<Player> mediumBRMatchmaking;
	private BlockingQueue<Player> hardBRMatchmaking;
	
	//WAITLIST
	private ScheduledExecutorService waitlistScheduler;
	public BlockingQueue<Player> lastInWaitlists;
	
	
	///////////////
	//CONSTRUCTOR//
	///////////////
	
	public Lobby() {
		
		rooms = new ConcurrentHashMap<>();
		
		playersInLobby = new ConcurrentHashMap<>();
		playersInGame = new ConcurrentHashMap<>();
		
		easyDuelMatchmaking = new LinkedBlockingQueue<>();
		mediumDuelMatchmaking = new LinkedBlockingQueue<>();
		hardDuelMatchmaking = new LinkedBlockingQueue<>();
		easyBRMatchmaking = new LinkedBlockingQueue<>();
		mediumBRMatchmaking = new LinkedBlockingQueue<>();
		hardBRMatchmaking = new LinkedBlockingQueue<>();
		
		waitlistScheduler = Executors.newScheduledThreadPool(1);
		lastInWaitlists = new LinkedBlockingQueue<>();
		
		
	}
	
	//toi probando gishun ^^
	//toi haciendo otra prueba en gishun esta es más compleja
	//pruebaaaaaaaas
	
	//////////////////////////////
	//MÉTODOS DE ACCESO AL LOBBY//
	//////////////////////////////
	
	//MÉTODO QUE AÑADE A UN JUGADOR AL LOBBY Y ACTUALIZA SU INFORMACIÓN (TRAS COMPROBAR QUE NO ESTUVIESE YA DENTRO)
	
	public void joinLobby(Player player) {
		
		if(!playersInLobby.contains(player)) {
			playersInLobby.put(player.getName(), player);
			System.out.println("[LOBBY] [PLAYER INFO] Player " + player.getName() + " joined the lobby");
			sendRoomListToPlayer(player);	
		}else {
			System.out.println("[LOBBY] [PLAYER ERROR] Unable to connect player " + player.getName() + " to lobby. Player already exists in lobby");
		}
	}
	
	//MÉTODO QUE RETIRA A UN JUGADOR DEL LOBBY (TRAS COMPROBAR QUE ESTÁ DENTRO)
	
	public void leaveLobby(Player player) {
		
		if(playersInLobby.contains(player)) {
			playersInLobby.remove(player.getName());
			System.out.println("[LOBBY] [PLAYER INFO] Player " + player.getName() + " removed from the lobby");
		}else {
			System.out.println("[LOBBY] [PLAYER ERROR] Unable to disconnect player " + player.getName() + " from the lobby. Player doesn't exist");
		}
	}
	
	//////////////////////////////////////
	//MÉTODOS DE GESTIÓN DEL MATCHMAKING//
	//////////////////////////////////////

	//MÉTODO QUE INCLUYE A UN JUGADOR EN EL SISTEMA DE MATCHMAKING
	//PRIMERO SE COMPRUEBAN LAS SALAS QUE EXISTEN, COMPARANDO SU MODO DE JUEGO Y DIFICULTAD CON LA QUE BUSCA EL JUGADOR
	//SI HAY ALGUNA SALA QUE CUMPLE LOS REQUISITOS Y HAY HUECO LIBRE, EL JUGADOR INTENTARÁ UNIRSE A ELLA
	//SI EL SISTEMA NO ENCUENTRA NINGUNA SALA DISPONIBLE, METERÁ AL JUGADOR EN LA COLA CORRESPONDIENTE DEPENDIENDO DE SU BÚSQUEDA
	
	public void joinMatchmaking(Player player, String desiredDiff, String desiredMode) {
		
		for (Room room : rooms.values()) {
			
			String ROOM_DIFFICULTY = room.getDifficulty();
			String ROOM_GAMEMODE = room.getGamemode();
			
			if(!room.hasFinished() && !room.isFull() && ROOM_DIFFICULTY == desiredDiff && ROOM_GAMEMODE == desiredMode) {
				
				try {
					
					System.out.println("[LOBBY] [MATCHMAKING INFO] Available room found for player " + player.getName() + ". Trying to join now");
					joinRoom(player, room.getName());
					return;
					
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("[LOBBY] [MATCHMAKING INFO] Couldn't find any available room for player " + player.getName() + ". Sending player to matchmaking queues");
		
		if(desiredMode == "BATTLE ROYALE") {
			switch(desiredDiff) {
			
				case "EASY":
					easyBRMatchmaking.add(player);
					break;
					
				case "MEDIUM":
					mediumBRMatchmaking.add(player);	
					break;
					
				case "HARD":
					hardBRMatchmaking.add(player);
					break;
					
				default:
					System.out.println("[LOBBY] [MATCHMAKING ERROR] Invalid difficulty selected. Setting difficulty to Easy by default");
					easyBRMatchmaking.add(player);
					break;
			}
		}else {
			
			if(desiredMode != "DUEL") {
				System.out.println("[LOBBY] [MATCHMAKING ERROR] Invalid gamemode selected. Setting gamemode to Duel by default");
			}
			
			switch(desiredDiff) {
			
			case "EASY":
				easyDuelMatchmaking.add(player);
				break;
				
			case "MEDIUM":
				mediumDuelMatchmaking.add(player);	
				break;
				
			case "HARD":
				hardDuelMatchmaking.add(player);
				break;
				
			default:
				System.out.println("[LOBBY] [MATCHMAKING ERROR] Invalid difficulty selected. Setting difficulty to Easy by default");
				easyDuelMatchmaking.add(player);
				break;
			}
		}
		
		ObjectNode msg = mapper.createObjectNode();
		
		msg.put("event",  "CONFIRMATION");
		msg.put("type", "JOINING MATCHMAKING");
		
		try {
			player.sendMessage(msg.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
		
	//MÉTODO QUE SACA A UN JUGADOR DE LA COLA DE MATCHMAKING EN LA QUE ESTUVIESE
	//SI ESTABA EN ALGUNA COLA Y HA SALIDO DE ELLA, SE ENVÍA UN MENSAJE DE CONFIRMACIÓN
	//EN CASO CONTRARIO SE INFORMA DEL ERROR
	
	public void leaveMatchmaking(Player player) throws Exception {
		
		boolean playerLeft = false;
		
		if       (easyDuelMatchmaking.contains(player))   { playerLeft = easyDuelMatchmaking.remove(player);
		}else if (mediumDuelMatchmaking.contains(player)) { playerLeft = mediumDuelMatchmaking.remove(player);
		}else if (hardDuelMatchmaking.contains(player))   { playerLeft = hardDuelMatchmaking.remove(player);
		}else if (easyBRMatchmaking.contains(player))     { playerLeft = easyBRMatchmaking.remove(player);
		}else if (mediumBRMatchmaking.contains(player))   { playerLeft = mediumBRMatchmaking.remove(player);
		}else if (hardBRMatchmaking.contains(player))     { playerLeft = hardBRMatchmaking.remove(player);}
		
		if(playerLeft) {
			
			System.out.println("[LOBBY] [MATCHMAKING INFO] Player " + player.getName() + " left matchmaking queues");
			
			ObjectNode msg = mapper.createObjectNode();
			msg.put("event", "CONFIRMATION");
			msg.put("type", "LEAVING MATCHMAKING");
			player.sendMessage(msg.toString());
		
		}else {
			
			System.out.println("[LOBBY] [MATCHMAKING ERROR] Error leaving matchmaking. Player " + player.getName() + " wasn't in any matchmaking queue");
		}
		
	}
	

	//MÉTODO QUE COMPRUEBA EL ESTADO DEL MATCHMAKING PARA UNA SALA CONCRETA
	//REVISA LA COLA DE MATCHMAKING CORRESPONDIENTE A LA DIFICULTAD Y MODO DE JUEGO DE LA SALA
	//EN CASO DE QUE HAYA ALGÚN JUGADOR ESPERANDO EN ESA COLA, INTENTA AÑADIRLO A LA SALA.
	//EN CASO CONTRARIO, INFORMA DE ELLO (Y EL JUGADOR SALE DEL SISTEMA DE MATCHMAKING, DE TODAS FORMAS
	//ESTE MÉTODO SE INVOCA CUANDO HAY ALGÚN HUECO NUEVO EN LA SALA, ASÍ QUE NO DEBERÍA OCURRIR)
	
	private boolean checkMatchmaking(Room room) {
		
		if(!room.hasFinished()) {
			
			String ROOM_GAMEMODE = room.getGamemode();
			String ROOM_DIFFICULTY = room.getDifficulty();
			Player joiningPlayer = null;
			
			if(ROOM_GAMEMODE == "DUEL") {
				
				switch(ROOM_DIFFICULTY) {
					case "EASY":
						joiningPlayer = easyDuelMatchmaking.poll();
						break;
					case "MEDIUM":
						joiningPlayer = mediumDuelMatchmaking.poll();
						break;
					case "HARD":
						joiningPlayer = hardDuelMatchmaking.poll();
						break;
					default:
						break;
				}
			}else {
				
				switch(ROOM_DIFFICULTY) {
				case "EASY":
					joiningPlayer = easyBRMatchmaking.poll();
					break;
				case "MEDIUM":
					joiningPlayer = mediumBRMatchmaking.poll();
					break;
				case "HARD":
					joiningPlayer = hardBRMatchmaking.poll();
					break;
				default:
					break;
				}
			}
			
			if(joiningPlayer != null) {
				
				System.out.println("[LOBBY] [MATCHMAKING INFO] Player " + joiningPlayer.getName() + " trying to join Room " + room.getName() + " from matchmaking queues");
				
				try {
					joinRoom(joiningPlayer, room.getName());
				}catch(Exception e) {
					e.printStackTrace();
				}
				
				return true;
				
			}else {
				
				System.out.println("[LOBBY] [MATCHMAKING INFO] No players found in matchmaking for Room " + room.getName() + " gamemode and difficulty");
				
			}
			
		}
		
		return false;
	}
	
	/////////////////////////////////////
	//MÉTODOS DE GESTIÓN DE LA WAITLIST//
	/////////////////////////////////////

	//falta por hacer
	private void removeFromWaitlist() {
		// TODO Auto-generated method stub
	}
	
	//MÉTODO QUE SACA DE LA LISTA DE ESPERA A UN JUGADOR EN LA SALA EN LA QUE ESTUVIESE ESPERANDO PARA ENTRAR
	//CAMBIA SU ATRIBUTO DE SALA Y ACTUALIZA SU INFORMACIÓN SOBRE LAS SALAS TRAS DEVOLVERLE AL LOBBY
	//EN CASO DE QUE NO PUEDA SACARLE DE UNA LISTA DE ESPERA ACTUALIZA SU INFORMACIÓN POR SI ACASO
	public void leaveWaitlist(Player player) throws Exception {
		
		ObjectNode msg = mapper.createObjectNode();
		
		if(lastInWaitlists.remove(player)) {
			
			Room room;
			
			synchronized(player.getSession()) {
				room = (Room) player.getSession().getAttributes().get(ROOM_ATTRIBUTE);
			}
			
			if(room.waitlist.remove(player)) {
				
				synchronized(player.getSession()) {
					player.getSession().getAttributes().remove(ROOM_ATTRIBUTE);
				}
				
				joinLobby(player);
				
				System.out.println("[ROOM] [PLAYER INFO] Player " + player.getName() + " removed from waitlist in Room " + room.getName());
				
				
				
			}
		}else {
			sendRoomListToPlayer(player);
		}
		
		msg.put("event", "LEAVING WAITLIST");
		player.sendMessage(msg.toString());
	}
	
	//falta por hacer
	private void checkPeopleWaitingToJoin(Room room) {
		/*
		// Comprobamos si hay algún player en la waiting room
		addPlayerToRoomFromWaitingList(sala);

		// Comprobamos si hay gente esperando al matchmaking
		checkMatchmaking(sala);
		*/
	}
	
	////////////////////////////////////////
	//MÉTODOS DE ACCESO Y GESTIÓN DE SALAS//
	////////////////////////////////////////
	
	//MÉTODO QUE INTENTA AÑADIR UN JUGADOR A UNA SALA. PRIMERO, LO SACA DEL LOBBY, DESPUÉS
	//RECOGE EL VALOR QUE DEVUELVE EL MÉTODO ADDPLAYER DE LA CLASE ROOM Y EN FUNCIÓN DEL RESULTADO:
	//SI SE UNE, SI LA PARTIDA HA EMPEZADO UNE AL JUGADOR A LA PARTIDA, Y SI NO HA EMPEZADO INTENTA EMPEZARLA
	//SI ENTRA EN LA LISTA DE ESPERA SE ADVIERTE AL SCHEDULER DE QUE LO RETIRE A LOS CINCO SEGUNDOS
	//SI OCURRE UN ERROR SE LE DEVUELVE AL LOBBY Y SE ACTUALIZA SU INFORMACIÓN
	
	public void joinRoom(Player player, String roomName) throws Exception {
		
		ObjectNode msg = mapper.createObjectNode();
		
		leaveLobby(player);
		
		Room room = rooms.get(roomName);
		int joiningResult = room.addPlayer(player);
		
		switch (joiningResult) {
		
		case 0:
			
			synchronized(player.getSession()){
				player.getSession().getAttributes().put(ROOM_ATTRIBUTE, room);
			}

			msg.put("event", "JOINING ROOM");
			msg.put("roomName", room.getName());
			player.sendMessage(msg.toString());
			
			broadcastRoomInfoToRoom(room);
			broadcastRoomListToLobby();

			if (room.hasStarted()) {
				
				playersInGame.put(player.getName(), player);
				broadcastPlayerListToAll();
				
				try {
					room.getGame().sendBeginningMsgToPlayer(player);
				} catch (Exception e) {
					e.printStackTrace();
				}
					
			}else if (room.startGameAuto()) {
				
				for (Player p : room.getPlayers()) {
					playersInGame.put(p.getName(), p);
				}
				broadcastPlayerListToAll();
			}
			break;
			
		case 1:
			
			synchronized(player.getSession()) {
				player.getSession().getAttributes().put(ROOM_ATTRIBUTE, room); 
			}
			
			msg.put("event", "WAITING ROOM");
			msg.put("roomName", room.getName());
			player.sendMessage(msg.toString());
			
			lastInWaitlists.put(player);
			waitlistScheduler.schedule(() -> removeFromWaitlist(), 5, TimeUnit.SECONDS);
			break;
			
		case -1:
			
			msg.put("event", "ERROR");
			msg.put("type", "JOINING ROOM ERROR");
			player.sendMessage(msg.toString());
			
			joinLobby(player);
			break;
		}
		
	}

	//MÉTODO QUE CREA UNA SALA NUEVA
	//COMPRUEBA QUE TANTO EL NOMBRE COMO LA DIFICULTAD COMO EL TIPO SEAN VÁLIDOS, SI NO LO SON LO CORRIGE
	//DESPUÉS CREA UNA SALA NUEVA CON ESTOS ATRIBUTOS, LA GUARDA EN EL MAPA DE SALAS Y METE AL JUGADOR EN ELLA
	//POR ÚLTIMO 
	
	public void createRoom(Player player, String roomType, String roomDiff, String roomName) throws Exception {

		String ROOM_NAME;
		
		if(!rooms.containsKey(roomName)) {
			ROOM_NAME = roomName;
		}else {
			System.out.println("[LOBBY] [ROOM ERROR] Invalid room name creating Room " + roomName + ". Room with that name already exists creating alternative name");
			while(rooms.containsKey("roomName")) {
				roomName += "Alt";
			}
			ROOM_NAME = roomName;
		}
			
		int MAX_PLAYERS;
			
		switch(roomType) {
		case "DUEL":
			MAX_PLAYERS = 2;
			break;
		case "BATTLE ROYALE":
			MAX_PLAYERS = 10;
			break;
		default:
			System.out.println("[LOBBY] [ROOM ERROR] Invalid gamemode value creating Room " + roomName + ". Setting game to Duel by default");
			roomType = "DUEL";
			MAX_PLAYERS = 2;
			break;
		}
			
		String ROOM_DIFFICULTY;
			
		if(roomDiff == "EASY" || roomDiff == "MEDIUM" || roomDiff == "HARD") {
			ROOM_DIFFICULTY = roomDiff;
		}else {
			System.out.println("[LOBBY] [ROOM ERROR] Invalid difficulty value creating Room " + roomName + ". Setting difficulty to Easy by default");
			ROOM_DIFFICULTY = "EASY";
		}
			
		Room room = new Room(ROOM_NAME, ROOM_DIFFICULTY, MAX_PLAYERS);
		System.out.println("[LOBBY] [ROOM INFO] Created new " + roomType + " room with name " + ROOM_NAME + " and difficulty set to " + ROOM_DIFFICULTY);
		rooms.put(room.getName(), room);
		
		joinRoom(player, room.getName());
		
		while (checkMatchmaking(room) && !room.isFull()) {};
	}

	//MÉTODO QUE INTENTA RETIRAR A UN JUGADOR DE UNA SALA
	//EMPLEA EL MÉTODO REMOVEPLAYER DE LA CLASE ROOM, QUE ELIMINARÁ LA PARTIDA DE LA SALA SI ES EL ÚLTIMO JUGADOR QUE QUEDABA EN ELLA
	//DESPUÉS, SE COMPRUEBA SI HAY ALGUIEN ESPERANDO PARA ENTRAR A LA SALA, O POR MATCHMAKING O POR LISTA DE ESPERA
	//FINALMENTE SE COMPRUEBA SI LA SALA HA QUEDADO VACÍA. SI LO ESTÁ, SE BORRA LA SALA. SI NO, SE ACTUALIZA LA INFORMACIÓN NECESARIA
	//SE DEVUELVE AL JUGADOR AL LOBBY Y SE ACTUALIZA SU INFORMACIÓN

	public void leaveRoom(Player player, Room room, WebSocketSession session){
			
		room.removePlayer(player);
		checkPeopleWaitingToJoin(room);
			
		if(room.isEmpty()) {
				
			System.out.println("[LOBBY] [ROOM INFO] Room " + room.getName() + " is now empty. Deleting room");
			
			ObjectNode msg = mapper.createObjectNode();
			msg.put("event", "DELETING ROOM");
			msg.put("roomName", room.getName());
			broadcastMsgToLobby(msg.toString());
			rooms.remove(room.getName());
				
		}else{
				
			ObjectNode msg = mapper.createObjectNode();
			msg.put("event", "PLAYER LEAVING ROOM");
			msg.put("playerName", player.getName());
			room.broadcast(msg.toString());
				
			if(room.hasStarted() && !room.hasFinished()) {
					
				ObjectNode msg2 = mapper.createObjectNode();
				msg2.put("event", "PLAYER LEAVING GAME");
				msg2.put("id", player.getPlayerId());
				room.broadcast(msg2.toString());
			}
		}
			
		if(playersInGame.contains(player)) playersInGame.remove(player.getName());
		broadcastPlayerListToAll();
			
		synchronized(session) {
			session.getAttributes().remove(ROOM_ATTRIBUTE);
		}
			
		joinLobby(player);
		broadcastRoomListToLobby();
	}
	
	
	/////////////////////////////////
	//MÉTODOS DE GESTIÓN DE PARTIDA//
	/////////////////////////////////
	
	//falta por hacer
	public void startGameManual(Room room) {
		/*
		 * if (sala.tryStartGame()) {
					for (Player p : sala.getPlayers()) {
						inGamePlayers.put(p.getPlayerName(), p);
					}
					sendPlayerListMessage();
					sendGetRoomsMessageAll();
				}
		 */
	}
	
	//falta por hacer
	public void updatePlayerMovement(Player player, Room room, JsonNode node) {
		/*
		 * player.loadMovement(node.path("movement").get("thrust").asBoolean(),
						node.path("movement").get("brake").asBoolean(),
						node.path("movement").get("rotLeft").asBoolean(),
						node.path("movement").get("rotRight").asBoolean(), node.path("propeller").asBoolean());
				if (node.path("bullet").asBoolean()) {
					Projectile projectile = new Projectile(player, sala.getGame().projectileId.incrementAndGet());
					// Gestiona el número de balas
					if (projectile.getOwner().getAmmo() > 0) {
						projectile.getOwner().decreaseAmmo();
					}
					if (projectile.getOwner().getAmmo() > 0) {
						sala.getGame().addProjectile(projectile.getId(), projectile);
					}
				}
		 */
	}
	
	///////////////////////////////
	//MÉTODOS DE GESTIÓN DEL CHAT//
	///////////////////////////////
	
	//falta por hacer
	public void newChatMsg(String text) {
		/*
		 * String text = node.get("text").asText();
				System.out.println("[CHAT] Message received [" + player.getPlayerName() + "]: " + text);
				msg.put("event", "CHAT MSG");
				msg.put("text", text);
				msg.put("player", player.getName());
				sendMessageToAll(msg.toString());
		 */
		
	}
	
	//////////////////////////////////////////////
	//MÉTODOS DE COMUNICACIÓN Y PASO DE MENSAJES//
	//////////////////////////////////////////////
	
	//falta por hacer
	public void sendRanking(Player player) {
		// TODO Auto-generated method stub
		
	}

	//falta por hacer
	private void sendRoomListToPlayer(Player player) {
		// TODO Auto-generated method stub
		
	}

	//falta por hacer
	private void broadcastRoomListToLobby() {
		// TODO Auto-generated method stub
		
	}

	//falta por hacer
	private void broadcastPlayerListToAll() {
		// TODO Auto-generated method stub
		
	}

	//falta por hacer
	private void broadcastMsgToLobby(String msg) {
		// TODO Auto-generated method stub
		
	}
	
	//falta por hacer
	private void broadcastRoomInfoToRoom(Room room) {
		// TODO Auto-generated method stub
		
	}
	
	//falta por hacer
	private void broadcast(String msg) {
		
		//HAY QUE CAMBIAR LA FORMA EN LA QUE SE HACE BROADCAST. DESDE AQUÍ NO SE ACCEDE A LA SESIÓN
		/*
		for(WebSocketSession session : openSessions.values()) {
			try {
				synchronized(session) {
					session.sendMessage(new TextMessage(msg));
				}
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		*/
	}
	
	//////////////////////
	//MÉTODOS DE CONTROL//
	//////////////////////

}
