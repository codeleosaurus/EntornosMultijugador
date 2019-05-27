/*function getPokemon(callback) {
	$.ajax({
		method : "GET",
		data : {valueTipo1: tipo1_traducido, valueTipo2:tipo2_traducido},
		url : 'http://localhost:8080/pokemones',
		processData : false,
		headers : {
			"Content-Type" : "application/json"
		}
	}).done(function(data) {
		console.log(tipo1_traducido);
		console.log("Get: " + JSON.stringify(data));
	})
}
*/
function sendInfo(callback){
	$.ajax({
        //url:'xxrsv.oracle.apps.ibe.tcav2.EndCustomer',
        data:{value123: tipo1_traducido, valueTipo2:tipo2_traducido, valueGen:generacion_traducida, valueLegen:legendario_traducido},
        type:'get',
        url : 'http://localhost:8080/pokemones'
        
	}).done(function(data) {
		mostrar(data);
	})
}

function remove(callback){
	$.ajax({
        //url:'xxrsv.oracle.apps.ibe.tcav2.EndCustomer',
        data:{id: pokActual},
        type:'get',
        url : 'http://localhost:8080/removepokemon'
        
	}).done(function(data) {
				console.log("Borrado");
				mostrar(data);
	})
}

function add(callback){
	$.ajax({
        //url:'xxrsv.oracle.apps.ibe.tcav2.EndCustomer',
        data:{valorTipo1: tipo1_add, valorTipo2:tipo2_add, valorGen:generacion_add, valorLegen:legendario_add, valorNum:numero_add, valorNombre:nombre_add},
        type:'get',
        url : 'http://localhost:8080/addpokemon'
        
	}).done(function(data) {
				console.log("añadido");
				mostrar(data);
	})
}
/*
 * function getPokemonFilter(callback) { $.ajax({
 * 
 * 
 * }).done(function(data) { //console.log("Get: " + JSON.stringify(data));
 * console.log(data); }) }
 */
function postPokemon() {
	$.ajax({
		method : "POST",
		url : 'http://localhost:8080/pokemones',
		processData : false,
		headers : {
			"Content-Type" : "application/json"
		},
	}).done(function(data) {
		console.log("Post: " + JSON.stringify(data));
	})
}


/*
 * function putPlayer() { game.player1.classS = classSelected; if (typeof
 * spaceshipParent !== 'undefined' && typeof spaceshipParent[0] !== 'undefined') {
 * game.player1.x = spaceshipParent[0].x; game.player1.y = spaceshipParent[0].y;
 * game.player1.rot = spaceship[0].rotation; game.player1.disparando =
 * clase.fireButton.isDown; game.player1.alive = clase.alive;
 * game.player1.usingUlt = clase.usingUlt; game.player1.deployed = gravActive; }
 * $.ajax({ method : "PUT", url : 'http://localhost:8080/jugadores/' +
 * game.player1.id, data : JSON.stringify(game.player1), processData : false,
 * headers : { "Content-Type" : "application/json" } }).done(function(data) { //
 * console.log("Actualizada posicion de player 1: " + // JSON.stringify(data)) }) }
 * 
 * function getPlayer(callback) { $.ajax({ method : "GET", url :
 * 'http://localhost:8080/jugadores/' + game.player2.id, processData : false,
 * headers : { "Content-Type" : "application/json" } }).done(function(data) {
 * game.player2 = JSON.parse(JSON.stringify(data)); callback(data); }) }
 * 
 * function deletePlayer(playerId) { $.ajax({ method : 'DELETE', url :
 * 'http://localhost:8080/jugadores/' + playerId }).done(function(player) {
 * console.log("Deleted player " + playerId) }) } // FUNCIONES DE MUNDO function
 * createWorld() { $.ajax({ method : "POST", url :
 * 'http://localhost:8080/mundo', processData : false, headers : {
 * "Content-Type" : "application/json" }, }).done(function(data) {
 * console.log("World created: " + JSON.stringify(data)); game.world1 = data; }) }
 * 
 * function putWorld() { game.world1.polvoPos = polvo; game.world1.bhPos = bh;
 * game.world1.lsRot = lootShip.rotation; game.world1.lsPosX = lootPosX;
 * game.world1.lsPosY = lootPosY; if (claseLoot != null) game.world1.lsHP =
 * claseLoot.health;
 * 
 * $.ajax({ method : "PUT", url : 'http://localhost:8080/mundo', data :
 * JSON.stringify(game.world1), processData : false, headers : { "Content-Type" :
 * "application/json" } }).done(function(data) { // console.log("Actualizada
 * posicion de player 1: " + // JSON.stringify(data)) }) }
 * 
 * function getWorld(callback) { $.ajax({ method : "GET", url :
 * 'http://localhost:8080/mundo', processData : false, headers : {
 * "Content-Type" : "application/json" } }).done(function(data) { game.world1 =
 * JSON.parse(JSON.stringify(data)); callback(data); })
 */