$(function() {

	var JOIN_GAME = "join_game";
	var PLAYER_JOINED = "player_joined";
	  var FIREBALL = "fireball";
	  var WIZARD = "wizard";
	  var RANGER = "ranger";
    var WALL = "wall";
	  var UPDATE = "update";
	  var PING = "ping";
	  var WELCOME = "welcome";
	  var PLAYER_UPDATE = "player_update";
	  var ATTACK = "attack";
    var wizardTexture = PIXI.Texture.fromImage("img/wizard-sm.png");
    var rangerTexture = PIXI.Texture.fromImage("img/ranger-sm.png");
    var fireballTexture = PIXI.Texture.fromImage("img/fireball-sm.png");
    var wallTexture = PIXI.Texture.fromImage("img/brick-wall.png");
    var userid = 0;
    var MAX_SPEED = 6;
    var FRICTION = 0.97;
    var upArrow = 38;
    var downArrow = 40;
    var rightArrow = 39;
    var leftArrow = 37;
    var upKey = 87;
    var downKey = 83;
    var rightKey = 68;
    var leftKey = 65;
    var STAGE_WIDTH = 800;
    var STAGE_HEIGHT = 600;
    var game = {};
    var keys = {};
    var type = WIZARD;
    var name = "";
    game.players = {}; // map id->player
    game.attacks = {}; // map id->attack
    game.presents = {}; // map id->present
    game.walls = {}; // map id->wall

    // text areas
    var pingNum = $("#pingNum");
    var classBox = $("#class");
    var currHP = $("#currHP");
    var maxHP = $("#maxHP");
    var atkDmg = $("#atkDmg");
    var ammo = $("#ammo");
    
    var stage = new PIXI.Stage(0xFFFFFF);
    stage.interactive = true;
    var renderer = new PIXI.autoDetectRenderer(STAGE_WIDTH, STAGE_HEIGHT);
    // document.body.appendChild(renderer.view);
    document.getElementById("stageBox").appendChild(renderer.view);
    requestAnimFrame( animate );
    
    var player = {};
    player.spectating = true;
    game.walls[0] = (addElementToStage(WALL, (STAGE_WIDTH / 4) - 10, (STAGE_HEIGHT / 4) + 10, -Math.PI / 4));
    game.walls[1] = (addElementToStage(WALL, (STAGE_WIDTH / 4) - 10, (3 * STAGE_HEIGHT / 4) - 10, Math.PI / 4));
    game.walls[2] = (addElementToStage(WALL, (3 * STAGE_WIDTH / 4) + 10, (STAGE_HEIGHT / 4) + 10, Math.PI / 4));
    game.walls[3] = (addElementToStage(WALL, (3 * STAGE_WIDTH / 4) + 10, (3 * STAGE_HEIGHT / 4) - 10, -Math.PI / 4));
    
    function animate() {
        requestAnimFrame( animate );
        renderer.render(stage);
    }
    
    function depthCompare(a,b) {
      if (a.z == null)
    	  a.z = 1;
      if (b.z == null)
    	  b.z = 1;
	  if (a.z < b.z)
	     return -1;
	  if (a.z > b.z)
	    return 1;
	  return 0;
	}
    
    // Creates and adds element to stage, returns reference to the element
    function addElementToStage(image, x, y, rotation) {
    	  var texture;
    	  switch (image) {
    	  case WIZARD:
            texture = wizardTexture;
    		    break;
    	  case RANGER:
            texture = rangerTexture;
    		    break;
    	  case FIREBALL:
            texture = fireballTexture;
    		    break;
        case WALL:
            texture = wallTexture;
            break;
    	  }
		    var sprite = new PIXI.Sprite(texture);
		    sprite.anchor.x = 0.5;
		    sprite.anchor.y = 0.5;
		    sprite.position.x = x;
		    sprite.position.y = y;
		    sprite.rotation = rotation;
		    stage.addChild(sprite);
		    return sprite;
    }
    
    var serverAddress = "ws://54.69.151.4:9999/";
    if (document.location.hostname == "localhost")
    	serverAddress = "ws://127.0.0.1:9999/";
    var ws = new WebSocket(serverAddress);
    
    function startGame(name, type) {
    	$('#charSelector').remove();
    	var msg = {};
    	msg.type = JOIN_GAME;
    	msg.id = userid;
    	msg.data = {};
    	msg.data.type = type;
    	msg.data.id = userid;
    	msg.data.username = name;
    	ws.send(JSON.stringify(msg));
    }
    
    function joinGameSequence() {
        $('body').append('<div id="charSelector"></div>');
        $("#charSelector").append("<p>CHOOSE YOUR NAME</p>");
        $("#charSelector").append("<input type='text' id='nameBox'></input>");
        $("#charSelector").append("<button id='okGoButton'>OK</Button>");
        $("#okGoButton").click(function() {
        	var name = $("#nameBox").val();
        	if (name != "") {
                $("#charSelector").empty();
                $("#charSelector").append("<p>SELECT YOUR HERO</p>");
                $("#charSelector").append("<img src='img/wizard-lg.png' id='wizardButton' />");
                $("#charSelector").append("<img src='img/ranger-lg.png' id='rangerButton' />");
                $("#wizardButton").click(function() {
                	startGame(name, WIZARD);
                })
                $("#rangerButton").click(function() {
                	startGame(name, RANGER);
                })
        	}
        });
    }
    
    ws.onopen = function() {
        console.log("socket opened");
        joinGameSequence();
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
            ws.send(message);
            break;
    	case (PLAYER_JOINED):
    		
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
            		    currHP.text(player.currHP);
            		    maxHP.text(player.maxHP);
            		    ammo.text(player.ammo);
            		    atkDmg.text(player.atkDmg);
            		    classBox.text(player.type);
            		    if (i == 20)
            			      console.log(player);
            	  }
            	  else if (game.players[id] == null) {
            		    game.players[id] = addElementToStage(player.type, player.xPos, player.yPos, 0);
            	  }
            	  else {
            		    if (game.players[id].position.x > player.xPos)
            			      game.players[id].scale.x = 1;
            		    else if (game.players[id].position.x < player.xPos)
            			      game.players[id].scale.x = -1;
            		    game.players[id].position.x = player.xPos;
            		    game.players[id].position.y = player.yPos;
            		    playerIDs.pop(id);
            	  }
            });
    	      data.attacks.forEach(function(attack) {
    	    	    //console.log(attack);
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
    	if (!player.spectating) {
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
	        var message = {type:ATTACK,id:userid,data:attack};
	        ws.send(JSON.stringify(message));
    	}
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
        if (!player.spectating) { // has the welcome message arrived?
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
    	if (!player.spectating) {
    		player.vx = 0;
      	  player.vy = 0;
      	  if (keys.right)
              if (player.position.x < STAGE_WIDTH - 20)
      		        player.vx += MAX_SPEED;
      	  if (keys.left)
              if (player.position.x > 20)
                  player.vx -= MAX_SPEED;
      	  if (keys.up)
              if (player.position.y > 20)
      		        player.vy -= MAX_SPEED;
      	  if (keys.down)
              if (player.position.y < STAGE_HEIGHT - 20)
      		        player.vy += MAX_SPEED;
      	  if (player.vx > 0)
      		    player.scale.x = -1;
      	  else if (player.vx < 0)
      		    player.scale.x = 1;
      	  player.position.x += player.vx;
      	  player.position.y += player.vy;
    	}
    }, 20);
});
