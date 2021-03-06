package com.mpec.backstab.game;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mpec.backstab.drops.ASBoost;
import com.mpec.backstab.drops.ASReduce;
import com.mpec.backstab.drops.ATKBoost;
import com.mpec.backstab.drops.ATKReduce;
import com.mpec.backstab.drops.Drop;
import com.mpec.backstab.drops.MSBoost;
import com.mpec.backstab.drops.MSReduce;
import com.mpec.backstab.drops.MobBoost;
import com.mpec.backstab.enemy_character.Enemy;
import com.mpec.backstab.enemy_character.Golem;
import com.mpec.backstab.enemy_character.SwordZombie;
import com.mpec.backstab.enemy_character.WizardZombie;
import com.mpec.backstab.main_character.Bullet;
import com.mpec.backstab.main_character.OtherPlayer;
import com.mpec.backstab.main_character.Playable;
import com.mpec.backstab.map.MapGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class GameScreen implements Screen {

    private final float UPDATE_TIME = 1/20f;

    float timer;

    private int whichEnemyID;

    public static boolean bulletIsShot;

    public static int contadorDrops = 0;

    String id;
    JSONObject enemyJSON;
    JSONArray enemyJSONArray;
    final Backstab game;
    Stage stage;
    TouchPadTest touchpad;
    public static Array<Enemy> enemyAL;
    Date startDate = new Date();
    boolean enemyToBeCreated = false;
    Date endDate;
    int numSeconds;
    float attackTimer;

    private Drop pickedDrop;

    private BitmapFont rankingDraw;

    private Socket socket;

    JSONObject updater;

    public static Array<Long> killedEnemies;

    public static HashMap<String, OtherPlayer> otherPlayers;

    int ranking = 1;


    float movingX;
    float movingY;
    Array<Drop> groundDrops;

    public static int contadorMatados=0;

    Array<Enemy> initializableMonsters;
    Array<Bullet> initializableBullets;
    Array<Drop> initializableDrops;

    public static boolean multiplayer = false;


    public GameScreen(Backstab game) {
        this.game = game;
        stage = new Stage(game.viewport, game.batch);
        Gdx.input.setInputProcessor(stage);
        otherPlayers = new HashMap<String, OtherPlayer>();
        attackTimer=0;
        rankingDraw = new BitmapFont();
        contadorMatados = 0;
        contadorDrops = 0;
        groundDrops = new Array<Drop>();

        rankingDraw.setColor(Color.BLACK);
        rankingDraw.getData().setScale(5, 5);
        initializableMonsters = new Array<Enemy>();
        initializableBullets = new Array<Bullet>();
        initializableDrops = new Array<Drop>();
        touchpad = new TouchPadTest();
        bulletIsShot = false;
        stage.addActor(game.timmy);
        stage.addActor(touchpad);
        killedEnemies = new Array<Long>();
        enemyAL = new Array<Enemy>();
        connectSocket();
        sendInitialPosition();
        configSocketEvents();
    }

    public void sendInitialPosition() {
        updater = new JSONObject();
        try {
            updater.put("x", game.timmy.getX());
            updater.put("y", game.timmy.getY());
            updater.put("direction", game.timmy.getDirection());
            socket.emit("initial", updater);
        } catch (JSONException e) {
            Gdx.app.log("SOCKETIO", "Error sending data to server! in updateServer()");
        }
    }

    public void updateServer(float delta) {
        timer += delta;
        if (timer >= UPDATE_TIME){
            if(game.timmy.hasMoved()) {
                updater = new JSONObject();
                try {
                    updater.put("x", game.timmy.getX());
                    updater.put("y", game.timmy.getY());
                    updater.put("direction", game.timmy.getDirection());
                    socket.emit("playerMoved", updater);
                } catch (JSONException e) {
                    Gdx.app.log("SOCKETIO", "Error sending data to server! in updateServer()");
                }
            }

            updater = new JSONObject();
            enemyJSONArray = new JSONArray();

            for (int i = 0; i < enemyAL.size; i++) {
                enemyJSON = new JSONObject();
                enemyJSON.put("id", enemyAL.get(i).getId());
                enemyJSON.put("whichEnemyId", enemyAL.get(i).getWhichEnemyId());
                enemyJSON.put("x", enemyAL.get(i).getX());
                enemyJSON.put("y", enemyAL.get(i).getY());
                enemyJSON.put("multiplier", enemyAL.get(i).getMultiplier());
                enemyJSON.put("vidaActual", enemyAL.get(i).getVidaActual());
                enemyJSONArray.put(enemyJSON);
            }
            updater.put("enemy", enemyJSONArray);
            socket.emit("updateMonsters", updater);

            updater = new JSONObject();
            enemyJSONArray = new JSONArray();

            for (int i = killedEnemies.size - 1; i >= 0; i--) {
                enemyJSON = new JSONObject();
                enemyJSON.put("id", killedEnemies.get(i));
                enemyJSONArray.put(enemyJSON);
                killedEnemies.removeIndex(i);
            }

            updater.put("enemy", enemyJSONArray);
            socket.emit("enemiesDead", updater);

            timer = 0;
        }
        if(bulletIsShot){
            updater = new JSONObject();
            updater.put("angle", Playable.bulletToSend.getAngleToEnemy());
            updater.put("x", Playable.bulletToSend.getBulletX());
            updater.put("y", Playable.bulletToSend.getBulletY());
            updater.put("range", Playable.bulletToSend.getRange());
            socket.emit("bulletShot", updater);
            GameScreen.bulletIsShot = false;
        }

        if(ASReduce.asReduceActive){
            ASReduce.asReduceActive = false;
            socket.emit("asReduce");
        }

        if(ATKReduce.atkReduceActive){
            ATKReduce.atkReduceActive = false;
            socket.emit("atkReduce");
        }

        if(MSReduce.msReduceActive){
            MSReduce.msReduceActive = false;
            socket.emit("msReduce");
        }

        if(MobBoost.mobBoostActive){
            MobBoost.mobBoostActive = false;
            socket.emit("mobBoost");
        }
    }

    public void pickedADrop(){
        if(pickedDrop != null){
            JSONObject object = new JSONObject();
            object.put("dropId", pickedDrop.getId());
            socket.emit("pickedADrop", object);
        }

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.mapGenerator.paintMap(game.batch);
        rankingDraw.draw(game.batch, String.valueOf(ranking), (game.camera.position.x + stage.getWidth() / 2 - 120), (game.camera.position.y + stage.getHeight() / 2 - 60));
        game.batch.end();
        if(multiplayer) {
            for (Map.Entry<String, OtherPlayer> entry : otherPlayers.entrySet()) {
                if (!entry.getValue().isInitialized) {
                    entry.getValue().initialize(game.timmy.getAttack(), game.timmy.getDefense(), game.timmy.getAttack_speed(), game.timmy.getHp(), game.timmy.getMovement_speed());
                    stage.addActor(entry.getValue());
                }
            }

            if(pickDrop()){
                pickedADrop();
            }

            for (int i = initializableMonsters.size - 1; i >= 0; i--) {
                if (initializableMonsters.get(i).getClass().equals(Golem.class)) {
                    initializableMonsters.get(i).initialize(Golem.baseAttack, Golem.baseDefense, Golem.baseAttackSpeed, Golem.baseHp, Golem.baseMovementSpeed, Golem.baseRange);
                } else if (initializableMonsters.get(i).getClass().equals(WizardZombie.class)) {
                    initializableMonsters.get(i).initialize(WizardZombie.baseAttack, WizardZombie.baseDefense, WizardZombie.baseAttackSpeed, WizardZombie.baseHp, WizardZombie.baseMovementSpeed, WizardZombie.baseRange);
                } else if (initializableMonsters.get(i).getClass().equals(SwordZombie.class)) {
                    initializableMonsters.get(i).initialize(SwordZombie.baseAttack, SwordZombie.baseDefense, SwordZombie.baseAttackSpeed, SwordZombie.baseHp, SwordZombie.baseMovementSpeed, SwordZombie.baseRange);
                }

                stage.addActor(initializableMonsters.get(i));
                enemyAL.add(initializableMonsters.get(i));
                initializableMonsters.removeIndex(i);
            }
            for (int i = initializableBullets.size - 1; i >= 0; i--) {
                initializableBullets.get(i).initialize();
                stage.addActor(initializableBullets.get(i));
                initializableBullets.removeIndex(i);
            }

            for (int i = initializableDrops.size - 1; i >= 0; i--) {
                initializableDrops.get(i).initialize();
                groundDrops.add(initializableDrops.get(i));
                stage.addActor(initializableDrops.get(i));
                initializableDrops.removeIndex(i);
            }

            updateServer(Gdx.graphics.getDeltaTime());
        }
        game.stateTime = game.stateTime + 1 + Gdx.graphics.getDeltaTime();
        checkCharacterAction();
        touchpad.setBounds(game.camera.position.x - touchpad.getWidth() / 2, game.camera.position.y - stage.getHeight() / 2 + 15, 150, 150);
        endDate = new Date();
        numSeconds = (int) ((endDate.getTime() - startDate.getTime()) / 1000);


        ranking = otherPlayers.size() + 1;

        game.moveCamera();

        attackTimer+=Gdx.graphics.getDeltaTime();

        game.camera.update();

        stage.act();


        if (game.timmy.getX() < 0) {
            movingX = 0;
        }
        if (game.timmy.getX() + game.timmy.getAction().getWidth() > MapGenerator.WORLD_WIDTH) {
            movingX = MapGenerator.WORLD_WIDTH - game.timmy.getAction().getWidth();
        }

        if (game.timmy.getY() < 0) {
            movingY = 0;
        }

        if (game.timmy.getY() + game.timmy.getAction().getHeight() > MapGenerator.WORLD_HEIGHT) {
            movingY = MapGenerator.WORLD_HEIGHT - game.timmy.getAction().getHeight();
        }

        if (!playerOverlaps()) {
            movingX = (float) (game.timmy.getX() + touchpad.getKnobPercentX() * game.timmy.getMovement_speed() * Gdx.graphics.getDeltaTime());
            movingY = (float) (game.timmy.getY() + touchpad.getKnobPercentY() * game.timmy.getMovement_speed() * Gdx.graphics.getDeltaTime());
            game.timmy.getPlayableRectangle().setPosition(movingX, movingY);
            game.timmy.setPosition(movingX, movingY);
        }


        if (!multiplayer) {
            try {
                whichEnemy((int) (Math.random() * 3));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if(game.timmy.getVidaActual()<=0){
            game.setScreen(new EndMenuScreen(game, numSeconds, ranking));
            socket.disconnect();
        }

        stage.draw();



    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        game.batch.dispose();
        for(Enemy enemy : enemyAL){
            enemy.getSlashEnemy().dispose();
            enemy.getHealthRedBar().dispose();
            enemy.getEnemySprite().getTexture().dispose();
            enemy.getEnemyAtlas().dispose();
        }
        game.mapGenerator.dispose();
        game.timmy.getAction().getTexture().dispose();
        game.timmy.getWalkPlayer().dispose();
        game.timmy.getPlayerAtlas().dispose();
        game.timmy.getWalkPlayer().dispose();
        stage.dispose();
        socket.disconnect();
    }

    private void checkCharacterAction(){

        if(touchpad.isTouched()){
            game.timmy.goAtackEnergyBall(false,stage);
            if(touchpad.getKnobPercentX() > 0.4){

                game.timmy.setDirection(AvailableActions.LOOK_RIGHT);
                game.timmy.goMove(AvailableActions.MOVE_RIGHT);

            } else if (touchpad.getKnobPercentX() < -0.4) {

                game.timmy.setDirection(AvailableActions.LOOK_LEFT);
                game.timmy.goMove(AvailableActions.MOVE_LEFT);

            } else if (touchpad.getKnobPercentY() > 0) {

                game.timmy.setDirection(AvailableActions.LOOK_UP);
                game.timmy.goMove(AvailableActions.MOVE_UP);

            } else if (touchpad.getKnobPercentY() < 0) {

                game.timmy.setDirection(AvailableActions.LOOK_DOWN);
                game.timmy.goMove(AvailableActions.MOVE_DOWN);

            }else{
                if(game.timmy.getAttack_speed()*Gdx.graphics.getDeltaTime()<attackTimer) {
                    game.timmy.goAtackEnergyBall(true, stage);
                    attackTimer=0;

                }
            }
        }else{
            if(game.timmy.getAttack_speed()*Gdx.graphics.getDeltaTime()<attackTimer) {
                game.timmy.goAtackEnergyBall(true, stage);
                attackTimer=0;

            }

        }
    }

    private boolean playerOverlaps() {
        for (Rectangle r : MapGenerator.collision) {
            if (game.timmy.getPlayableRectangle().overlaps(r)) {
                switch (game.timmy.getDirection()) {
                    case AvailableActions.LOOK_LEFT:
                        game.timmy.setX(game.timmy.getX() + 5);
                        break;
                    case AvailableActions.LOOK_RIGHT:
                        game.timmy.setX(game.timmy.getX() - 5);
                        break;
                    case AvailableActions.LOOK_UP:
                        game.timmy.setY(game.timmy.getY() - 5);
                        break;
                    case AvailableActions.LOOK_DOWN:
                        game.timmy.setY(game.timmy.getY() + 5);
                        break;
                }
                return true;
            }
        }
        return false;
    }

    public boolean pickDrop(){
        for(int i = groundDrops.size - 1; i >= 0; i--){
            if(game.timmy.getPlayableRectangle().overlaps(groundDrops.get(i).getDropRectangle())){
                groundDrops.get(i).changeStats();
                if(groundDrops.get(i).getDropName().equalsIgnoreCase("mobs_damage_boost")){
                    ((MobBoost)groundDrops.get(i)).applyDrop();
                }
                stage.getActors().removeValue(groundDrops.get(i), true);
                pickedDrop = groundDrops.get(i);
                contadorDrops++;
                groundDrops.removeIndex(i);
                return true;
            }
        }
        return false;
    }


    private void createEnemy(int whichEnemy) {
        if (numSeconds % 3 != 0) {
            enemyToBeCreated = true;
        } else if (numSeconds % 3 == 0 && enemyToBeCreated == true) {
            Enemy enemy = null;
            switch (whichEnemy) {
                case AvailableActions.CREATE_GOLEM:
                    enemy = new Golem(game, Golem.baseAttack * game.multiplier,Golem.baseDefense * game.multiplier, Golem.baseAttackSpeed * game.multiplier, Golem.baseHp * game.multiplier, Golem.baseMovementSpeed * game.multiplier, Golem.baseRange * game.multiplier,stage);
                    enemyAL.add(enemy);
                    break;
                case AvailableActions.CREATE_SWORD_ZOMBIE:
                    enemy = new SwordZombie(game, SwordZombie.baseAttack * game.multiplier,SwordZombie.baseDefense * game.multiplier, SwordZombie.baseAttackSpeed * game.multiplier, SwordZombie.baseHp * game.multiplier, SwordZombie.baseMovementSpeed * game.multiplier, SwordZombie.baseRange * game.multiplier,stage);
                    enemyAL.add(enemy);
                    break;
                case AvailableActions.CREATE_WIZARD_ZOMBIE:
                    enemy = new WizardZombie(game, WizardZombie.baseAttack * game.multiplier,WizardZombie.baseDefense * game.multiplier, WizardZombie.baseAttackSpeed * game.multiplier, WizardZombie.baseHp * game.multiplier, WizardZombie.baseMovementSpeed * game.multiplier, WizardZombie.baseRange * game.multiplier,stage);
                    enemyAL.add(enemy);
                    break;
            }
            stage.addActor(enemy);
            MapGenerator.collision.add(enemy.getEnemyRectangle());
            enemyToBeCreated = false;
        }
    }

    private void whichEnemy(int rdm) throws Exception {
        switch (rdm) {
            case 0:
                createEnemy(AvailableActions.CREATE_GOLEM);
                break;
            case 1:
                createEnemy(AvailableActions.CREATE_WIZARD_ZOMBIE);
                break;
            case 2:
                createEnemy(AvailableActions.CREATE_SWORD_ZOMBIE);
                break;
            default:
                throw new Exception("Error! Number out of range (0-2)!");
        }
    }

    public void connectSocket(){
        try {
            socket = IO.socket("http://localhost:3000/");
            socket.connect();
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public void configSocketEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Connected");
                multiplayer = true;
            }
        }).on("socketID", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {

                    id = data.getString("id");
                    game.timmy.setId(id);
                    Gdx.app.log("SocketIO", "My ID: " + id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting ID");
                }
            }
        }).on("getMap", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final JSONObject data = (JSONObject) args[0];
                try {

                    if (data.get("map") != JSONObject.NULL) {
                        //game.mapGenerator = (MapGenerator) deserialize(DatatypeConverter.parseBase64Binary((String)data.get("map")));
                    } else {
                        /*String mapString = DatatypeConverter.printBase64Binary(serialize(game.mapGenerator));
                        data.put("map", mapString);
                        socket.emit("newMap", data);*/
                    }
                } catch (JSONException e) {// | IOException | ClassNotFoundException e) {
                    Gdx.app.log("SocketIO", "Error getting map. Error: " + e.getMessage());
                }
            }
        }).on("newPlayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String playerId = data.getString("id");
                    Gdx.app.log("SocketIO", "New Player Connect: " + playerId);
                    OtherPlayer otherPlayer = new OtherPlayer(game);//, game.timmy.getAttack(), game.timmy.getDefense(), game.timmy.getAttack_speed(), game.timmy.getHp(), game.timmy.getMovement_speed());
                    otherPlayer.setId(playerId);
                    //double attack, double defense, double attack_speed, double hp, double movement_speed

                    otherPlayers.put(playerId, otherPlayer);
                    //stage.addActor(otherPlayer);

                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting New PlayerID");
                }
            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String playerId = data.getString("id");
                    stage.getActors().removeValue(otherPlayers.get(playerId), true);
                    otherPlayers.remove(playerId);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting disconnected PlayerID");
                }
            }
        }).on("playerMoved", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String playerId = data.getString("id");
                    Double x = data.getDouble("x");
                    Double y = data.getDouble("y");
                    if (otherPlayers.get(playerId) != null) {
                        OtherPlayer otherPlayer = otherPlayers.get(playerId);
                        otherPlayer.setPosition(x.floatValue(), y.floatValue());
                        otherPlayer.setDirection(data.getInt("direction"));

                        switch (otherPlayer.getDirection()) {
                            case AvailableActions.LOOK_UP:
                                System.out.println("Ha entrado a look up");
                                otherPlayer.goMove(AvailableActions.MOVE_UP);
                                break;
                            case AvailableActions.LOOK_DOWN:
                                otherPlayer.goMove(AvailableActions.MOVE_DOWN);
                                break;
                            case AvailableActions.LOOK_LEFT:
                                otherPlayer.goMove(AvailableActions.MOVE_LEFT);
                                break;
                            case AvailableActions.LOOK_RIGHT:
                                otherPlayer.goMove(AvailableActions.MOVE_RIGHT);
                                break;
                        }
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting a player position.");
                }
            }
        }).on("initial", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String playerId = data.getString("id");
                    Double x = data.getDouble("x");
                    Double y = data.getDouble("y");
                    if (otherPlayers.get(playerId) != null) {
                        otherPlayers.get(playerId).setPosition(x.floatValue(), y.floatValue());
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error setting initial player position.");
                }
            }
        }).on("getPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = (JSONArray) args[0];

                try {
                    for (int i = 0; i < objects.length(); i++) {
                        OtherPlayer coopPlayer = new OtherPlayer(game);
                        coopPlayer.setId(objects.getJSONObject(i).getString("id"));
                        Vector2 position = new Vector2();
                        position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
                        position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
                        coopPlayer.setPosition(position.x, position.y);

                        otherPlayers.put(coopPlayer.getId(), coopPlayer);

                        //stage.addActor(coopPlayer);
                    }
                } catch (JSONException e) {
                    Gdx.app.log("ERROR_ARRAY", "Error getting getPlayers Array... Error: " + e.getMessage());
                }
            }
        }).on("getMonsters", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = (JSONArray) args[0];

                JSONObject object;
                Enemy enemy = null;
                float multiplier;
                try {
                    for (int i = 0; i < objects.length(); i++) {
                        object = objects.getJSONObject(i);
                        multiplier = ((Double) object.getDouble("multiplier")).floatValue();
                        switch (object.getInt("whichEnemyId")) {
                            case 0:
                                enemy = new Golem(game);
                                break;
                            case 1:
                                enemy = new WizardZombie(game);
                                break;
                            case 2:
                                enemy = new SwordZombie(game);
                                break;
                        }
                        if (enemy != null) {
                            enemy.setPosition(((Double) object.getDouble("x")).floatValue(), ((Double) object.getDouble("y")).floatValue());
                            enemy.setMultiplier(multiplier);
                            enemy.setId(object.getLong("id"));
                            initializableMonsters.add(enemy);
                        }
                    }
                } catch (JSONException e) {
                    Gdx.app.log("ERROR_ARRAY", "Error getting monsters Array... Error: " + e.getMessage());
                }


            }
        }).on("updateMonsters", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = ((JSONObject) args[0]).getJSONArray("enemy");
                JSONObject object;
                try {
                    for (int i = 0; i < objects.length(); i++) {
                        object = objects.getJSONObject(i);
                        for(Enemy enemy : enemyAL){
                            if(object.getLong("id") == enemy.getId()){
                                enemy.setX(((Double)object.getDouble("x")).floatValue());
                                enemy.setY(((Double)object.getDouble("y")).floatValue());
                                if(enemy.getVidaActual() > object.getDouble("vidaActual")) {
                                    enemy.setVidaActual(object.getDouble("vidaActual"));
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    Gdx.app.log("ERROR_ARRAY", "Error getting monsters Array to update... Error: " + e.getMessage());
                }


            }
        }).on("enemiesDead", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = ((JSONObject) args[0]).getJSONArray("enemy");
                JSONObject object;
                try {
                    for (int i = 0; i < objects.length(); i++) {
                        object = objects.getJSONObject(i);
                        for(int j = 0; j < enemyAL.size; j++){
                            if(object.getLong("id") == enemyAL.get(j).getId()){
                                stage.getActors().removeValue(enemyAL.get(j), true);
                                enemyAL.removeValue(enemyAL.get(j), true);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Gdx.app.log("ERROR_ARRAY", "Error getting monsters Array to update... Error: " + e.getMessage());
                }


            }
        }).on("createMonster", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONObject object = (JSONObject) args[0];
                Enemy enemy = null;
                float multiplier;
                try {
                    multiplier = ((Double) object.getDouble("multiplier")).floatValue();
                    switch (object.getInt("whichEnemyId")) {
                        case 0:
                            enemy = new Golem(game);
                            break;
                        case 1:
                            enemy = new WizardZombie(game);
                            break;
                        case 2:
                            enemy = new SwordZombie(game);
                            break;
                    }
                    if (enemy != null) {
                        enemy.setPosition(((Double) object.getDouble("x")).floatValue(), ((Double) object.getDouble("y")).floatValue());
                        enemy.setMultiplier(multiplier);
                        enemy.setId(object.getLong("id"));
                        initializableMonsters.add(enemy);
                        /*enemyAL.add(enemy);
                        stage.addActor(enemy);*/
                    }
                } catch (JSONException e) {
                    Gdx.app.log("ERROR_newMonster", "Error getting new monster created... Error: " + e.getMessage());
                }
            }
        }).on("newDrop", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONObject object = (JSONObject) args[0];
                Drop drop = null;
                try {
                    int range = object.getInt("range") + 1;
                    if(range <= MSBoost.staticMax_Range){
                        drop = new MSBoost(game);
                    }else if(range >= ASBoost.staticMin_Range && range <= ASBoost.staticMax_Range){
                        drop = new ASBoost(game);
                    }else if(range >= ATKBoost.staticMin_Range && range <= ATKBoost.staticMax_Range){
                        drop = new ATKBoost(game);
                    }else if(range >= MSReduce.staticMin_Range && range <= MSReduce.staticMax_Range){
                        drop = new MSReduce(game);
                    }else if(range >= ATKReduce.staticMin_Range && range <= ATKReduce.staticMax_Range){
                        drop = new ATKReduce(game);
                    }else if(range >= ASReduce.staticMin_Range && range <= ASReduce.staticMax_Range){
                        drop = new ASReduce(game);
                    }else if(range >= MobBoost.staticMin_Range && range <= MobBoost.staticMax_Range){
                        drop = new MobBoost(game);
                    }
                    if(drop != null){
                        drop.setPosition(((Double) object.getDouble("x")).floatValue(), ((Double) object.getDouble("y")).floatValue());
                        drop.setId(object.getLong("id"));
                        initializableDrops.add(drop);
                    }
                } catch (JSONException e) {
                    Gdx.app.log("ERROR_newMonster", "Error getting new monster created... Error: " + e.getMessage());
                }
            }
        }).on("bulletShot", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONObject object = (JSONObject) args[0];
                try{
                Bullet bullet = new Bullet(object.getDouble("angle"), object.getDouble("x"), object.getDouble("y"), stage, object.getDouble("range"), false);
                initializableBullets.add(bullet);
                } catch (JSONException e) {
                    Gdx.app.log("ERROR_newMonster", "Error getting new monster created... Error: " + e.getMessage());
                }
            }
        }).on("asReduce", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                new ASReduce(game).applyDrop();
            }
        }).on("atkReduce", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                new ATKReduce(game).applyDrop();
            }
        }).on("msReduce", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("movement Speed reduce!");
                new MSReduce(game).applyDrop();
            }
        }).on("mobBoost", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                new MobBoost(game).applyDrop();
            }
        }).on("pickedADrop", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    long dropId = data.getLong("dropId");
                    for(int i = groundDrops.size - 1; i >= 0; i--){
                        if(groundDrops.get(i).getId() == dropId){
                            stage.getActors().removeValue(groundDrops.get(i), true);
                            groundDrops.removeIndex(i);
                        }
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting disconnected PlayerID");
                }
            }
        });
    }

    public static float getDistance(Enemy enemy, Playable player){
        Vector2 source = new Vector2(player.getX(), player.getY());
        Vector2 target = new Vector2(enemy.getX(), enemy.getY());
        return (float)Math.sqrt(Math.pow((source.x - target.x), 2) + Math.pow((source.y - target.y), 2));
    }

    public byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
}
