Spacewar.gameState = function(game) {
	this.bulletTime
	this.fireBullet
	this.numStars = 100 // Should be canvas size dependant
	this.maxProjectiles = 800 // 8 per player
}
function actualizarVida(){
	//cada vez que le llega un mensaje comprueba la vida que viene del servidor y hace una regla de 3 para calcular
	//el porcentaje que se encoge la barra
	//(vidaActual*barraVida)/vidaTotal
	
	//if (vida==0){
	//game.state.start('endState')
	//}
}
function muerte(){
	
}


Spacewar.gameState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **GAME** state");
		}
	},

	preload : function() {
		// We create a procedural starfield background
		for (var i = 0; i < this.numStars; i++) {
			let sprite = game.add.sprite(game.world.randomX,
					game.world.randomY, 'spacewar', 'staralpha.png');
			let random = game.rnd.realInRange(0, 0.6);
			sprite.scale.setTo(random, random)
		}

		// We preload the bullets pool
		game.global.proyectiles = new Array(this.maxProjectiles)
		for (var i = 0; i < this.maxProjectiles; i++) {
			game.global.projectiles[i] = {
				image : game.add.sprite(0, 0, 'spacewar', 'projectile.png')
			}
			game.global.projectiles[i].image.anchor.setTo(0.5, 0.5)
			game.global.projectiles[i].image.visible = false
		}

		// we load a random ship
		let random = [ 'blue', 'darkgrey', 'green', 'metalic', 'orange',
				'purple', 'red' ]
		let randomImage = random[Math.floor(Math.random() * random.length)]
				+ '_0' + (Math.floor(Math.random() * 6) + 1) + '.png'
		game.global.myPlayer.image = game.add.sprite(200, 200, 'spacewar',
				game.global.myPlayer.shipType)
		game.global.myPlayer.image.anchor.setTo(0.5, 0.5)
		console.log(game.global.myPlayer)
	},

	create : function() {
		/*
		//pasarle el nombre del jugador desde el server
		//el texto que va a seguir a la nave se crea aqui
		text = game.add.bitmapText(0, 0,name,style);
		text.x = Math.floor(randomImage.x + sprite.width / 2);
	    text.y = Math.floor(randomImage.y + sprite.height / 2);
		*/
		this.bulletTime = 0
		this.fireBullet = function() {
			if (game.time.now > this.bulletTime) {
				this.bulletTime = game.time.now + 250;
				// this.weapon.fire()
				return true
			} else {
				return false
			}
		}

		this.wKey = game.input.keyboard.addKey(Phaser.Keyboard.W);
		this.sKey = game.input.keyboard.addKey(Phaser.Keyboard.S);
		this.aKey = game.input.keyboard.addKey(Phaser.Keyboard.A);
		this.dKey = game.input.keyboard.addKey(Phaser.Keyboard.D);
		this.spaceKey = game.input.keyboard.addKey(Phaser.Keyboard.SPACEBAR);

		// Stop the following keys from propagating up to the browser
		game.input.keyboard.addKeyCapture([ Phaser.Keyboard.W,
				Phaser.Keyboard.S, Phaser.Keyboard.A, Phaser.Keyboard.D,
				Phaser.Keyboard.SPACEBAR ]);

		game.camera.follow(game.global.myPlayer.image);
		
		var vidas=game.add.sprite(
				 game.world.centerX-400,
				 game.world.centerY -260,
				 "vida"
				 );
	},
	//aqui actualiza lo de la posicion es donde llega el mensaje que se hace en el servidor
	update : function() {
		let evento = new Object()
		evento.event = 'UPDATE MOVEMENT'

			evento.movement = {
			thrust : false,
			brake : false,
			rotLeft : false,
			rotRight : false
		}

		evento.bullet = false

		if (this.wKey.isDown)
			evento.movement.thrust = true;
		if (this.sKey.isDown)
			evento.movement.brake = true;
		if (this.aKey.isDown)
			evento.movement.rotLeft = true;
		if (this.dKey.isDown)
			evento.movement.rotRight = true;
		if (this.spaceKey.isDown) {
			evento.bullet = this.fireBullet()
		}
		
		
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Sending UPDATE MOVEMENT message to server")
		}
			game.global.socket.send(JSON.stringify(evento))
	}
}