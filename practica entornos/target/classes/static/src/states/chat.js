Spacewar.chatState = function(game) {}

	


Spacewar.chatState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **MENU** state");
		}
	},

	preload : function() {
		// In case JOIN message from server failed, we force it
	
	},

	create : function() {
		//mostrar(false);
		var menubackground = game.add.sprite(game.world.X, game.world.Y, "menu");
		menubackground.height = game.height;
		menubackground.width = game.width;
		
		//Aqui meto el botón de que le de a jugar
		 var backButton = game.add.sprite(
			      game.world.centerX,
			      game.world.centerY - 75,
			      "volver"
			    );
		 playbutton.inputEnabled = true;
		 playbutton.events.onInputDown.add(back, this);
		 
		 var sendMessage = game.add.sprite(
			      game.world.centerX,
			      game.world.centerY + 75,
			      "Mensajear"
			    );
		 rank.inputEnabled = true;
		 rank.events.onInputDown.add(send, this);

	},
	update : function() {
		
	}
}

function send(){
	var mensaje = prompt("Mensaje que enviar", "Mensaje")
	let evento = new Object();
	evento.event = 'CHAT MESSAGE';
	evento.text = mensaje;
			//event : "JOIN LOBBY"
		console.log("Enviando mensaje");
		game.global.socket.send(JSON.stringify(evento));
}

function back(){
	mostrar2(false);
	gamet.state.start("menuState");
}

function mostrar2(){
	var uList3 = document.getElementsByClassName("list3");
	if (modo == true){
		uList3[0].hidden = false;
	} else {
		//UList[0].visible = false;
		uList3[0].hidden = true;
	}
}