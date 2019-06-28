Spacewar.lobbyState = function(game) {}
//funciones que se llaman al pulsar los botones
function onSelect(){
	//aqui hay que hacer una funcion que haga que al seleccionar una sala y darle a confirmar llame a join room
	//let evento = new Object();
	//evento.event = 'JOIN ROOM'
		//evento.roomName = selRoom;
	//de momento esta comentada porque no tenemos manera de seleccionar las salas disponibles 
}
function crearSala(){
		var roomName= prompt("Write the rooms name", "Villa oculta de la hoja")
		var dif= prompt("write dificulty(EASY, MEDIUM, HARD)", "EASY")
		console.log(dif);
		if(dif=="EASY"||dif=="MEDIUM"||dif=="HARD"){
		console.log("se envia mensaje al servidor con la dificultad y toda la mierda")
		}else{
			while(dif!="EASY"||dif!="MEDIUM"||dif!="HARD"){
				dif=prompt("please select a valid dificulty","EASY")
				break
			}
		}
		var mode=prompt("Select playmode(BATTLE ROYALE,DUEL)","BATTLE ROYALE")
		console.log(dif);
		let evento = new Object();
			evento.event = 'CREATE ROOM'
			evento.roomName = roomName;
			evento.roomDiff=dif;
			evento.roomType = mode;
			console.log("Room created, sending message to server")
			game.global.socket.send(JSON.stringify(evento))
	
}
function selecMatchmaking(){
	let evento = new Object();
	evento.event = 'JOIN MATCHMAKING'
	game.state.start('matchmakingState')
	
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
