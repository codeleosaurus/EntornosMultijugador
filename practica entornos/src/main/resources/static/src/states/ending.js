Spacewar.endState = function(game) {}
var puntuacion;
function newGame(){
	let evento = new Object();
	evento.event = 'LEAVE ROOM'
		//evento.event = 'LEAVE LOBBY'
		game.global.socket.send(JSON.stringify(evento))
	game.state.start('lobbyState')
}

Spacewar.endState.prototype = {

	init : function() {
		
	},

	preload : function() {

	},

	create : function() {
		mostrar(false);
		var menubackground = game.add.sprite(game.world.X, game.world.Y, "ending");
		menubackground.height = game.height;
		menubackground.width = game.width;
		
		var replayButton= game.add.sprite(
			      game.world.centerX-100,
			      game.world.centerY +120,
			      "replay"
			    );
		replayButton.inputEnabled = true;
		replayButton.events.onInputDown.add(newGame, this);
	},

	update : function() {
		
	}
}
function showResults(msg){
	var puntuaciones = ("El ganador es " + msg.winner.playerName + " con " + msg.winner.points + " puntos y " + msg.winner.hp + " vidas")
	for ( var player of msg.losers){
		puntuaciones += ("\n Un perdedor es " + player.playerName + " con " + player.points + " puntos" )
	}
	alert(puntuaciones)
}