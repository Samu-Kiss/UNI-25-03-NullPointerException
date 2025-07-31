package com.lapuj.demo;


import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.Camera3D;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.TransformComponent;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Point3D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import static com.almasb.fxgl.dsl.FXGL.*;

public class DemoGame3d extends GameApplication {

    private Entity player;
    private final double MOVE_SPEED = 0.1;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("FXGL 3D Demo");
        settings.setWidth(800);
        settings.setHeight(600);
        settings.set3D(true);
    }

    @Override
    protected void initGame() {
        Box mesh = new Box(1, 1, 1);
        mesh.setMaterial(new PhongMaterial(Color.BLUE));

        player = entityBuilder()
                .at(0, 0, 0)
                .view(mesh)
                .buildAndAttach();

        Camera3D cam = getGameScene().getCamera3D();
        cam.getTransform().setPosition3D(0, 2, -6);
    }

    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Move Forward") {
            @Override protected void onAction() {
                movePlayer(0, -MOVE_SPEED,0);
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Move Backward") {
            @Override protected void onAction() {
                movePlayer(0, MOVE_SPEED, 0);
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Move Left") {
            @Override protected void onAction() {
                movePlayer(-MOVE_SPEED, 0, 0);
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("Move Right") {
            @Override protected void onAction() {
                movePlayer(MOVE_SPEED, 0, 0);
            }
        }, KeyCode.D);
    }

    private void movePlayer(double dx, double dy, double dz) {
        TransformComponent t = player.getTransformComponent();
        Point3D pos = t.getPosition3D();
        t.setPosition3D(pos.add(dx, dy, dz));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
