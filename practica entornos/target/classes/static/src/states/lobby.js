Spacewar.lobbyState = function(game) {}
	//esta variable hace que cuando selecciones la sala en la que vas a entrar se ponga a true
selected=false;
clickR=false;
clickM=false;

function crearSala(){
clickR = true;		
}
function matchmaking(){
	clickM = true;		
	}
function crearSala(){
	if(clickR==true){
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
		clickR=false;
		msg.event = 'CREATE ROOM'
			msg.roomName = roomName;
			msg.roomDiff=dif;
			msg.roomType = roomType;
			console.log("Mensaje pa tu body ")
			game.global.socket.send(JSON.stringify(msg))
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
		
		var menubackground = game.add.sprite(game.world.X, game.world.Y, "lobby");
		menubackground.height = game.height;
		menubackground.width = game.width;
		
		 var roomButt = game.add.sprite(
			      game.world.centerX -400,
			      game.world.centerY + 150,
			      "room"
			    );
		 roomButt.inputEnabled = true;
		 //playbutton.events.onInputDown.add(crearSala, this);
		 var roomCreate = game.add.sprite(
			      game.world.centerX -35,
			      game.world.centerY + 150,
			      "createroom"
			    );
		 roomButt.inputEnabled = true;
		 roomButt.events.onInputDown.add(crearSala,this);
		 
			
		
	},

	update : function() {
			
		}
	
		
		
		//jugador, roomtipe(string), string dificultad(roomdis)easymeadiumhard,roomname
		//nombre de la sala dificultad y modo
		//if (selected == true){
			//game.state.start('matchmakingState')
		//}
	}
