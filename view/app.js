$(function() {

	var FIREBALL = "fireball";
	var WIZARD = "wizard";
	var RANGER = "ranger";
	var UPDATE = "update";
	var PING = "ping";
	var WELCOME = "welcome";
	var PLAYER_UPDATE = "player_update";
	var ATTACK = "attack";
    var userid = 0;
    var MAX_SPEED = 6000;
    var FRICTION = 0.97;
    var upArrow = 38;
    var downArrow = 40;
    var rightArrow = 39;
    var leftArrow = 37;
    var upKey = 87;
    var downKey = 83;
    var rightKey = 68;
    var leftKey = 65;
    var STAGE_WIDTH = 1000;
    var STAGE_HEIGHT = 600;
    var game = {};
    var keys = {};
    var player;
    var keys = {};
    game.players = {}; // map id->player
    game.attacks = {}; // map id->attack
    
    var stage = new PIXI.Stage(0xFFFFFF);
    stage.interactive = true;
    var renderer = new PIXI.autoDetectRenderer(STAGE_WIDTH, STAGE_HEIGHT);
    document.body.appendChild(renderer.view);
    requestAnimFrame( animate );
    
    player = addElementToStage(RANGER, STAGE_WIDTH / 2, STAGE_HEIGHT / 2, 0);
    
    function animate() {
        requestAnimFrame( animate );
        renderer.render(stage);
    }
    
    // Creates and adds element to stage, returns reference to the element
    function addElementToStage(image, x, y, rotation) {
    	var imgPath;
    	switch (image) {
    	case WIZARD:
    		imgPath = "img/wizard-sm.png";
    		break;
    	case RANGER:
    		imgPath = "img/ranger-sm.png";
    		break;
    	case FIREBALL:
    		imgPath = "img/fireball-sm.png";
    		break;
    	}
	    var texture = PIXI.Texture.fromImage(imgPath);
		var sprite = new PIXI.Sprite(texture);
		sprite.anchor.x = 0.5;
		sprite.anchor.y = 0.5;
		sprite.position.x = x;
		sprite.position.y = y;
		sprite.rotation = rotation;
		stage.addChild(sprite);
		return sprite;
    }
    
    var ws = new WebSocket("ws://127.0.0.1:9999/");
    //var ws = new WebSocket("ws://54.69.151.4:9999/");
    
    ws.onopen = function() {
        console.log("socket opened");
    };
    
    var i = 0;
    ws.onmessage = function (evt) {
    	var message = JSON.parse(evt.data);
    	switch (message.type) {
    	case (WELCOME):
    	    console.log("welcome msg received, id: " + message.id);
    	    userid = message.id;
    	    break;
    	case (PING):
            var ping = new Date().getTime() - message.data;
            $("#pingNum").text(ping);
            break;
    	case (UPDATE):
    		i++;
    		if (i % 100 == 1)
    			console.log(message.data);
    	    var data = message.data;
    		var playerIDs = []; // track IDs that are no longer in game
            var attackIDs = [];
    	    for (id in game.players) {
    	    	playerIDs.push(id);
    	    }
    	    for (id in game.attacks) {
    	    	attackIDs.push(id);
    	    }
        	data.players.forEach(function(player) {
            	var id = player.id;
            	if (id == userid) {
            		// update my own stats here
            	}
            	else if (game.players[id] == null) {
            		game.players[id] = addElementToStage(player.type, player.xPos, player.yPos, 0);
            	}
            	else {
            		game.players[id].position.x = player.xPos;
            		game.players[id].position.y = player.yPos;
            		playerIDs.pop(id);
            	}
            });
    	    data.attacks.forEach(function(attack) {
    	    	console.log(attack);
    	    	var id = attack.id;
    	    	if (game.attacks[id] == null) {
    	    		game.attacks[id] = addElementToStage(attack.type, attack.xPos, attack.yPos, attack.rotation);
    	    	}
    	    	else {
                    game.attacks[id].position.x = attack.xPos;
                    game.attacks[id].position.y = attack.yPos;
                    attackIDs.pop(id);
    	    	}
    	    });
    	    // remove attacks and players that have left the game
    	    playerIDs.forEach(function(id) {
    	    	stage.removeChild(game.players[id]);
    	    	delete game.players[id];
    	    });
            attackIDs.forEach(function(id) {
                stage.removeChild(game.attacks[id]);
                delete game.attacks[id];
            });
            break;
        default:
        	console.log("unknown message type:");
            console.log(message);
        	break;
        }
    };
    
    ws.onclose = function() {
        console.log("socket closed");
    };
    
    ws.onerror = function(err) {
        console.log("error: " + err.data);
    };
    
    // send an attack
    stage.click = function(data) {
        var attack = {};
        attack.xPos = player.position.x;
        attack.yPos = player.position.y;
        var totalVelocity = 15; // TODO: get this from player or from attack type or whatever
        var xClick = data.global.x;
        var yClick = data.global.y;
        var xDelta = xClick - attack.xPos;
        var yDelta = yClick - attack.yPos;
        var d = Math.sqrt((xDelta * xDelta) + (yDelta * yDelta));
        var factor = 10 / d;
        attack.xVelocity = xDelta * factor;
        attack.yVelocity = yDelta * factor;
        attack.ownerID = userid;
        attack.type = FIREBALL;
        attack.rotation = Math.atan2(xDelta, yDelta) * -1 + Math.PI / 2;
        //attack.rotation *= -1;
        var message = {type:ATTACK,id:userid,data:attack};
        ws.send(JSON.stringify(message));
    }
    
    // send ping requests
    setInterval(function() {
        if (userid != 0) { // has the welcome message arrived?
            var message = {};
            message.type = PING;
            message.id = userid;
            message.data = new Date().getTime();
            ws.send(JSON.stringify(message));
        }
    }, 2000);

    // send player update
    setInterval(function() {
        if (userid != 0) { // has the welcome message arrived?
            var playerInfo = {};
            playerInfo.xPos = player.position.x;
            playerInfo.yPos = player.position.y;
            playerInfo.id = userid;
            var message = {};
            message.type = PLAYER_UPDATE;
            message.id = userid;
            message.data = playerInfo;
            ws.send(JSON.stringify(message));
        }
    }, 20);
    
    $(document).keydown(function(event) {
        switch(event.which) {
        case (upArrow):
        case (upKey):
        	keys.up = true;
            break;
        case (downArrow):
        case (downKey):
        	keys.down = true;
            break;
        case (leftArrow):
        case (leftKey):
        	keys.left = true;
            break;
        case (rightArrow):
        case (rightKey):
        	keys.right = true;
            break;
        }
    });
    
    $(document).keyup(function(event) {
        switch(event.which) {
        case (upArrow):
        case (upKey):
        	keys.up = false;
            break;
        case (downArrow):
        case (downKey):
        	keys.down = false;
            break;
        case (leftArrow):
        case (leftKey):
        	keys.left = false;
            break;
        case (rightArrow):
        case (rightKey):
        	keys.right = false;
            break;
        }
    });

    setInterval(function() {
    	player.vx = 0;
    	player.vy = 0;
    	if (keys.right)
    		player.vx += MAX_SPEED;
    	if (keys.left)
    		player.vx -= MAX_SPEED;
    	if (keys.up)
    		player.vy -= MAX_SPEED;
    	if (keys.down)
    		player.vy += MAX_SPEED;
    	player.position.x += player.vx / 1000;
    	player.position.y += player.vy / 1000;
    }, 20);
});