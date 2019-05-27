package b00mProductions.GDD;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.BSONObject;
import org.bson.BsonDocument;
import org.bson.BsonString;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

@RestController
public class Controller {

	// CONECTAR CON BASE DE DATOS
	MongoClient mongoClient = new MongoClient("localhost", 27017);
	// CREA UNA BASE DE DATOS
	MongoDatabase db = mongoClient.getDatabase("pokedex");

	// INICIALIZAR CONVERTER
	Converter converter = new Converter();
	ConverterArray converterA = new ConverterArray();

	// CREAR LA COLECCIÓN
	MongoCollection<org.bson.Document> collection = db.getCollection("pokemones");

	// CREA UN DOCUMENTO A PARTIR DE UN JSON
	// Document doc = Document.parse(converter.getJson());

	@PostMapping(value = "/pokemones")
	@ResponseStatus(HttpStatus.CREATED)
	public String init() {
		// collection.insertOne(doc);
		collection.drop();
		for (int i = 0; i < 801; i++) {
			Document doc = Document.parse(converterA.getJson()[i]);
			collection.insertOne(doc);
		}

		return "Inserted!" + collection.count();
	}
	

	@GetMapping(value = "/pokemones")
	public List<Document> getPokemon(HttpServletRequest request) {
		// String out = collection.find().first().toString();
		String type1 = request.getParameter("value123");
		String type2 = request.getParameter("valueTipo2");
		int gen = Integer.parseInt(request.getParameter("valueGen"));
		int legen = Integer.parseInt(request.getParameter("valueLegen"));

		System.out.println(type1);
		System.out.println(type2);
		// String generacion=request.getParameter("valueGeneracion");

		// Bson filter = new Document("type1", "grass").append("type2",
		// "poison").append("generation", 3);

		Document document = new Document();

		if (!type1.equals("----")) {
			document.append("type1", type1);
		}
		if (!type2.equals("----")) {

			document.append("type2", type2);
		}

		if (gen != -1) {
			document.append("generation", gen);
		}

		document.append("is_legendary", legen);

		Bson filter = document;

		List<Document> all = collection.find(filter).into(new ArrayList<Document>());
		// String out = all.toString();
		System.out.println(all);
		return all;
	}
	
	@GetMapping(value = "/removepokemon")
	public  List<Document> remove(HttpServletRequest request) {
		// String out = collection.find().first().toString();
		int id = Integer.parseInt(request.getParameter("id"));

		System.out.println(id);
		Document document = new Document();
		document.append("pokedex_number", id);
		Bson removeFilter = document;

		List<Document> test = collection.find(removeFilter).into(new ArrayList<Document>());
		System.out.println(test);
		//System.out.println(removeFilter);
		//collection.remove( { pokedex_number: { $eq: id } } );
		collection.deleteOne(removeFilter);
		return collection.find().into(new ArrayList<Document>());
	}

	@GetMapping(value = "/addpokemon")
	public  List<Document> add(HttpServletRequest request) {

		String type1 = request.getParameter("valorTipo1");
		String type2 = request.getParameter("valorTipo2");
		int gen = Integer.parseInt(request.getParameter("valorGen"));
		int legen = Integer.parseInt(request.getParameter("valorLegen"));
		int num = Integer.parseInt(request.getParameter("valorNum"));
		String nombre = request.getParameter("valorNombre");

		Document document = new Document();
		document.append("name", nombre);
		document.append("pokedex_number", num);
		document.append("type1", type1);
		document.append("type2", type2);
		document.append("generation", gen);
		document.append("is_legendary", legen);
		collection.insertOne(document);

		return collection.find().into(new ArrayList<Document>());
	}

	/*
	 * @GetMapping() public String getPokemonFilter(String type) { /* Bson filter =
	 * new Document("type1", type); List<Document> all =
	 * collection.find(filter).into(new ArrayList<Document>()); //String out =
	 * collection.find().first().toString(); return all;
	 * 
	 * String tipo="jajajaj"; return tipo; }
	 * 
	 * /*
	 * 
	 * @GetMapping(value = "/pokemones{type}") public void filterTipo1(@PathVariable
	 * long type) { Bson filter = new Document("type1", type); // --> te muestra los
	 * documentos (pokemon) que satisfagan el filtro List<Document> all =
	 * collection.find(filter).into(new ArrayList<Document>());
	 * 
	 * System.out.println(all); }
	 * 
	 * /*@RequestMapping(value = "/pokemones", method = RequestMethod.GET) public
	 * void getBD() { // TE MUESTRA LAS BASES DE DATOS ACTIVAS MongoCursor<String>
	 * dbsCursor = mongoClient.listDatabaseNames().iterator();
	 * 
	 * while (dbsCursor.hasNext()) { System.out.println(dbsCursor.next()); } }
	 */

	/*
	 * AtomicLong nextId = new AtomicLong(0); Random rnd = new Random();
	 * 
	 * @RequestMapping(value="/jugadores",method=RequestMethod.GET) public
	 * List<Jugador> getJugadores(){ return pokemones; }
	 * 
	 * 
	 * @RequestMapping(value="/jugadores",method=RequestMethod.POST) public
	 * ResponseEntity<Boolean> addJugador(){ Jugador a = new Jugador();
	 * jugadores.add(a); return new ResponseEntity<>(true, HttpStatus.CREATED); }
	 * 
	 * // Con GET recuperamos el número de jugadores
	 * 
	 * @GetMapping(value = "/pokemon") public Collection<Pokemon> getPlayers() {
	 * return jugadores.values(); }
	 * 
	 * // Con POST creamos un nuevo jugador
	 * 
	 * @PostMapping(value = "/jugadores")
	 * 
	 * @ResponseStatus(HttpStatus.CREATED) public Jugador newJugador() { Jugador
	 * jugador = new Jugador(); long id = nextId.incrementAndGet();
	 * jugador.setId(id); jugador.setX(rnd.nextInt(700));
	 * jugador.setY(rnd.nextInt(500)); jugador.setRot(0.0);
	 * jugador.setDisparando(false); jugador.setAlive(true); jugador.setClassS(0);
	 * jugador.setUsingUlt(false); jugador.setDeployed(false);
	 * jugadores.put(jugador.getId(), jugador); return jugador; }
	 * 
	 * // Con este GET, podemos recuperar la información particular de cada uno de
	 * los // jugadores
	 * 
	 * @GetMapping(value = "/jugadores/{id}") public ResponseEntity<Jugador>
	 * getJugador(@PathVariable long id) { Jugador jugador = jugadores.get(id); if
	 * (jugador != null) { return new ResponseEntity<>(jugador, HttpStatus.OK); }
	 * else { return new ResponseEntity<>(HttpStatus.NOT_FOUND); } }
	 * 
	 * // Con este PUT actualizamos la información del jugador con ID = id
	 * 
	 * @PutMapping(value = "/jugadores/{id}") public ResponseEntity<Jugador>
	 * updateJugador(@PathVariable long id, @RequestBody Jugador jugador) { Jugador
	 * savedPlayer = jugadores.get(jugador.getId()); if (savedPlayer != null) {
	 * jugadores.put(id, jugador); return new ResponseEntity<>(jugador,
	 * HttpStatus.OK); } else { return new ResponseEntity<>(HttpStatus.NOT_FOUND); }
	 * }
	 * 
	 * // Con este DELETE borramos el jugador con ID = id
	 * 
	 * @DeleteMapping(value = "/jugadores/{id}") public ResponseEntity<Jugador>
	 * borraJugador(@PathVariable long id) { Jugador savedPlayer =
	 * jugadores.get(id); if (savedPlayer != null) {
	 * jugadores.remove(savedPlayer.getId()); return new
	 * ResponseEntity<>(savedPlayer, HttpStatus.OK); } else { return new
	 * ResponseEntity<>(HttpStatus.NOT_FOUND); } }
	 * 
	 * 
	 * 
	 * /*
	 * 
	 * private List<Jugador> jugadores = new ArrayList<>();
	 * 
	 * 
	 * @RequestMapping(value="/jugadores",method=RequestMethod.GET) public
	 * List<Jugador> getJugadores(){ return jugadores; }
	 * 
	 * 
	 * @RequestMapping(value="/jugadores",method=RequestMethod.POST) public
	 * ResponseEntity<Boolean> addJugador(){ Jugador a = new Jugador();
	 * jugadores.add(a); return new ResponseEntity<>(true, HttpStatus.CREATED);
	 * 
	 * 
	 * }
	 * 
	 * @RequestMapping(value = "/jugadores", method = RequestMethod.GET) public int
	 * getNumPlayers() { return jugadores.size(); }
	 * 
	 * @RequestMapping(value = "/jugadores/{id}", method = RequestMethod.GET) public
	 * ResponseEntity<Jugador> getJugador(@PathVariable int id) { Jugador
	 * savedPlayer = jugadores.get(id);
	 * 
	 * if (savedPlayer != null) { return new ResponseEntity<>(savedPlayer,
	 * HttpStatus.OK); } else { return new ResponseEntity<>(HttpStatus.NOT_FOUND); }
	 * } }
	 *
	 * 
	 * /*
	 * 
	 * @RequestMapping(value="/jugadores", method=RequestMethod.DELETE) public
	 * ResponseEntity<Jugador> borraJugador() { jugadores.remove(arg0)
	 * 
	 * return new ResponseEntity<>(HttpStatus.OK);
	 * 
	 * }
	 */
}