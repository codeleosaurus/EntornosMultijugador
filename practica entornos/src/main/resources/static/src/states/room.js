Spacewar.roomState = function(game) {
}
//aqui no se por que hay un paso intermedio pero deberia ir a 
//game al recibir un mensaje
function toGame(){
	let evento = new Object();
	evento.event= 'START GAME MANUALLY'
		game.global.socket.send(JSON.stringify(evento))
		game.state.start('gameState')
	
}
function leaveRoom(){
	let evento = new Object();
	evento.event = 'LEAVE ROOM'
	game.global.socket.send(JSON.stringify(evento))
	game.state.start('lobbyState')
	
}

Spacewar.roomState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **ROOM** state");
		}
	},

	preload : function() {

	},

	create : function() {
		var background = game.add.sprite(game.world.X, game.world.Y, "menu");
		background.height = game.height;
		background.width = game.width;
		 var leave = game.add.sprite(
			      game.world.centerX +150,
			      game.world.centerY + 150,
			      "leaveRoom"
			    );
		 leave.inputEnabled = true;
		 leave.events.onInputDown.add(leaveRoom,this);
		 var play = game.add.sprite(
			      game.world.centerX -150,
			      game.world.centerY + 150,
			      "play"
			    );
		 play.inputEnabled = true;
		 play.events.onInputDown.add(toGame,this);
	},

	update : function() {
		
	}
}