$(function() {

	// messages
    var JOIN_GAME = "join_game";
    var PLAYER_JOINED = "player_joined";
    var UPDATE = "update";
    var WELCOME = "welcome";
    var ACK = "ack";
    var LOCATION = "location";
    var PLAYER_UPDATE = "player_update";
    var ATTACK = "attack";
    var YOU_HAVE_DIED = "you_have_died";
    var PING = "ping";
    // attacks
    var FIREBALL = "fireball";
    var ARROW = "arrow";
    var LASER_BALL = "laser_ball";
    // player classes
    var WIZARD = "wizard";
    var RANGER = "ranger";
    var HAPPY_KITTY = "happy_kitty";
    var ANGRY_KITTY = "angry_kitty";
    var UNIKITTY = "UNIKITTY";
    // msc image types
    var WALL = "wall";
    var PRESENT = "present";
    var PRESENT0 = "present0";
    var PRESENT1 = "present1";
    var PRESENT2 = "present2";
    var PRESENT3 = "present3";
    var TOMBSTONE = "tombstone";
    // images
    var fireballImg = "img/fireball-sm.png";
    var arrowImg = "img/arrow-sm.png";
    var wallImg = "img/brick-wall.png";
    var presentImg0 = "img/present1.png";
    var presentImg1 = "img/present2.png";
    var presentImg2 = "img/present3.png";
    var presentImg3 = "img/present4.png";
    var greenSquareImg = "img/square-green.png";
    var happyKittyImg = "img/happyKitty-sm.png";
    var angryKittyImg = "img/angryKitty-sm.png";
    var laserBallImg = "img/laser-ball.png";
    var tombstoneImg = "img/tombstone-sm.png";
    var assetsToLoader = [fireballImg, arrowImg, wallImg, presentImg0, presentImg1, presentImg2, presentImg3, greenSquareImg, happyKittyImg, angryKittyImg, laserBallImg, tombstoneImg];
    var PLAYER_NAME_FONT = {font:"14px Courier"};
    var userid = 0;
    var MAX_SPEED = 6;
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
	var WALL_COL_DIST = 10;
	var PLAYER_HEIGHT = 32;
	var PLAYER_WIDTH = 24;
    var UPPER_LEFT_WALL_X = (STAGE_WIDTH / 4) - 10;
    var UPPER_LEFT_WALL_Y = (STAGE_HEIGHT / 4) + 10;
    var LOWER_LEFT_WALL_X = (STAGE_WIDTH / 4) - 10;
    var LOWER_LEFT_WALL_Y = (3 * STAGE_HEIGHT / 4) - 10;
    var UPPER_RIGHT_WALL_X = (3 * STAGE_WIDTH / 4) + 10;
    var UPPER_RIGHT_WALL_Y = (STAGE_HEIGHT / 4) + 10;
    var LOWER_RIGHT_WALL_X = (3 * STAGE_WIDTH / 4) + 10;
    var LOWER_RIGHT_WALL_Y = (3 * STAGE_HEIGHT / 4) - 10;
    var game = {};
    var keys = {};
    var player = createNewPlayer();
    var type = WIZARD;
    var name = "";
    game.players = {}; // map id->player
    game.attacks = {}; // map id->attack
    game.presents = {}; // map id->present
    game.walls = {}; // map id->wall
    game.graveYard = [];
    game.textDisplays = []; // array of text displays
    // text areas
    var pingNum = $("#pingNum");
    var classBox = $("#class");
    var currHP = $("#currHP");
    var maxHP = $("#maxHP");
    var atkDmg = $("#atkDmg");
    var ammo = $("#ammo");
    var kills = $("#kills");
    var update = false;
    var ws;
    var rttTable = $("#rttTable");
    var jumbotron = $("#jumbotron");
    
    var loader = new PIXI.AssetLoader(assetsToLoader);
    loader.onComplete = assetsLoaded;
    loader.load();
    
    function assetsLoaded() {
        if (player.connected)
            showWelcomeScreen();
        else
            player.loaded = true;
    }
    
    var stage = new PIXI.Stage(0xFFFFFF);
    stage.interactive = true;
    var renderer = new PIXI.autoDetectRenderer(STAGE_WIDTH, STAGE_HEIGHT);
    document.getElementById("stageBox").appendChild(renderer.view);
    requestAnimFrame( animate );
    
    function animate() {
        requestAnimFrame( animate );
        renderer.render(stage);
    }
    
    // Creates and adds element to stage, returns reference to the element
    function addElementToStage(image, x, y, rotation, color) {
        var sprite;
        switch (image) {
        case WIZARD:
            var texture = new PIXI.Texture.fromImage("img/wizard-" + color + ".png");
            texture.height = 32;
            texture.width = 24;
            sprite = new PIXI.Sprite(texture);
            break;
        case RANGER:
            var texture = new PIXI.Texture.fromImage("img/ranger-" + color + ".png");
            sprite = new PIXI.Sprite(texture);
            break;
        case FIREBALL:
            sprite = new PIXI.Sprite.fromImage(fireballImg);
            break;
        case ARROW:
            sprite = new PIXI.Sprite.fromImage(arrowImg);
            break;
        case WALL:
            sprite = new PIXI.Sprite.fromImage(wallImg);
            break;
        case PRESENT0:
            sprite = new PIXI.Sprite.fromImage(presentImg0);
            break;
        case PRESENT1:
            sprite = new PIXI.Sprite.fromImage(presentImg1);
            break;
        case PRESENT2:
            sprite = new PIXI.Sprite.fromImage(presentImg2);
            break;
        case PRESENT3:
            sprite = new PIXI.Sprite.fromImage(presentImg3);
            break;
        case HAPPY_KITTY:
            sprite = new PIXI.Sprite.fromImage(happyKittyImg);
            break;
        case ANGRY_KITTY:
            sprite = new PIXI.Sprite.fromImage(angryKittyImg);
            break;
        case LASER_BALL:
        	sprite = new PIXI.Sprite.fromImage(laserBallImg);
        	break;
        case TOMBSTONE:
        	sprite = new PIXI.Sprite.fromImage(tombstoneImg);
        	break;
        }
        sprite.anchor.x = 0.5;
        sprite.anchor.y = 0.5;
        sprite.position.x = Math.round(x);
        sprite.position.y = Math.round(y);
        sprite.rotation = rotation;
        stage.addChild(sprite);
        return sprite;
    }
    
    function startGame(name, type) {
        $('#overlay').css("display", "none");
        var msg = {};
        msg.type = JOIN_GAME;
        msg.id = userid;
        msg.data = {};
        msg.data.type = type;
        msg.data.id = userid;
        msg.data.username = name;
        ws.send(JSON.stringify(msg));
    }
    
    function showWelcomeScreen() {
        $("#black").css("display", "none");
        $("#nameSelector").css("display", "none");
        $("#charSelector").css("display", "none");
        $("#youHaveDied").css("display", "none");
        $("#welcome").css("display", "block");
    }
    
    function showNameSelector() {
        $("#black").css("display", "none");
        $("#welcome").css("display", "none");
        $("#charSelector").css("display", "none");
        $("#youHaveDied").css("display", "none");
        $("#nameSelector").css("display", "block");
    }
    
    function showCharSelector() {
        $("#black").css("display", "none");
        $("#welcome").css("display", "none");
        $("#nameSelector").css("display", "none");
        $("#youHaveDied").css("display", "none");
        $("#charSelector").css("display", "block");
    }
    
    function showYouHaveDied() {
        $("#black").css("display", "none");
        $("#overlay").css("display", "block");
        $("#welcome").css("display", "none");
        $("#nameSelector").css("display", "none");
        $("#charSelector").css("display", "none");
        $("#youHaveDied").css("display", "block");
    }
    
    function youHaveDied(message) {
        stage.removeChild(player);
        player = createNewPlayer();
        $("#deathTextBox").text(message);
        showYouHaveDied();
    }
    
    $(".playButton").click(showNameSelector);
    
    $("#okNameButton").click(function() {
        name = $("#nameBox").val();
        if (name.length != 0)
            showCharSelector();
        else
            $("#youMustHaveAName").css("display", "block");
    });
    
    $("#wizardButton").click(function() {
        type = WIZARD;
        startGame(name, WIZARD);
    });
    
    $("#rangerButton").click(function() {
        type = RANGER;
        startGame(name, RANGER);
    });
    
    $("#playAgainButton").click(function() {
        startGame(name, type);
    });

    // Upper left
    game.walls[0] = (addElementToStage(WALL, UPPER_LEFT_WALL_X, UPPER_LEFT_WALL_Y, -Math.PI / 4), 0);
    // Lower left
    game.walls[1] = (addElementToStage(WALL, LOWER_LEFT_WALL_X, LOWER_LEFT_WALL_Y, Math.PI / 4), 0);
    // Upper right
    game.walls[2] = (addElementToStage(WALL, UPPER_RIGHT_WALL_X, UPPER_RIGHT_WALL_Y, Math.PI / 4), 0);
    // Lower right
    game.walls[3] = (addElementToStage(WALL, LOWER_RIGHT_WALL_X, LOWER_RIGHT_WALL_Y, -Math.PI / 4), 0);
    
    var serverAddress = "ws://54.69.151.4:9999/";
    if (document.location.hostname == "localhost")
        serverAddress = "ws://127.0.0.1:9999/";
    ws = new WebSocket(serverAddress);
    
    ws.onopen = function() {
        console.log("socket opened");
        if (player.loaded)
            showWelcomeScreen();
        else
            player.connected = true;
    };

    var i = 0;
    ws.onmessage = function (evt) {
        var message = JSON.parse(evt.data);
        switch (message.type) {
        case (WELCOME):
            console.log("welcome msg received, id: " + message.id);
            userid = message.id;
            var ackWelcome = {};
            ackWelcome.type = ACK;
            ackWelcome.id = userid;
            ackWelcome.data = userid;
            ws.send(JSON.stringify(ackWelcome));
            break;
        case (YOU_HAVE_DIED):
            youHaveDied(message.data);
            break;
        case (PING):
            // console.log(message);
            ws.send(JSON.stringify(message));
            break;
        case (PLAYER_JOINED):
            console.log(message);
            var playerInfo = message.data;
            player = addElementToStage(playerInfo.type, playerInfo.xPos, playerInfo.yPos, 0, playerInfo.color);
            addNameToPlayer(player, playerInfo.username);
            player.type = playerInfo.type;
            player.id = playerInfo.id;
            player.spectating = false;
            player.username = playerInfo.username;
            var location = {};
            $(document).ready(function() {
                $.getJSON("http://www.telize.com/geoip?callback=?",
                          function(json) {
                              location.latitude = json.latitude;
                              location.longitude = json.longitude;
                              location.city = json.city;
                              location.region = json.region;
                              location.country = json.country;
                              location.id = userid;
                              location.name = player.username;
                              var loc_message = {};
                              loc_message.type = LOCATION;
                              loc_message.id = userid;
                              loc_message.data = location;
                              ws.send(JSON.stringify(loc_message));
                          }
                         );
            });
            break;
        case (UPDATE):
            i++;
            if (i % 100 == 1) {
                console.log(message.data);
            }
            update = message.data;    
            break;
        case ("text_display"):
        	var text = new PIXI.Text(message.data.text, {font:"16px Courier", fill:message.data.color});
        	text.expiration = ( new Date().getTime() ) + 750;
        	text.anchor.x = 0.5;
        	text.anchor.y = 1;
        	text.scale.x = player.scale.x;
        	player.addChild(text);
        	game.textDisplays.push(text);
        	break;
        case ("chat"):
          var chat_messages = [];
          chat_messages = message.data;
          console.log(message.data);
          if (Array.isArray(message.data)) {
              for (var i in chat_messages) {
          	      jumbotron.append("<p>" + chat_messages[i] + "</p>");
          	      jumbotron.scrollTop(jumbotron.prop('scrollHeight'));
              }
          } else {
          	  jumbotron.append("<p>" + message.data + "</p>");
          	  jumbotron.scrollTop(jumbotron.prop('scrollHeight'));
          }
        	break;
        default:
            console.log("unknown message type:");
            console.log(message);
            break;
        }
    };
    
    ws.onclose = function() {
        console.log("socket closed");
        $("#loadingImg").css("display", "none");
        $("#somethingWentWrong").css("display", "block");
    };
    
    ws.onerror = function(err) {
        console.log("error: " + err.data);
        $("#loadingImg").css("display", "none");
        $("#overlay").css("display", "none");
        $("#somethingWentWrong").css("display", "block");
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
            switch(player.type) {
            case (WIZARD):
                attack.type = FIREBALL;
                break;
            case (RANGER):
                attack.type = ARROW;
                break;
            }
            attack.rotation = Math.atan2(xDelta, yDelta) * -1 + Math.PI / 2;
            var message = {type:ATTACK,id:userid,data:attack};
            ws.send(JSON.stringify(message));
        }
    }
    

    setInterval(function() {
        if (!player.spectating) {
	          player.vx = 0;
	          player.vy = 0;
            if (keys.right && player.position.x < STAGE_WIDTH - 20 && !hitWallGoingRight())
                player.vx += MAX_SPEED;
            if (keys.left && player.position.x > 20 && !hitWallGoingLeft())
                player.vx -= MAX_SPEED;
		          if (keys.up && player.position.y > 20 && !hitWallGoingUp())
		              player.vy -= MAX_SPEED;
		          if (keys.down && player.position.y < STAGE_HEIGHT - 20 && !hitWallGoingDown())
		              player.vy += MAX_SPEED;
		          if (player.vx > 0) {
		              player.scale.x = -1;
		          }
		          else if (player.vx < 0) {
		              player.scale.x = 1;
		          }
		          player.text.scale.x = player.scale.x;
	              game.textDisplays.forEach(function(text) {
	            	  text.scale.x = player.scale.x;
	              })
		          player.position.x += player.vx;
		          player.position.y += player.vy;
        }
    }, 20);

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
        updateView();
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
    
    function updateView() {
        if (update.players == undefined)
            return;
        var data = update;
        var currTime = new Date().getTime();
        rttTable.html("<tr><th></th><th>Player</th><th>RTT</th><th>Kills</th></tr>");
        data.players.sort(sortPlayers);
        data.players.forEach(function(p) {
            if (p.username != UNIKITTY)
            	rttTable.append(createTableEntry(p));
            var id = p.id;
            if (id == userid) {
                // update my own stats here
                currHP.text(p.currHP);
                maxHP.text(p.maxHP);
                ammo.text(p.ammo);
                atkDmg.text(p.atkDmg);
                classBox.text(p.type);
                kills.text(p.kills);
                pingNum.text(p.rtt);
                player.currHP = p.currHP;
                player.maxHP = p.maxHP;
                updateHealthBarWidth(player);
            }
            else if (game.players[id] == null) {
                // add new player
                game.players[id] = addElementToStage(p.type, p.xPos, p.yPos, 0, p.color);
                addNameToPlayer(game.players[id], p.username);
                game.players[id].currHP = p.currHP;
                game.players[id].maxHP = p.maxHP;
                updateHealthBarWidth(game.players[id]);
                game.players[id].lastUpdate = currTime;
            }
            else {
                if (game.players[id].position.x > p.xPos) {
                    game.players[id].scale.x = 1;
                }
                else if (game.players[id].position.x < p.xPos) {
                    game.players[id].scale.x = -1;
                }
                game.players[id].text.scale.x = game.players[id].scale.x;
                game.players[id].position.x = Math.round(p.xPos);
                game.players[id].position.y = Math.round(p.yPos);
                game.players[id].currHP = p.currHP;
                game.players[id].maxHP = p.maxHP;
                updateHealthBarWidth(game.players[id]);
                game.players[id].lastUpdate = currTime;
            }
        });
        data.attacks.forEach(function(attack) {
            var id = attack.id;
            if (game.attacks[id] == null) {
                game.attacks[id] = addElementToStage(attack.type, attack.xPos, attack.yPos, attack.rotation);
                game.attacks[id].lastUpdate = currTime;
                game.attacks[id].type = attack.type;
            }
            else {
            	var atk = game.attacks[id];
                atk.position.x = Math.round(attack.xPos);
                atk.position.y = Math.round(attack.yPos);
                atk.lastUpdate = currTime;
                if (atk.type == LASER_BALL) {
	                if (atk.scale.x > 1.3 || atk.scale.x < 0.7)
	                	atk.scale.y = atk.scale.x = 1;
	                else
	                	atk.scale.x = atk.scale.y = atk.scale.x + Math.random() - Math.random();
                }
            }
        });
        data.presents.forEach(function(present) {
            var id = present.id;
            if (game.presents[id] == null) {
                game.presents[id] = addElementToStage(PRESENT + present.imageNum, present.xPos, present.yPos, 0);
                game.presents[id].lastUpdate = currTime;
            }
            else {
                game.presents[id].lastUpdate = currTime;
            }
        });
        for (var i in game.textDisplays) {
        	var text = game.textDisplays[i];
        	if (currTime > text.expiration) {
        		player.removeChild(text);
        		delete game.textDisplays[i];
        	} else {
        		text.anchor.y += 0.075;
        	}
        }
        data.graveYard.forEach(function(grave) {
        	var id = grave.xPos + grave.yPos;
        	if (game.graveYard[id] == null) {
        		game.graveYard[id] = addElementToStage(TOMBSTONE, grave.xPos, grave.yPos, 0);
        		game.graveYard[id].lastUpdate = currTime;
        	}
        	else {
        		game.graveYard[id].lastUpdate = currTime;
        	}
        });
        // remove elements no longer in game
        for (var id in game.players) {
            if (game.players[id].lastUpdate != currTime) {
                stage.removeChild(game.players[id]);
                delete game.players[id];
            }
        }
        for (var id in game.attacks) {
            if (game.attacks[id].lastUpdate != currTime) {
                stage.removeChild(game.attacks[id]);
                delete game.attacks[id];
            }
        }
        for (var id in game.presents) {
            if (game.presents[id].lastUpdate != currTime) {
                stage.removeChild(game.presents[id]);
                delete game.presents[id];
            }
        }
        for (var id in game.graveYard) {
            if (game.graveYard[id].lastUpdate != currTime) {
                stage.removeChild(game.graveYard[id]);
                delete game.graveYard[id];
            }
        }
    }
    
    function updateHealthBarWidth(player) {
    	var width = ( 80.0 * player.currHP ) / player.maxHP;
    	// console.log(width);
    	player.healthBar.width = width;
    }
    
    function addNameToPlayer(player, name) {
        player.text = new PIXI.Text(name, PLAYER_NAME_FONT);
        player.healthBar = PIXI.Sprite.fromImage(greenSquareImg);
        player.healthBar.anchor.x = 0.5;
        player.healthBar.anchor.y = -1.3;
        player.healthBar.height = 14;
        player.healthBar.width = 80;
        player.addChild(player.healthBar);
        player.text.anchor.x = 0.5;
        player.text.anchor.y = -1.3;
        player.addChild(player.text);
    }
    
    function createNewPlayer() {
    	var p = {};
    	p.spectating = true;
    	return p;
    }
    
    // create text for table entry
    function createTableEntry(player) {
        var result = "<tr><td><img src='img/square-" + player.color + ".png' /></td><td>" + player.username + "</td><td>" + player.rtt + "</td><td>" + player.kills + "</td></tr>"; // TODO: add color square
        return result;
    }

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

    function checkWallNE(x, y) {
        var x1 = x - 4 * 14.142;
        var y1 = y + 4 * 14.142;
        for(var i = 0; i <= 9; i++) {
		        var xDelta = Math.abs(player.position.x - x1);
		        var yDelta = Math.abs(player.position.y - y1);
		        if (isWallCollision(xDelta, yDelta)) {
			          return true;
		        }
		        x1 += 14.142;
		        y1 -= 14.142;
	      }
	      return false;
    }

    function checkWallNW(x, y) {
        var x1 = x + 4 * 14.142;
        var y1 = y + 4 * 14.142;
        for(var i = 0; i <= 9; i++) {
		        var xDelta = Math.abs(player.position.x - x1);
		        var yDelta = Math.abs(player.position.y - y1);
		        if (isWallCollision(xDelta, yDelta)) {
			          return true;
		        }
		        x1 -= 14.142;
		        y1 -= 14.142;
	      }
	      return false;
    }

    function isWallCollision(xDelta, yDelta) {
	      return xDelta < WALL_COL_DIST && yDelta < WALL_COL_DIST;
    } 

    function hitWallGoingRight() {
        return checkWallNE(UPPER_LEFT_WALL_X - 20,UPPER_LEFT_WALL_Y) || checkWallNE(LOWER_RIGHT_WALL_X - 20,LOWER_RIGHT_WALL_Y) || checkWallNW(UPPER_RIGHT_WALL_X - 20,UPPER_RIGHT_WALL_Y) || checkWallNW(LOWER_LEFT_WALL_X - 20,LOWER_LEFT_WALL_Y)
    }

    function hitWallGoingLeft() {
        return checkWallNE(UPPER_LEFT_WALL_X + 20,UPPER_LEFT_WALL_Y) || checkWallNE(LOWER_RIGHT_WALL_X + 20,LOWER_RIGHT_WALL_Y) || checkWallNW(UPPER_RIGHT_WALL_X + 20,UPPER_RIGHT_WALL_Y) || checkWallNW(LOWER_LEFT_WALL_X + 20,LOWER_LEFT_WALL_Y)
    }

    function hitWallGoingUp() {
        return checkWallNE(UPPER_LEFT_WALL_X,UPPER_LEFT_WALL_Y + 20) || checkWallNE(LOWER_RIGHT_WALL_X,LOWER_RIGHT_WALL_Y + 20) || checkWallNW(UPPER_RIGHT_WALL_X,UPPER_RIGHT_WALL_Y + 20) || checkWallNW(LOWER_LEFT_WALL_X,LOWER_LEFT_WALL_Y + 20)
    }

    function hitWallGoingDown() {
        return checkWallNE(UPPER_LEFT_WALL_X,UPPER_LEFT_WALL_Y - 20) || checkWallNE(LOWER_RIGHT_WALL_X,LOWER_RIGHT_WALL_Y - 20) || checkWallNW(UPPER_RIGHT_WALL_X,UPPER_RIGHT_WALL_Y - 20) || checkWallNW(LOWER_LEFT_WALL_X,LOWER_LEFT_WALL_Y - 20)
    }
    
    function sortPlayers(p1, p2) {
    	return p2.kills - p1.kills;
    }
});
