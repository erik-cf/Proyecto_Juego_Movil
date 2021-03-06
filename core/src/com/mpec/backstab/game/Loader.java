package com.mpec.backstab.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.mpec.backstab.api.ApiTools;
import com.mpec.backstab.drops.ASBoost;
import com.mpec.backstab.drops.ASReduce;
import com.mpec.backstab.drops.ATKBoost;
import com.mpec.backstab.drops.ATKReduce;
import com.mpec.backstab.drops.MSBoost;
import com.mpec.backstab.drops.MSReduce;
import com.mpec.backstab.drops.MobBoost;
import com.mpec.backstab.enemy_character.Golem;
import com.mpec.backstab.enemy_character.SwordZombie;
import com.mpec.backstab.enemy_character.WizardZombie;

import org.json.JSONArray;
import org.json.JSONObject;

public class Loader implements Screen {

    JSONObject getter;
    JSONArray jsonArray;
    String name;
    String str = "Loading data...";
    boolean loading = false;
    public static boolean finish = false;
    BitmapFont font;

    final Backstab game;
    public Loader(Backstab game) {
        this.game = game;

        font = new BitmapFont();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(!loading){
            loading = true;
            try {
                getter = ApiTools.InfoRequest("Enemy");
                jsonArray = getter.getJSONArray("enemy");
                for(int i = 0; i < jsonArray.length(); i++) {
                    getter = jsonArray.getJSONObject(i);
                    name = getter.getString("name");
                    if(name.equalsIgnoreCase("golem")){
                        str = "Loading Golem data";
                        Golem.baseAttack = getter.getDouble("attack");
                        Golem.baseDefense = getter.getDouble("defense");
                        Golem.baseHp = getter.getDouble("hp");
                        Golem.baseMovementSpeed = getter.getDouble("movement_speed");
                        Golem.baseRange = getter.getDouble("range");
                        Golem.baseAttackSpeed = getter.getDouble("attack_speed");
                    }else if(name.equalsIgnoreCase("Zombie-Wizard")){
                        str = "Loading Zombie-Wizard data";
                        WizardZombie.baseAttack = getter.getDouble("attack");
                        WizardZombie.baseDefense = getter.getDouble("defense");
                        WizardZombie.baseHp = getter.getDouble("hp");
                        WizardZombie.baseMovementSpeed = getter.getDouble("movement_speed");
                        WizardZombie.baseRange = getter.getDouble("range");
                        WizardZombie.baseAttackSpeed = getter.getDouble("attack_speed");
                    }else if(name.equalsIgnoreCase("Melee-Zombie")){
                        str = "Loading Melee-Zombie data";
                        SwordZombie.baseAttack = getter.getDouble("attack");
                        SwordZombie.baseDefense = getter.getDouble("defense");
                        SwordZombie.baseHp = getter.getDouble("hp");
                        SwordZombie.baseMovementSpeed = getter.getDouble("movement_speed");
                        SwordZombie.baseRange = getter.getDouble("range");
                        SwordZombie.baseAttackSpeed = getter.getDouble("attack_speed");
                    }
                }
                str = "Loading Timmy data";
                getter = ApiTools.InfoRequest("MainCharacter/Timmy").getJSONArray("mainCharacter").getJSONObject(0);
                game.timmy.setAttack(getter.getDouble("attack"));
                game.timmy.setDefense(getter.getDouble("defense"));
                game.timmy.setHp(getter.getDouble("hp"));
                game.timmy.setMovement_speed(getter.getDouble("movement_speed"));
                game.timmy.setAttack_speed(getter.getDouble("attack_speed"));
                game.timmy.setRange(getter.getDouble("range"));
                game.timmy.setVidaActual(game.timmy.getHp());

                str = "Loading Drops data";
                getter = ApiTools.InfoRequest("DropsData");
                jsonArray = getter.getJSONArray("dropsData");
                for(int i = 0; i < jsonArray.length(); i++) {
                    getter = jsonArray.getJSONObject(i);
                    name = getter.getString("name");
                    if(name.equalsIgnoreCase("movement_speed_boost")){
                        str = "Loading Movement Speed Boost";
                        MSBoost.staticName = name;
                        MSBoost.staticValue = getter.getInt("value");
                        MSBoost.staticDuration = getter.getInt("duration");
                        MSBoost.staticMin_Range = getter.getInt("min_range");
                        MSBoost.staticMax_Range = getter.getInt("max_range");
                    }else if(name.equalsIgnoreCase("attack_speed_boost")){
                        str = "Loading Attack Speed Boost";
                        ASBoost.staticName = name;
                        ASBoost.staticValue = getter.getInt("value");
                        ASBoost.staticDuration = getter.getInt("duration");
                        ASBoost.staticMin_Range = getter.getInt("min_range");
                        ASBoost.staticMax_Range = getter.getInt("max_range");
                    }else if(name.equalsIgnoreCase("attack_boost")){
                        str = "Loading Attack Boost";
                        ATKBoost.staticName = name;
                        ATKBoost.staticValue = getter.getInt("value");
                        ATKBoost.staticDuration = getter.getInt("duration");
                        ATKBoost.staticMin_Range = getter.getInt("min_range");
                        ATKBoost.staticMax_Range = getter.getInt("max_range");
                    }else if(name.equalsIgnoreCase("reduce_movement_speed")){
                        str = "Loading Movement Speed Reduce";
                        MSReduce.staticName = name;
                        MSReduce.staticValue = getter.getInt("value");
                        MSReduce.staticDuration = getter.getInt("duration");
                        MSReduce.staticMin_Range = getter.getInt("min_range");
                        MSReduce.staticMax_Range = getter.getInt("max_range");
                    }else if(name.equalsIgnoreCase("reduce_attack")){
                        str = "Loading Reduce Attack";
                        ATKReduce.staticName = name;
                        ATKReduce.staticValue = getter.getInt("value");
                        ATKReduce.staticDuration = getter.getInt("duration");
                        ATKReduce.staticMin_Range = getter.getInt("min_range");
                        ATKReduce.staticMax_Range = getter.getInt("max_range");
                    }else if(name.equalsIgnoreCase("reduce_attack_speed")){
                        str = "Loading Reduce Attack Speed";
                        ASReduce.staticName = name;
                        ASReduce.staticValue = getter.getInt("value");
                        ASReduce.staticDuration = getter.getInt("duration");
                        ASReduce.staticMin_Range = getter.getInt("min_range");
                        ASReduce.staticMax_Range = getter.getInt("max_range");
                    }else if(name.equalsIgnoreCase("mobs_damage_boost")){
                        str = "Loading Mobs Damage Boost";
                        MobBoost.staticName = name;
                        MobBoost.staticValue = getter.getInt("value");
                        MobBoost.staticDuration = getter.getInt("duration");
                        MobBoost.staticMin_Range = getter.getInt("min_range");
                        MobBoost.staticMax_Range = getter.getInt("max_range");
                    }
                }

                finish = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(finish){
            game.setScreen(new GameScreen(game));
        }
        game.batch.begin();
        font.draw(game.batch, "Loading data...", (int)(Gdx.graphics.getWidth() / 2 - str.length() / 2),Gdx.graphics.getHeight() / 2 - 3);
        game.batch.end();
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

    }
}
