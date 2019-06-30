package spacewar;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class WebsocketGameHandler extends TextWebSocketHandler {

	///////////////////////////////////////////////////////////////////////////////
	//DECLARACIÓN E INICIALIZACIÓN DE ATRIBUTOS DEL HANDLER E INSTANCIA DEL LOBBY//
	///////////////////////////////////////////////////////////////////////////////
	
	private static final String PLAYER_ATTRIBUTE = "PLAYER";
	private static final String ROOM_ATTRIBUTE = "ROOM";
	private ObjectMapper mapper = new ObjectMapper();
	
	private ConcurrentHashMap<String, WebSocketSession> openSessions = new ConcurrentHashMap<>();
	private Lobby lobby = new Lobby();
	
	////////////////////////////////
	//GESTIÓN DE NUEVAS CONEXIONES//
	////////////////////////////////
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		
		String name;
		
		synchronized(session) {
			String[] uri = session.getUri().toString().split("/");
			name = uri[uri.length - 1];
		}
		
		if(!openSessions.containsKey(name)) {
			
			Player player = new Player(name, session);
			System.out.println("[SYSTEM] [INFO] Created new player with name " + name);
				
			synchronized(session) {
				session.getAttributes().put(PLAYER_ATTRIBUTE, player);
				//session.getAttributes().put(ROOM_ATTRIBUTE, "");
			}
			
			System.out.println("[SYSTEM] [INFO] Updated session attributes");
			
			openSessions.put(name, session);
			
			System.out.println("[SYSTEM] [INFO] Added session to open session list");
			
			lobby.allPlayers.put(name, player);
			
			System.out.println("[SYSTEM] [INFO] Added player to global player list");
			
			ObjectNode msg = mapper.createObjectNode();
			msg.put("event", "INIT SESSION"); 
			msg.put("validname", true);	      
			player.sendMessage(msg.toString());
			
			System.out.println("[SYSTEM] [INFO] Confirmation message sent to player");
			
		}else {
			
			System.out.println("[SYSTEM] [ERROR] Unable to create new player. Name " + name + " already used");
			
			ObjectNode msg = mapper.createObjectNode();
			msg.put("event", "INIT SESSION");
			msg.put("validname", false);
				
			synchronized(session) {
				session.sendMessage(new TextMessage(msg.toString()));
			}
		}
	}

	/////////////////////////////////
	//GESTIÓN DE RECIBO DE MENSAJES//
	/////////////////////////////////
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		
		try {
			
			JsonNode node = mapper.readTree(message.getPayload());
			String evento = node.get("event").asText();
			
			Player player = (Player) session.getAttributes().get(PLAYER_ATTRIBUTE);
			Room room = (Room) session.getAttributes().get(ROOM_ATTRIBUTE);

			switch (evento) { 
			
			case "GET RANKING":
				lobby.sendRanking(player);
				break;
			
			///////////////////////////////////////
			//MENSAJES DE GESTIÓN DE LOBBY Y SALA//
			///////////////////////////////////////
				
			//ENTRAR AL MATCHMAKING AUTOMÁTICO
				
			case "JOIN MATCHMAKING":
				lobby.joinMatchmaking(player, node.get("diff").asText(), node.get("mode").asText());
				break;

			//SALIR DEL MATCHMAKING AUTOMÁTICO
				
			case "LEAVE MATCHMAKING":
				lobby.leaveMatchmaking(player);
				break;

			case "LEAVE WAITLIST":
				lobby.leaveWaitlist(player);
				break;

			//JUGADOR ENTRA AL LOBBY
				
			case "JOIN LOBBY":
				lobby.joinLobby(player);
				break;

			//JUGADOR SALE DEL LOBBY
				
			case "LEAVE LOBBY":
				lobby.leaveLobby(player);
				break;

			//JUGADOR SE UNE A UNA SALA
				
			case "JOIN ROOM":
				lobby.joinRoom(player, node.get("roomName").asText());
				break;

			//JUGADOR CREA UNA SALA
				
			case "CREATE ROOM":
				lobby.createRoom(player, node.get("roomType").asText(), node.get("roomDiff").asText(), node.get("roomName").asText());
				break;

			//JUGADOR SALE DE UNA SALA
				
			case "LEAVE ROOM":
				lobby.leaveRoom(player, room, session);
				break;

			//////////////////////////////////
			//MENSAJES DE GESTIÓN DE PARTIDA//
			//////////////////////////////////


			//INICIO MANUAL DE PARTIDA
				
			case "START GAME MANUALLY":
				lobby.startGameManual(room);
				break;

			//ACTUALIZAR POSICION
				
			case "UPDATE MOVEMENT":
				lobby.updatePlayerMovement(player, room, node);
				break;

			////////////////////
			//MENSAJES DE CHAT//
			////////////////////
				
			case "CHAT MESSAGE":
				lobby.newChatMsg(player, node.get("text").asText());
				break;
				
			default:
				break;
			}

		} catch (Exception e) {
			
			System.err.println("[SYSTEM] [ERROR] Exception receiving message " + message.getPayload());
			e.printStackTrace(System.err);
		}
	}
	
	//////////////////////////////
	//GESTIÓN DE FIN DE CONEXIÓN//
	//////////////////////////////
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		
		Player player = (Player) session.getAttributes().get(PLAYER_ATTRIBUTE);
		Room room = (Room) session.getAttributes().get(ROOM_ATTRIBUTE);
		
		if(player == null) return;
		
		if(room != null) lobby.leaveRoom(player, room, session);
		
		lobby.leaveLobby(player);
		
		lobby.allPlayers.remove(player.getName());
		
		synchronized(session) {
			if(openSessions.containsKey(player.getName())) openSessions.remove(player.getName());
		}
		
		System.out.println("[SYSTEM] [INFO] Player " + player.getName() + " disconnected from the server");
		
	}
}
