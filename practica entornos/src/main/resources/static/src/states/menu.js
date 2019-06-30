Spacewar.menuState = function(game) {}

	function play(){
		//if (typeof game.global.myPlayer.id !== 'undefined') {
		//	if (typeof game.global.myPlayer.id != 'undefined') {
				let evento = new Object();
				evento.event = 'JOIN LOBBY';
						//event : "JOIN LOBBY"
					console.log(evento);
					game.global.socket.send(JSON.stringify(evento));
					game.state.start('lobbyState');
					if (game.global.DEBUG_MODE) {}
				}
	


Spacewar.menuState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **MENU** state");
		}
	},

	preload : function() {
		// In case JOIN message from server failed, we force it
	
	},

	create : function() {
		
		var menubackground = game.add.sprite(game.world.X, game.world.Y, "menu");
		menubackground.height = game.height;
		menubackground.width = game.width;
		
		//Aqui meto el botón de que le de a jugar
		 var playbutton = game.add.sprite(
			      game.world.centerX,
			      game.world.centerY - 75,
			      "play"
			    );
		 playbutton.inputEnabled = true;
		 playbutton.events.onInputDown.add(play, this);
		 
		 var rank = game.add.sprite(
			      game.world.centerX,
			      game.world.centerY + 75,
			      "rankingBot"
			    );
		 rank.inputEnabled = true;
		 rank.events.onInputDown.add(ranking, this);

	},
	update : function() {
		
	}
}

function ranking(){
	let evento = new Object();
	evento.event = 'GET RANKING';
			//event : "JOIN LOBBY"
		console.log("asking for ranking");
		game.global.socket.send(JSON.stringify(evento));
}