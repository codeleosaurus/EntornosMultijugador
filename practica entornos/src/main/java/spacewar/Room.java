package spacewar;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Room {
	
	////////////////////////////
	//DECLARACIÓN DE ATRIBUTOS//
	////////////////////////////
	
	//VALORES DEL JUEGO
	public final int MAX_PLAYERS;
	public final int MIN_PLAYERS = 2;
	
	//INSTANCIA DEL JUEGO
	private SpacewarGame game;
	
	//CONTROL DEL ESTADO DE LA SALA 
	private AtomicBoolean started;
	private AtomicBoolean finished;
	
	//ATRIBUTOS DE LA SALA
	private final String name;
	private ConcurrentHashMap<String, Player> players;
	private String diff;
	private AtomicInteger n_players;
	public BlockingQueue<Player> waitlist;
	
	///////////////
	//CONSTRUCTOR//
	///////////////
	
	public Room(String name, String diff, int max_players) {
		
		//INICIALIZAR ATRIBUTOS DE LA SALA Y CREAR INSTANCIA DEL JUEGO
		this.name = name;
		this.MAX_PLAYERS = max_players;
		this.diff = diff;
		
		this.started = new AtomicBoolean(false);
		this.finished = new AtomicBoolean(false);
		
		this.players = new ConcurrentHashMap<>();
		this.n_players = new AtomicInteger(0);
		this.waitlist = new LinkedBlockingQueue<Player>();
		this.game = new SpacewarGame(diff, this);
	}
	
	///////////////////////////////////
	//MÉTODOS DE GESTIÓN DE JUGADORES//
	///////////////////////////////////
	
	//MÉTODO QUE DEVUELVE UN INT EN FUNCIÓN DE CUÁL HAYA SIDO EL RESULTADO DE INTENTAR AÑADIR UN JUGADOR.
	//DEVUELVE -1: ERROR AL AÑADIR JUGADOR (PARTIDA TERMINADA O EL JUGADOR YA EXISTE EN LA SALA).
	//DEVUELVE 0: EL JUGADOR SE HA AÑADIDO CORRECTAMENTE.
	//DEVUELVE 1: SE PUEDE AÑADIR PERO LA SALA ESTÁ LLENA. SE AÑADE A LA LISTA DE ESPERA.
	
	public synchronized int addPlayer(Player player) throws Exception{
		
		try {
			
			if(!hasFinished() && !players.containsKey(player.getSession().getId())) {
			
				if(!isFull()) {
				
					players.put(player.getSession().getId(), player);
					System.out.println("[ROOM] [PLAYER INFO] Player " + player.getName() + " joined Room '" + this.name + "'");
					
					game.addPlayer(player);
					player.setPlayerId(n_players.getAndIncrement());
				
					return 0;
				
				}else {
				
					waitlist.put(player);
					System.out.println("[ROOM] [PLAYER INFO] Player " + player.getName() + " joined WAITLIST for Room '" + this.name + "'");
					return 1;
				
				}
			}else {
			
				System.out.println("[ROOM] [PLAYER ERROR] Player " + player.getName() + " was unable to join Room '" + this.name + "'");
				return -1;
			
			}
		
		}catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	//MÉTODO QUE EXTRAE UN JUGADOR DE LA LISTA DE ESPERA E INTENTA AÑADIRLO A LA SALA
	//DEVUELVE REFERENCIA AL PROPIO JUGADOR
	
	public synchronized Player tryAddPlayerFromWaitlist() throws Exception{
		
		Player joiningPlayer = waitlist.poll();
		
		if(joiningPlayer != null) {
			
			try {
				
				if(addPlayer(joiningPlayer) == 0) {
					
					System.out.println("[ROOM] [PLAYER INFO] Player " + joiningPlayer.getName() + " joined Room '" + this.name + "' from the WAITLIST.");
					return joiningPlayer;
				}
				
			}catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		
		return null;
	}

	//MÉTODO QUE EXTRAE UN JUGADOR DE LA SALA (Y DE LA PARTIDA DE LA SALA).
	//SI NO QUEDA NINGÚN JUGADOR EN LA SALA, SE ACABA LA PARTIDA DE LA SALA.
	
	public synchronized void removePlayer(Player player) {
		
		
		players.remove(player.getSession().getId());
		game.removePlayer(player);
		player.resetStats();
			
		System.out.println("[ROOM] [PLAYER INFO] Player " + player.getName() + " removed from Room '" + this.name + "'");
			
		tryEndGame();
	}
	
	////////////////////////////////////
	//MÉTODOS DE GESTIÓN DE LA PARTIDA//
	////////////////////////////////////
	
	//MÉTODO QUE INICIA LA PARTIDA AUTOMÁTICAMENTE SI LA SALA ESTÁ LLENA
	
	public synchronized boolean startGameAuto() throws Exception {
		
		if(!hasStarted()) {
			
			try {
				
				if(isFull()) {
					
					System.out.println("[ROOM] [GAME INFO] Full room. Starting game from Room '" + this.name + "'");
					setStarted(true);
					//game.sendStartMsg();
					game.broadcastBeginningMsg();
					game.startGameLoop();
					return true;
				}
			}catch(Exception e) {
				
				e.printStackTrace();
			}
		}else {
			
			System.out.println("[ROOM] [GAME ERROR] Unable to start game from Room '" + this.name + "'. A game has already started");
		}
		
		return false;
	}
	
	//MÉTODO QUE INTENTA INICIAR LA PARTIDA, SI EN LA SALA HAY JUGADORES SUFICIENTES
	//EL CREADOR PUEDE INTENTAR INICIAR LA PARTIDA AUNQUE LA SALA NO ESTÉ LLENA
	
	public synchronized boolean startGameManual() throws Exception{
		
		if(!hasStarted()) {
			
			try {
				
				if(getNumberOfPlayers() >= MIN_PLAYERS) {
					
					System.out.println("[ROOM] [GAME INFO] Manual start selected with enough players. Starting game from Room '" + this.name + "'");
					setStarted(true);
					//game.sendStartMsg();
					game.broadcastBeginningMsg();
					game.startGameLoop();
					return true;
				}
			}catch(Exception e) {
				
				e.printStackTrace();
			}
		}else {
			
			System.out.println("[ROOM] [GAME ERROR] Unable to start game from Room '" + this.name + "'. A game has already started");
		}
		
		return false;
	}
	
	//MÉTODO QUE TRAS ACTUALIZAR EL NÚMERO DE JUGADORES COMPRUEBA CUÁNTOS QUEDAN EN LA SALA. SI NO HAY NINGUNO, SE ACABA LA PARTIDA.
	
	public synchronized void tryEndGame() {
		
		if(n_players.decrementAndGet() == 0) {
			
			game.stopGameLoop();
			System.out.println("[ROOM] [GAME INFO] Empty room. Ending game from Room '" + this.name + "'");
		}
	}
	
	/////////////////////////////////////////
	//MÉTODOS DE GESTIÓN DE LA COMUNICACIÓN//
	/////////////////////////////////////////
	
	//MÉTODO QUE ENVÍA UN MENSAJE A TODOS LOS JUGADORES DE LA SALA
	
	public void broadcast(String msg) {
		
		for(Player player : getPlayers()) {
			
			try {
				
				player.sendMessage(msg);
				
			}catch(Throwable e) {
				
				System.err.println("[ROOM] [MSG ERROR] Exception sending msg to player " + player.getName());
				e.printStackTrace(System.err);
				
				try {
					
					this.removePlayer(player);
					
				} catch (Exception exc) {
					
					exc.printStackTrace();
				}
			}
		}
	}
	
	///////////////////////////////////////
	//COMPROBACIONES DE ESTADO DE LA SALA//
	///////////////////////////////////////
	
	public boolean hasStarted() {
		return this.started.get();
	}
	
	public boolean hasFinished() {
		return this.finished.get();
	}
	
	public void setStarted(boolean state) {
		this.started.set(state);
	}
	
	public void setFinished(boolean state) {
		this.finished.set(state);
	}
	
	public boolean isFull() {
		return n_players.get() == MAX_PLAYERS;
	}
	
	public boolean isEmpty() {
		return n_players.get() <= 0;
	}
	
	public boolean contains(Player player) {
		return players.contains(player);
	}
	
	/////////////////////////////////////
	//GET Y SET DE ATRIBUTOS DE LA SALA//
	/////////////////////////////////////
	
	public int getNumberOfPlayers() {
		return this.n_players.get();
	}
	
	public SpacewarGame getGame() {
		return game;
	}
	
	public Collection<Player> getPlayers(){
		return players.values();
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDifficulty() {
		return this.diff;
	}
	
	public String getGamemode() {
		if(MAX_PLAYERS == 2) {
			return "DUEL";
		}else {
			return "BR";
		}
	}
	
	public ObjectNode getRoomInfo(ObjectNode roomJSON) {
		
		roomJSON.put("roomName", getName());
		roomJSON.put("started", hasStarted());
		roomJSON.put("finished",  hasFinished());
		roomJSON.put("gamemode", getGamemode());
		roomJSON.put("difficulty",  getDifficulty());
		roomJSON.put("numberOfPlayers", getNumberOfPlayers());
		roomJSON.put("maxPlayers", MAX_PLAYERS);
		
		return roomJSON;
	}

}
