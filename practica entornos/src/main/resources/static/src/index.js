window.onload = function() {

	game = new Phaser.Game(1024, 600, Phaser.AUTO, 'gameDiv')
	var name= prompt("Write your name", "shrek")
	// GLOBAL VARIABLES
	game.global = {
		FPS : 30,
		DEBUG_MODE : false,
		socket : null,
		myPlayer : new Object(),
		otherPlayers : [],
		projectiles : [],
		rooms : [],
		playersGame : [],
		currentRoom : null,
		waiting: false
	}

	// WEBSOCKET CONFIGURATOR

	game.global.socket = new WebSocket("ws://127.0.0.1:8080/spacewar/"+ name)
	//game.global.socket = new WebSocket("ws://192.168.1.37:8080/spacewar/"+ name)
	
	game.global.socket.onopen = () => {
		if (game.global.DEBUG_MODE) {
			console.log('[DEBUG] WebSocket connection opened.')
		}
	}

	game.global.socket.onclose = () => {
		if (game.global.DEBUG_MODE) {
			console.log('[DEBUG] WebSocket connection closed.')
		}
	}
	
	game.global.socket.onmessage = (message) => {
		var msg = JSON.parse(message.data)
		
		switch (msg.event) {
		case 'INIT SESSION':
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] [MSG INFO] INIT SESSION message recieved')
				console.dir(msg)
			}

			nameConfirmation(msg.validname)
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
		
			
		case 'JOINING GAME':
			game.state.start('gameState')
			break
		case 'GAME END':
			game.state.clearCurrentState()
			game.global.myPlayer = new Object()
			game.global.otherPlayers = []
			game.state.start('endState')
			showResults(msg)
			break
			
		case 'ROOM LIST':
			createList(msg.roomList)
			break
			
		case 'JOINING ROOM':
			//game.global.currentRoom.roomName = msg.roomName;
			console.log("Joining room:" + msg.roomName)
			game.state.start('roomState')
			break
			
		case 'ROOM INFO':
			console.log(msg)
			//game.global.currentRoom.roomName = msg.roomName;
			//game.global.currentRoom.playerlist = msg.playerlist;
			//updateRoom();
			
		case 'CHAT MSG':
			//displayChatMsg(msg.text, msg.playerName);
			break
		
		case 'WAITING ROOM':
			console.log("waiting for " + msg.roomName);
			game.global.waiting = true;
			game.state.start("matchmakingState")
			break
		
		case 'LEAVE WAITING':
			console.log("leaving queue")
			game.global.waiting = false;
			game.state.start("lobbyState")
			break
			
		case 'RANKING':
			console.log("recibido ranking")
			showRanking(msg.rankingList)
			break
			
		case 'JOINING MATCHMAKING':
			console.log("joining matchmaking")			
			game.state.start("matchmakingState")
			break
			
		case 'LEAVING MATCHMAKING':
			console.log("leaving matchmaking")
			game.state.start("lobbyState")
			break
			
		case 'JOINING ROOM ERROR':
			console.log("error at joining room")
			game.state.start("lobbyState")
			break
			
		case 'DELETING ROOM':
			console.log("Deleting room: " + msg.roomName)
			break
			
		case 'PLAYER LEAVING ROOM':
			console.log("Player " + msg.id + " leaves room")
                /*var pos = -1;
                for (i = 0; i < game.global.currentRoom.playerList.length; i++) {
                    if (game.global.currentRoom.playerList[i] == msg.playerName) {
                        pos = i;
                    }
                }
                if (pos >= 0) game.global.currentRoom.playerList.remove(pos);
                updateSalaInfo();*/
			break
			
		case 'PLAYER LEAVING GAME':
			console.log("Player " + msg.id + " leaves game")
			game.global.otherPlayers[msg.id].image.destroy()
            //game.global.UIPlayerName[player.id].destroy();
            //game.global.UIText[player.id].destroy();
            delete game.global.otherPlayers[msg.id]
			break
			
		case 'PLAYER LIST':
			console.log("Recibiendo lista de jugadores")
			playersGame = msg.playersInGame
			break
			
			
		case 'GAME STATE UPDATE' :
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] GAME STATE UPDATE message recieved')
				console.dir(msg)
			}
			if (typeof game.global.myPlayer.image !== 'undefined') {
				for (var player of msg.players) {
	
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

	// PHASER SCENE CONFIGURATOR
	game.state.add('bootState', Spacewar.bootState)
	game.state.add('preloadState', Spacewar.preloadState)
	game.state.add('menuState', Spacewar.menuState)
	game.state.add('lobbyState', Spacewar.lobbyState)
	game.state.add('matchmakingState', Spacewar.matchmakingState)
	game.state.add('roomState', Spacewar.roomState)
	game.state.add('gameState', Spacewar.gameState)
	game.state.add('endState', Spacewar.endState)
	//game.state.start('bootState')
	
}
function nameConfirmation(validname) {
	
	if (validname){
		console.log("nombre aprobado");
		game.state.start('bootState');
	}
	else{
		if (game.global.DEBUG_MODE) {
		console.log("[DEBUG] [ERROR] Confirmation name error. Name was not valid");
		}
		askName();
	}
}

function askName(){
	name= prompt("Write your name", "shrek")
	let evento = new Object();
			evento.event = 'INIT SESSION'
			evento.playerName = name;
			console.log("Name retry, sending message to server")
			game.global.socket.send(JSON.stringify(evento))
	
}

function showRanking(rankingList){
	var ranking = "";
	var puesto = 1;
	for (var player of rankingList){
		ranking += (puesto + "º: " + player.playerName + " -> " + player.score + " puntos" + "\n");
		puesto++;
	}
	alert(ranking);
}

