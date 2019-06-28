Spacewar.endState = function(game) {}
var puntuacion;
function newGame(){
	game.state.start('menuState')
}

Spacewar.lobbyState.prototype = {

	init : function() {
		
	},

	preload : function() {

	},

	create : function() {
		
		var menubackground = game.add.sprite(game.world.X, game.world.Y, "ending");
		menubackground.height = game.height;
		menubackground.width = game.width;
		
		var replayButton= game.add.sprite(
			      game.world.centerX,
			      game.world.centerY + 100,
			      "replay"
			    );
		replayButton.inputEnabled = true;
		replayButton.events.onInputDown.add(newGame, this);
	},

	update : function() {
		
	}
}