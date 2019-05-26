Spacewar.matchmakingState = function(game) {

}

Spacewar.matchmakingState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **MATCH-MAKING** state");
		}
	},

	preload : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Joining room...");
		}
		let message = {
			event : 'JOIN ROOM'
		}
		game.global.socket.send(JSON.stringify(message))
	},

	create : function() {
		
		var menubackground = game.add.sprite(game.world.X, game.world.Y, "matchmaking");
		menubackground.height = game.height;
		menubackground.width = game.width;
		//seria conveniente meter alguna flecha de estas girando que indica que se esta cargando pero eso es decorativo
		//tampoco estaría mal un timeout por si llevas esperando conectarte a la sala demasiado tiempo
	},

	update : function() {
		if (typeof game.global.myPlayer.room !== 'undefined') {
			if (game.global.DEBUG_MODE) {
				console.log("[DEBUG] Joined room " + game.global.myPlayer.room);
			}
			game.state.start('roomState')
		}
	}
}