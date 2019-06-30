Spacewar.lobbyState = function(game) {}
//funciones que se llaman al pulsar los botones
function crearSala(){
		mostrar(false);
		var roomName= prompt("Write the rooms name", "Villa oculta de la hoja")
		var dif= prompt("write dificulty(EASY, MEDIUM, HARD)", "EASY")
		var mode=prompt("Select playmode(BATTLE ROYALE,DUEL)","DUEL")
		let evento = new Object();
			evento.event = 'CREATE ROOM'
			evento.roomName = roomName;
			evento.roomDiff=dif;
			evento.roomType = mode;
			console.log("Room created, sending message to server")
			game.global.socket.send(JSON.stringify(evento))
			//game.state.start('roomState')
	
}
function selecMatchmaking(){
	mostrar(false);
	var difM= prompt("write dificulty(Easy, Meedium, Hard)", "EASY")
	var modeM=prompt("Select playmode(BATTLE ROYALE,DUEL)","DUEL")
	console.log(difM);
	console.log(modeM);
	game.global.waiting = false;
	let evento = new Object();
	evento.event = 'JOIN MATCHMAKING'
		evento.diff=difM;
		evento.mode = modeM;
		game.global.socket.send(JSON.stringify(evento))
		console.log("Joining matchmaking")
	
	//game.state.start('matchmakingState')
	
}

function leave(){
	
	let evento = new Object();
	evento.event = 'LEAVE LOBBY'
		game.global.socket.send(JSON.stringify(evento))
		console.log("Leaving lobby, message sent to server")
	game.state.start('menuState')
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
	var uList2 = document.getElementsByClassName("list2");
	if (modo == true){
		uList[0].hidden = false;
		uList2[0].hidden = false;
	} else {
		//UList[0].visible = false;
		uList[0].hidden = true;
		uList2[0].hidden = true;
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
		
		console.log("hola")
		var menubackground = game.add.sprite(game.world.X, game.world.Y, "lobby");
		menubackground.height = game.height;
		menubackground.width = game.width;
		//creamos el boton del matchmaking y le damos posicion
		 var matchmaking = game.add.sprite(
			      game.world.centerX -350,
			      game.world.centerY + 137,
			      "matchmakingButt"
			    );
		 //le ponemos el input a true y le asignamos el evento de matchmaking 
		 //al hacer click
		 matchmaking.inputEnabled = true;
		 matchmaking.events.onInputDown.add(selecMatchmaking,this);
		
		 var roomCreate = game.add.sprite(
			      game.world.centerX -100,
			      game.world.centerY + 137,
			      "createroom"
			    );
		 roomCreate.inputEnabled = true;
		 roomCreate.events.onInputDown.add(crearSala,this);
		 
		 var leaveLobby = game.add.sprite(
			      game.world.centerX +150,
			      game.world.centerY + 137,
			      "leaveLobby"
			    );
		 leaveLobby.inputEnabled = true;
		 leaveLobby.events.onInputDown.add(leave,this);
		 
		 var roomList=game.add.sprite(
				 game.world.centerX -350,
			      game.world.centerY - 210,
			      "roomTex"
				 );
		 roomList.scale.setTo(0.7,0.7);
		 var playerList=game.add.sprite(
				 game.world.centerX +150,
			      game.world.centerY - 210,
			      "playerTex"
				 );
		 playerList.scale.setTo(0.7,0.7);
			
		 mostrar(true);
	},

	update : function() {
			
		}
}
