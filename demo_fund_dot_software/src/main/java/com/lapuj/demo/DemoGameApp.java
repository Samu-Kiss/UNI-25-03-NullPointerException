package com.lapuj.demo;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import javafx.scene.input.KeyCode;


import static com.almasb.fxgl.dsl.FXGLForKtKt.getInput;

public class DemoGameApp extends GameApplication {

    private Entity player;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Demo Game App");
        settings.setWidth(800);
        settings.setHeight(600);
    }

    @Override
    protected void initGame() {
        FXGL.getGameWorld().addEntityFactory(new SimpleFactory());

        player = FXGL.spawn("player");
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                player.translateX(5);
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                player.translateX(-5);
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                player.translateY(-5);
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                player.translateY(5);
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Shoot") {
            @Override
            protected void onAction() {
                FXGL.spawn("some", player.getPosition());
            }
        }, KeyCode.SPACE);
    }

    public static void main(String[] args) {
        launch(args);
    }
}