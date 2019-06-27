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
		var menubackground = game.add.sprite(game.world.X, game.world.Y, "lobby");
		menubackground.height = game.height;
		menubackground.width = game.width;
		
		 var roomButt = game.add.sprite(
			      game.world.centerX -80,
			      game.world.centerY + 150,
			      "room"
			    );
		 roomButt.inputEnabled = true;
		 //playbutton.events.onInputDown.add(crearSala, this);
		 
		 
		 

		
	},

	update : function() {
		//if (selected == true){
			game.state.start('matchmakingState')
		//}
	}
}