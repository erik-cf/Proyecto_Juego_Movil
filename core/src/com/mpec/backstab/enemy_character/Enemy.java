package com.mpec.backstab.enemy_character;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mpec.backstab.game.Backstab;

public class Enemy {

    protected Sprite enemySprite;
    protected Rectangle enemyRectangle;
    protected TextureAtlas enemyAtlas;
    protected Animation<TextureRegion> enemyAnimation;
    protected int direction;

    private double attack;
    private double defense;
    private double attack_speed;
    private double hp;
    private double movement_speed;
    private double range;

    final Backstab game;

    public Enemy(Backstab game){
        this.game = game;
    }

    public Enemy(Backstab game, double attack, double defense, double attack_speed, double hp, double movement_speed) {
        this.game = game;
        this.attack = attack;
        this.defense = defense;
        this.attack_speed = attack_speed;
        this.hp = hp;
        this.movement_speed = movement_speed;
    }



    public double getAttack() {
        return attack;
    }

    public void setAttack(double attack) {
        this.attack = attack;
    }

    public double getDefense() {
        return defense;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

    public double getAttack_speed() {
        return attack_speed;
    }

    public void setAttack_speed(double attack_speed) {
        this.attack_speed = attack_speed;
    }

    public double getHp() {
        return hp;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public double getMovement_speed() {
        return movement_speed;
    }

    public void setMovement_speed(double movement_speed) {
        this.movement_speed = movement_speed;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public Sprite getEnemySprite() {
        return enemySprite;
    }

    public void setEnemySprite(Sprite enemySprite) {
        this.enemySprite = enemySprite;
    }

    public Rectangle getEnemyRectangle() {
        return enemyRectangle;
    }

    public void setEnemyRectangle(Rectangle enemyRectangle) {
        this.enemyRectangle = enemyRectangle;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}