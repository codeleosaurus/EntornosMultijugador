window.onload = function() {
	
	//var name = prompt("Please enter your name", "Shrek");

	var config = {
		    width: 1024,
		    height: 600,
		    type: Phaser.AUTO,
		    parent: 'gameDiv',
		    
		};
	game = new Phaser.Game(config)
	// GLOBAL VARIABLES
	game.global = {
		FPS : 30,
		DEBUG_MODE : false,
		socket : null,
		myPlayer : new Object(),
		otherPlayers : [],
		projectiles : []
	}
	// PHASER SCENE CONFIGURATOR
	
	game.state.add('bootState', Spacewar.bootState)
	game.state.add('preloadState', Spacewar.preloadState)
	game.state.add('menuState', Spacewar.menuState)
	game.state.add('lobbyState', Spacewar.lobbyState)
	game.state.add('matchmakingState', Spacewar.matchmakingState)
	game.state.add('roomState', Spacewar.roomState)
	game.state.add('gameState', Spacewar.gameState)
	game.state.add('endState', Spacewar.ending)
	//game.state.start('bootState')
	
}

	///////////////////////////////////////////////////////////////
	////////    LA FUNCION CUANDO SE ABRE EL CALCETIN    /////////
	//////////////////////////////////////////////////////////////
	function openWebsocket() {
	/*console.log(name);
	if (name == "" | name.length < 4 | name.length > 20) {
		console.log("[ERROR] Player name is too short or too long!")
		return;
		
	}
	console.log(name);
	var splChars = "*|,\":<>[]{}`'^´;()@&$#% €_-+*/
	/*for (i = 0; i < name.length; i++) {
		if (splChars.indexOf(name.charAt(i)) != -1) {
			// Caracteres no permitidos en el string!
			console.log("[ERROR] Invalid characters in player name!")
			return;
		}*/
		game.global.socket = new WebSocket("ws://127.0.0.1:8080/spacewar" + name)
		game.global.socket.onopen = () => {
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] WebSocket connection opened.')
			}
		}
		calcetinDeWeb();
	}
	
	
	////////////////////////////////////////////////////////////////
	/////////         WEBSOCKET CONFIGURATOR     //////////////////
	///////////////////////////////////////////////////////////////
	//aqui hay que ir metiendo los mensajes que vamos enviando del server y el comportamiento que queremos
	//tambien debo poner una funcion para que una vez introducido el nombre lo gestione y te cambie de
	//estado
	//anadir un fonfo chulo y conseguir que el coso de meter el nombre reciba un nombre y lo envie
	function calcetinDeWeb(){
	

	game.global.socket.onclose = () => {
		if (game.global.DEBUG_MODE) {
			console.log('[DEBUG] WebSocket connection closed.')
		}
	}
	
	game.global.socket.onmessage = (message) => {
		var msg = JSON.parse(message.data)
		
			switch (msg.event) {
			case 'CONFIRMATION':
				handleConfirmation(msg);
				break
			case 'JOIN':
				if (game.global.DEBUG_MODE) {
					console.log('[DEBUG] JOIN message received')
					console.dir(msg)
					console.log(name);
				}
				game.global.myPlayer.id = msg.id
				game.global.myPlayer.shipType = msg.shipType
				var i = msg.id;
				if (game.global.DEBUG_MODE) {
					console.log('[DEBUG] ID assigned to player: ' + game.global.myPlayer.id)
				}
				break
			break
			case 'NEW ROOM' :
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] NEW ROOM message recieved')
				console.dir(msg)
			}
			game.global.myPlayer.room = {
					name : msg.room
			}
			break
			case 'GAME STATE UPDATE' :
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] GAME STATE UPDATE message recieved')
				console.dir(msg)
			}
			if (typeof game.global.myPlayer.image !== 'undefined') {
				for (var player of msg.players) {
					//if (game.global.myPlayer.name.equals(player.name)) {
					if (game.global.myPlayer.id == player.id) {
						game.global.myPlayer.image.x = player.posX
						game.global.myPlayer.image.y = player.posY
						game.global.myPlayer.image.angle = player.facingAngle
					} else {
						if (typeof game.global.otherPlayers[player.id] == 'undefined') {
							game.global.otherPlayers[player.id] = {
									image : game.add.sprite(player.posX, player.posY, 'spacewar', player.shipType)
							}
							game.global.otherPlayers[player.id].image.anchor.setTo(0.5, 0.5)
						} else {
							game.global.otherPlayers[player.id].image.x = player.posX
							game.global.otherPlayers[player.id].image.y = player.posY
							game.global.otherPlayers[player.id].image.angle = player.facingAngle
						}
					}
				}
				
				for (var projectile of msg.projectiles) {
					if (projectile.isAlive) {
						game.global.projectiles[projectile.id].image.x = projectile.posX
						game.global.projectiles[projectile.id].image.y = projectile.posY
						if (game.global.projectiles[projectile.id].image.visible === false) {
							game.global.projectiles[projectile.id].image.angle = projectile.facingAngle
							game.global.projectiles[projectile.id].image.visible = true
						}
					} else {
						if (projectile.isHit) {
							// we load explosion
							let explosion = game.add.sprite(projectile.posX, projectile.posY, 'explosion')
							explosion.animations.add('explosion')
							explosion.anchor.setTo(0.5, 0.5)
							explosion.scale.setTo(2, 2)
							explosion.animations.play('explosion', 15, false, true)
						}
						game.global.projectiles[projectile.id].image.visible = false
					}
				}
			}
			break
			case 'REMOVE PLAYER' :
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] REMOVE PLAYER message recieved')
				console.dir(msg.players)
			}
			game.global.otherPlayers[msg.id].image.destroy()
			delete game.global.otherPlayers[msg.id]
			default :
			console.dir(msg)
			break
		}
	}
	/*function handleConfirmation(confirm) {
		switch (confirm.type) {
			case 'CORRECT NAME':
				game.state.start('bootState');
				break
			case 'JOIN MATCHMAKING':
				game.state.start('matchmakingState');
				break
			case 'LEAVE MATCHMAKING':
				game.state.start('menuState');
				break
			default:
				console.log("[CONFIRM] Unknown confirmation received, type: " + confirm.type);
				break
		}
	
	}*/
};