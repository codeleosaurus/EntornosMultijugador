Spacewar.lobbyState = function(game) {}
	//esta variable hace que cuando selecciones la sala en la que vas a entrar se ponga a true
selected=false;
function crearSala(){
	
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
		
		
	},

	update : function() {
		if (selected == true){
			game.state.start('matchmakingState')
		}
	}
}