Spacewar.roomState = function(game) {
}
//aqui no se por que hay un paso intermedio pero deberia ir a 
//game al recibir un mensaje
function toGame(){
	
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
		 
	},

	update : function() {
		
	}
}