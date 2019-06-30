Spacewar.chatState = function(game) {}

	


Spacewar.chatState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **CHAT** state");
		}
	},

	preload : function() {
		// In case JOIN message from server failed, we force it
	
	},

	create : function() {
		//mostrar(false);
		var chatbackground = game.add.sprite(game.world.X, game.world.Y, "chatFondo");
		chatbackground.height = game.height;
		chatbackground.width = game.width;
		
		//Aqui meto el botón de que le de a jugar
		 var backButton = game.add.sprite(
			      game.world.centerX + 300,
			      game.world.centerY - 75,
			      "volver"
			    );
		 backButton.inputEnabled = true;
		 backButton.events.onInputDown.add(back, this);
		 
		 var sendMessage = game.add.sprite(
			      game.world.centerX + 300,
			      game.world.centerY + 75,
			      "mensajear"
			    );
		 sendMessage.inputEnabled = true;
		 sendMessage.events.onInputDown.add(send, this);

	},
	update : function() {
		
	}
}

function send(){
	var mensaje = prompt("Mensaje que enviar", "")
	let evento = new Object();
	evento.event = 'CHAT MESSAGE';
	evento.text = mensaje;
			//event : "JOIN LOBBY"
		console.log("Enviando mensaje");
		game.global.socket.send(JSON.stringify(evento));
}

function back(){
	mostrar2(false);
	game.state.start("menuState");
}

function mostrar2(modo){
	var uList3 = document.getElementsByClassName("list3");
	if (modo == true){
		uList3[0].hidden = false;
	} else {
		//UList[0].visible = false;
		uList3[0].hidden = true;
	}
}