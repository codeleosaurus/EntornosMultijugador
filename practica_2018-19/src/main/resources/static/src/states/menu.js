Spacewar.menuState = function(game) {}
	 
	clicked = false;
	function listener(){
		clicked = true;
		}
	


Spacewar.menuState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **MENU** state");
		}
	},

	preload : function() {
		// In case JOIN message from server failed, we force it
		if (typeof game.global.myPlayer.id == 'undefined') {
			if (game.global.DEBUG_MODE) {
				console.log("[DEBUG] Forcing joining server...");
			}
			let message = {
				event : 'JOIN'
			}
			game.global.socket.send(JSON.stringify(message))
		}
	},

	create : function() {
		var menubackground = game.add.sprite(game.world.X, game.world.Y, "menu");
		menubackground.height = game.height;
		menubackground.width = game.width;
		//Aqui meto el botón de que le de a jugar
		 var playbutton = game.add.sprite(
			      game.world.centerX,
			      game.world.centerY + 100,
			      "play"
			    );
		 playbutton.inputEnabled = true;
		 playbutton.events.onInputDown.add(listener, this);

	},
		//ahora pasaría solo cuando demos click al boton
	update : function() {
		 if (clicked && typeof game.global.myPlayer.id !== 'undefined') {
		      clicked = false;
		      game.state.start('lobbyState')
		}
	}
}