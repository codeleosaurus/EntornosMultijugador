package spacewar;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SpacewarGame {

	private final static int FPS = 30;
	private static long TICK_DELAY;
	public final static boolean DEBUG_MODE = true;
	public final static boolean VERBOSE_MODE = true;
	public final static int HIT_POINTS = 5;
	public final static int KILL_POINTS = 10;
	ObjectMapper mapper = new ObjectMapper();
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public Room room;
	
	public boolean started = false;
	public boolean finished = false;
	
	private Map<String, Player> players = new ConcurrentHashMap<>();
	private Map<Integer, Projectile> projectiles = new ConcurrentHashMap<>();
	public AtomicInteger projectileId = new AtomicInteger(0);
	
	///////////////
	//CONSTRUCTOR//
	///////////////
	
	public SpacewarGame(String diff, Room room) {
		switch(diff) {
		case "EASY":
			TICK_DELAY = 1000 / FPS;
			break;
		case "MEDIUM":
			TICK_DELAY = 750 / FPS;
			break;
		case "HARD":
			TICK_DELAY = 500 / FPS;
			break;
		default:
			break;
		}
		
		this.room = room;
	}
	
	/////////////////////////
	//MÉTODO DE GAME UPDATE//
	/////////////////////////
	
	private void tick() {
		ObjectNode msg = mapper.createObjectNode();
		ArrayNode arrayNodePlayers = mapper.createArrayNode();
		ArrayNode arrayNodeProjectiles = mapper.createArrayNode();

		long thisInstant = System.currentTimeMillis();
		Set<Integer> bullets2Remove = new HashSet<>();
		boolean removeBullets = false;
		
		try {
			String winnerPlayer = null;
			
			for (Player player : getPlayers()) {
				
				ObjectNode playerJSON = mapper.createObjectNode();
				
				if(player.getLives()>0) {
					
					player.calculateMovement();
					
					playerJSON.put("id", player.getPlayerId());
					playerJSON.put("playerName", player.getName());
					playerJSON.put("shipType", player.getShipType());
					playerJSON.put("posX", player.getPosX());
					playerJSON.put("posY", player.getPosY());
					playerJSON.put("facingAngle", player.getFacingAngle());
					playerJSON.put("hp", player.getLives());
					playerJSON.put("points", player.getPoints());
					
					if(players.size() == 1) winnerPlayer = player.getSession().getId();
					
				}else if(player.isAlive()) {
					
					player.setAlive(false);
					
					playerJSON.put("id", player.getPlayerId());
					
					players.remove(player.getSession().getId());
					
					System.out.println("[GAME] [PLAYER INFO] Player " + player.getName() + " has been defeated");
					
					if(players.size() == 0) winnerPlayer = player.getSession().getId();
				}
				
				playerJSON.put("alive", player.isAlive());
				arrayNodePlayers.addPOJO(playerJSON);
				
			}
			
			if(winnerPlayer != null) {
				
				players.remove(winnerPlayer);
				finishGame(winnerPlayer);
			}

			for (Projectile projectile : getProjectiles()) {
				projectile.applyVelocity2Position();
				
				for (Player player : getPlayers()) {
					
					if (player.isAlive() && (projectile.getOwner().getPlayerId() != player.getPlayerId()) && player.intersect(projectile)) {
						
						projectile.setHit(true);
						player.decreaseHP();
						
						if(player.isAlive()) {
							if(player.getLives() != 0) {
								projectile.getOwner().increasePoints(HIT_POINTS);
							}else {
								projectile.getOwner().increasePoints(KILL_POINTS);
							}
						}
					
						break;
					}
				}
				
				ObjectNode projectileJSON = mapper.createObjectNode();
				projectileJSON.put("id", projectile.getId());

				if (!projectile.isHit() && projectile.isAlive(thisInstant)) {
					
					projectileJSON.put("posX", projectile.getPosX());
					projectileJSON.put("posY", projectile.getPosY());
					projectileJSON.put("facingAngle", projectile.getFacingAngle());
					projectileJSON.put("isAlive", true);
					
				} else {
					
					removeBullets = true;
					bullets2Remove.add(projectile.getId());
					
					projectileJSON.put("isAlive", false);
					
					if (projectile.isHit()) {
						
						projectileJSON.put("isHit", true);
						projectileJSON.put("posX", projectile.getPosX());
						projectileJSON.put("posY", projectile.getPosY());
					}
				}
				
				arrayNodeProjectiles.addPOJO(projectileJSON);
			}

			if (removeBullets)
				this.projectiles.keySet().removeAll(bullets2Remove);

			msg.put("event", "GAME STATE UPDATE");
			msg.putPOJO("players", arrayNodePlayers);
			msg.putPOJO("projectiles", arrayNodeProjectiles);
			
			this.room.broadcast(msg.toString());
			
		} catch (Throwable ex) {
		}
	}

	///////////////////////////////////
	//MÉTODOS DE GESTIÓN DE JUGADORES//
	///////////////////////////////////
	
	public void addPlayer(Player player) {
		
		players.put(player.getSession().getId(), player);
		System.out.println("[GAME] [PLAYER INFO] Player " + player.getName() + " joined the game in Room '" + room.getName() + "'");
	}

	public void removePlayer(Player player) {
		
		players.remove(player.getSession().getId());
		System.out.println("[GAME] [PLAYER INFO] Player " + player.getName() + " left the game in Room '" + room.getName() + "'");
	}

	/////////////////////////////////////
	//MÉTODOS DE GESTIÓN DE PROYECTILES//
	/////////////////////////////////////
	
	public void addProjectile(int id, Projectile projectile) {
		projectiles.put(id, projectile);
	}

	public void removeProjectile(Projectile projectile) {
		players.remove(projectile.getId(), projectile);
	}

	///////////////////////////
	//MÉTODOS DE COMUNICACIÓN//
	///////////////////////////
	
	public void sendBeginningMsgToPlayer(Player player) throws Exception{
		
		ObjectNode msg = mapper.createObjectNode();
		msg.put("event", "JOINING GAME");
		msg.put("id", player.getPlayerId());
		msg.put("shipType", player.getShipType());
		player.sendMessage(msg.toString());
	
	
		ObjectNode msg2 = mapper.createObjectNode();
		msg2.put("event", "GAME STARTING");
		player.sendMessage(msg2.toString());
	}
	
	public void broadcastBeginningMsg() throws Exception{
		
		for(Player player : players.values()) {
			
			ObjectNode msg = mapper.createObjectNode();
			msg.put("event", "JOINING GAME");
			msg.put("id", player.getPlayerId());
			msg.put("shipType", player.getShipType());
			player.sendMessage(msg.toString());
		}
		
		ObjectNode msg2 = mapper.createObjectNode();
		msg2.put("event", "GAME STARTING");
		room.broadcast(msg2.toString());
	}
	
	//////////////////////////////////////////
	//MÉTODOS DE CONTROL DEL ESTADO DE JUEGO//
	//////////////////////////////////////////
	
	public void startGameLoop() {
		
		if(!hasStarted()) {
			scheduler = Executors.newScheduledThreadPool(1);
			scheduler.scheduleAtFixedRate(() -> tick(), TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
			started = true;
		}
	}
	
	public void stopGameLoop() {
		
		if (scheduler != null) {
			scheduler.shutdown();
			started = false;
		}
	}
	
	public void finishGame(String winnerPlayer) {
		
		if(!hasFinished()) {
			finished = true;
			room.setFinished(finished);
			
			System.out.println("[GAME] [GAME INFO] The game has finished. The winner is " + winnerPlayer);
			
			ObjectNode msg = mapper.createObjectNode();
			msg.put("event", "GAME END");
			ArrayNode losersArray = mapper.createArrayNode();
			
			for (Player player : room.getPlayers()) {
				
				if (player.getSession().getId() == winnerPlayer) {
					
					ObjectNode winnerJSON = mapper.createObjectNode();
					winnerJSON.put("playerName", player.getName());
					winnerJSON.put("points", player.getPoints());
					winnerJSON.put("hp", player.getLives());
					msg.putPOJO("winner", winnerJSON);
					
				} else {
					
					ObjectNode loserJSON = mapper.createObjectNode();
					loserJSON.put("playerName", player.getName());
					loserJSON.put("points", player.getPoints());
					loserJSON.put("hp", player.getLives());
					losersArray.addPOJO(loserJSON);
				}
			}

			msg.putPOJO("losers", losersArray);
			room.broadcast(msg.toString());
			stopGameLoop();
		}
		
	}
	
	////////////////////////
	//MÉTODOS DE GET Y SET//
	////////////////////////
	
	public boolean hasStarted() {
		return started;
	}
	
	public boolean hasFinished() {
		return finished;
	}
	
	public Collection<Player> getPlayers() {
		return players.values();
	}
	
	public Collection<Projectile> getProjectiles() {
		return projectiles.values();
	}
}
