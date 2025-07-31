package com.lapuj.demo;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SimpleFactory implements EntityFactory {
    @Spawns("some")
    public Entity newEnemy(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view(new Rectangle(20, 20, Color.RED))
                //.with(new ProjectileComponent(new Point2D(1, 0), 150))
                .build();
    }
    @Spawns("player")
    public Entity newPlayer(SpawnData data)
    {
        return FXGL.entityBuilder()
                .at(100, 100)
                .view(new Rectangle(20, 20, Color.BLUE))
                .buildAndAttach();
    }


}
