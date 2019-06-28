Spacewar.lobbyState = function(game) {}
	//esta variable hace que cuando selecciones la sala en la que vas a entrar se ponga a true
function onSelect(){
	
}

function matchmaking(){
	clickM = true;		
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
	console.log("Bernarda");
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
		
		 var matchmaking = game.add.sprite(
			      game.world.centerX -250,
			      game.world.centerY + 150,
			      "matchmakingButt"
			    );
		 matchmaking.inputEnabled = true;
		 matchmaking.events.onInputDown.add(selecMatchmaking,this);
		 //playbutton.events.onInputDown.add(crearSala, this);
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
	
		
		
		//jugador, roomtipe(string), string dificultad(roomdis)easymeadiumhard,roomname
		//nombre de la sala dificultad y modo
		//if (selected == true){
			//game.state.start('matchmakingState')
		//}
	}
