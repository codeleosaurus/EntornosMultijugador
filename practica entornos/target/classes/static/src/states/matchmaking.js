Spacewar.matchmakingState = function(game) {}
function leaveMatchmaking(){
	game.state.start('lobbyState')
}
//al recibir un mensaje del servidor de que estan todos los jugadores
//deberia pasar a room
function toRoom{
	if (typeof game.global.myPlayer.room !== 'undefined') {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Joined room " + game.global.myPlayer.room);
		}
		game.state.start('roomState')
	}
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
		 var nope=game.add.sprite(
				 game.world.centerX+400,
				 game.world.centerY -260,
				 "nope"
				 );
		 nope.inputEnabled = true;
		 nope.events.onInputDown.add( leaveMatchmaking,this);
	
	},
//hay que cambiar el update por un mensaje del server pero no estoy seguro de como
	update : function() {	
	}
}