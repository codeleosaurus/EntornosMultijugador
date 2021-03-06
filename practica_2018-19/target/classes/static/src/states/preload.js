Spacewar.preloadState = function(game) {

}

Spacewar.preloadState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **PRELOAD** state");
		}
	},

	preload : function() {
		//fondos de la interfaz y botones 
		game.load.image("menu", "/assets/images/menuFondo.png");
		game.load.image("play", "assets/images/play.png");
		game.load.image("room", "assets/images/crearSala.png");
		game.load.image("lobby", "assets/images/lobbyFondo.png");
		game.load.image("matchmaking", "assets/images/matchmakingFondo.png");
		game.load.image("ending", "assets/images/endingFondo.png");
		game.load.image("replay", "assets/images/replay.png");
		//explosiones y naves
		game.load.atlas('spacewar', 'assets/atlas/spacewar.png',
				'assets/atlas/spacewar.json',
				Phaser.Loader.TEXTURE_ATLAS_JSON_HASH)
		game.load.atlas('explosion', 'assets/atlas/explosion.png',
				'assets/atlas/explosion.json',
				Phaser.Loader.TEXTURE_ATLAS_JSON_HASH)
				
	},

	create : function() {
		game.state.start('menuState')
	},

	update : function() {

	}
}