Spacewar.endState = function(game) {}
var puntuacion;
var replay= false;
function newGame(){
	replay=true;
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
		var replayButton;
		
		//falta crear el botón
		playbutton.inputEnabled = true;
	    playbutton.events.onInputDown.add(newGame, this);
	},

	update : function() {
		if (replay == true){
			game.state.start('menuState')
		}
	}
}