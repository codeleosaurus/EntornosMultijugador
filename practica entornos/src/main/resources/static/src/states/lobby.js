Spacewar.lobbyState = function(game) {}
//funciones que se llaman al pulsar los botones
function crearSala(){
		mostrar(false);
		var roomName= prompt("Write the rooms name", "Villa oculta de la hoja")
		var dif= prompt("write dificulty(EASY, MEDIUM, HARD)", "EASY")
		var mode=prompt("Select playmode(BATTLE ROYALE,DUEL)","BATTLE ROYALE")
		let evento = new Object();
			evento.event = 'CREATE ROOM'
			evento.roomName = roomName;
			evento.roomDiff=dif;
			evento.roomType = mode;
			console.log("Room created, sending message to server")
			game.global.socket.send(JSON.stringify(evento))
			game.state.start('roomState')
	
}
function selecMatchmaking(){
	mostrar(false);
	var difM= prompt("write dificulty(Easy, Meedium, Hard)", "EASY")
	var modeM=prompt("Select playmode(BATTLE ROYALE,DUEL)","DUEL")
	console.log(difM);
	console.log(modeM);
	let evento = new Object();
	evento.event = 'JOIN MATCHMAKING'
		evento.diff=difM;
		evento.mode = modeM;
		game.global.socket.send(JSON.stringify(evento))
		console.log("Joining matchmaking")
	game.state.start('roomState')
	
}

function createList(roomlist) {
	var ul = document.getElementById("LUL");
	$(ul).empty();
	mostrar(true);
	//console.log(roomlist.length)
	for (i = 0; i < roomlist.length; i++) { 
		var room = roomlist[i]
		//console.log(room)
		//console.log(room.roomName)
		var li = document.createElement("li");
		var a = document.createElement("a");
	
		a.appendChild(document.createTextNode(room.roomName + " - " + room.numberOfPlayers + "/" + room.maxPlayers + " jugadores"));
		//if room.started
		a.addEventListener("click", function() {
			join(room.roomName);
		});
		li.appendChild(a);
	
		ul.appendChild(li);
	}
}
function join(roomName){
	mostrar(false);
	let evento = new Object();
	evento.event = 'JOIN ROOM'
	evento.roomName = roomName;
	console.log("Sending join room petition to server for: " + roomName)
	game.global.socket.send(JSON.stringify(evento))
}

function mostrar(modo){
	var uList = document.getElementsByClassName("list");
	if (modo == true){
		uList[0].hidden = false;
	} else {
		uList[0].hidden = true;
	}
	
}

Spacewar.lobbyState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **LOBBY** state");
		}
	},

	preload : function() {

	},

	create : function() {
		
		var uList = document.getElementsByClassName("list");
		uList[0].hidden = false;
		
		var menubackground = game.add.sprite(game.world.X, game.world.Y, "lobby");
		menubackground.height = game.height;
		menubackground.width = game.width;
		//creamos el boton del matchmaking y le damos posicion
		 var matchmaking = game.add.sprite(
			      game.world.centerX -250,
			      game.world.centerY + 150,
			      "matchmakingButt"
			    );
		 //le ponemos el input a true y le asignamos el evento de matchmaking 
		 //al hacer click
		 matchmaking.inputEnabled = true;
		 matchmaking.events.onInputDown.add(selecMatchmaking,this);
		
		 var roomCreate = game.add.sprite(
			      game.world.centerX +55,
			      game.world.centerY + 150,
			      "createroom"
			    );
		 roomCreate.inputEnabled = true;
		 roomCreate.events.onInputDown.add(crearSala,this);
		 
			
		
	},

	update : function() {
			
		}
	}
