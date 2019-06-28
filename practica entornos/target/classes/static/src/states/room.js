Spacewar.roomState = function(game) {
}
//aqui no se por que hay un paso intermedio pero deberia ir a 
//game al recibir un mensaje
function toGame{
	game.state.start('gameState')
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

	},

	update : function() {
		
	}
}