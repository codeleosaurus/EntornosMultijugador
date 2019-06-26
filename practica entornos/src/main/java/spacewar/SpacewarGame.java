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

import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SpacewarGame {

	//public final static SpacewarGame INSTANCE = new SpacewarGame();

	private final static int FPS = 30;
	private static long TICK_DELAY;
	public final static boolean DEBUG_MODE = true;
	public final static boolean VERBOSE_MODE = true;
	int vidas;
	ObjectMapper mapper = new ObjectMapper();
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	// GLOBAL GAME ROOM
	
	private Map<String, Player> players = new ConcurrentHashMap<>();
	private Map<Integer, Projectile> projectiles = new ConcurrentHashMap<>();
	public AtomicInteger projectileId = new AtomicInteger(0);
	
	//HE AÑADIDO LA DIFICULTAD
	
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//FALTA POR ACTUALIZAR TODA ESTA CLASE
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	public SpacewarGame(String diff) {
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
	}
	
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//FALTA POR ACTUALIZAR TODA ESTA CLASE
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	private void tick() {
		ObjectNode json = mapper.createObjectNode();
		ArrayNode arrayNodePlayers = mapper.createArrayNode();
		ArrayNode arrayNodeProjectiles = mapper.createArrayNode();

		long thisInstant = System.currentTimeMillis();
		Set<Integer> bullets2Remove = new HashSet<>();
		boolean removeBullets = false;
		
//aqui dentro se lleva todo lo que va de colisiones, meter que se reste 1 a la vida al detectarse una 
		
		try {
			// Update players
			for (Player player : getPlayers()) {
				player.calculateMovement();

				ObjectNode jsonPlayer = mapper.createObjectNode();
				jsonPlayer.put("id", player.getPlayerId());
				jsonPlayer.put("name", player.getName());
				jsonPlayer.put("shipType", player.getShipType());
				jsonPlayer.put("posX", player.getPosX());
				jsonPlayer.put("posY", player.getPosY());
				jsonPlayer.put("facingAngle", player.getFacingAngle());
				arrayNodePlayers.addPOJO(jsonPlayer);
			}

			// Update bullets and handle collision
			for (Projectile projectile : getProjectiles()) {
				projectile.applyVelocity2Position();

				// Handle collision
				//ACTUALIZADO PARA QUE COMPARE NOMBRE EN VEZ DE ID
				//UPDATE: LO HE VUELTO A PONER EN FUNCIÓN DEL ID..
				
				for (Player player : getPlayers()) {
					//if (!(projectile.getOwner().getName().equals(player.getName())) && player.intersect(projectile)) {
					if (projectile.getOwner().getPlayerId() != player.getPlayerId() && player.intersect(projectile)) {
						System.out.println("Player " + player.getName() + " was hit!!!");
						projectile.setHit(true);
						//aqui se actualiza la vida, se resta bien 
						player.setLives(player.getLives()-1);
						//las vidas se restan a todos los jugadores en vez de al que ha sido golpeado, debe ponerse de modo
						//que se le resten al jugador en funcion de su id
						if(player.getLives()<=0) {
							player.setLives(0);
						}
						break;
					}
				}
				
				ObjectNode jsonProjectile = mapper.createObjectNode();
				jsonProjectile.put("id", projectile.getId());

				if (!projectile.isHit() && projectile.isAlive(thisInstant)) {
					jsonProjectile.put("posX", projectile.getPosX());
					jsonProjectile.put("posY", projectile.getPosY());
					jsonProjectile.put("facingAngle", projectile.getFacingAngle());
					jsonProjectile.put("isAlive", true);
				} else {
					removeBullets = true;
					bullets2Remove.add(projectile.getId());
					jsonProjectile.put("isAlive", false);
					if (projectile.isHit()) {
						jsonProjectile.put("isHit", true);
						jsonProjectile.put("posX", projectile.getPosX());
						jsonProjectile.put("posY", projectile.getPosY());
					}
				}
				arrayNodeProjectiles.addPOJO(jsonProjectile);
			}

			if (removeBullets)
				this.projectiles.keySet().removeAll(bullets2Remove);

			json.put("event", "GAME STATE UPDATE");
			json.putPOJO("players", arrayNodePlayers);
			json.putPOJO("projectiles", arrayNodeProjectiles);
			
			this.broadcast(json.toString());
		} catch (Throwable ex) {

		}
	}

	public void addPlayer(Player player) {
		//players.put(player.getSession().getName(), player);
		players.put(player.getName(), player);
		
		/*int count = numPlayers.getAndIncrement();
		
		if (count == 0) {
			this.startGameLoop();
		}
		*/
	}

	public Collection<Player> getPlayers() {
		return players.values();
	}

	public void removePlayer(Player player) {
		players.remove(player.getSession().getId());

		/*
		int count = this.numPlayers.decrementAndGet();
		if (count == 0) {
			this.stopGameLoop();
		}
		*/
	}

	public void addProjectile(int id, Projectile projectile) {
		projectiles.put(id, projectile);
	}

	public Collection<Projectile> getProjectiles() {
		return projectiles.values();
	}

	public void removeProjectile(Projectile projectile) {
		players.remove(projectile.getId(), projectile);
	}

	public void startGameLoop() {
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> tick(), TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

	public void stopGameLoop() {
		if (scheduler != null) {
			scheduler.shutdown();
		}
	}

	public void broadcast(String message) {
		for (Player player : getPlayers()) {
			try {
				player.sendMessage((message.toString()));
			} catch (Throwable ex) {
				System.err.println("Execption sending message to player " + player.getSession().getId());
				ex.printStackTrace(System.err);
				this.removePlayer(player);
			}
		}
	}

	public void handleCollision() {

	}

	
	//habrá que implementarlo en un futuro
	public ConcurrentHashMap<String, Integer> getScores() {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendBeginningMsgToPlayer(Player player) {
		// TODO Auto-generated method stub
		
	}
}
