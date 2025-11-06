package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.entities.Ficha;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/** Responsable de crear y gestionar las `Ficha` (spatials) en escena. */
public class FichaController {
  private final AssetManager assetManager;
  private final BulletAppState bullet;
  private final Node rootNode;
  private final Vector3f boardFirstPosition;
  private final int totalCasillas;
  private float cellSize;
  private final int side;

  public FichaController(
      AssetManager assetManager,
      BulletAppState bullet,
      Node rootNode,
      Vector3f boardFirstPosition,
      int totalCasillas) {
    this.assetManager = assetManager;
    this.bullet = bullet;
    this.rootNode = rootNode;
    this.boardFirstPosition = boardFirstPosition;
    this.totalCasillas = totalCasillas;
    this.cellSize = 1.5385f;
    this.side = totalCasillas / 4;
  }

  public void loadFichasModels(Ficha[] data) {
    for (int i = 0; i < data.length; i++) {
      Spatial s = assetManager.loadModel(data[i].getRutaFicha());

      int jugadorId = data[i].getJugadorId();
      s.setName("Ficha_J" + jugadorId);
      s.setUserData("jugadorId", jugadorId);

      s.scale(1f);

      Vector3f placed;
      if (i < 2) {
        placed =
            new Vector3f(boardFirstPosition.getX() + i * 0.5f, 0.5f, boardFirstPosition.getZ());
      } else {
        placed =
            new Vector3f(
                boardFirstPosition.getX() + i % 2 * 0.5f, 0.5f, boardFirstPosition.getZ() + 0.5f);
      }

      s.setLocalTranslation(placed);

      s.setUserData("startPosition", placed.clone());

      Vector3f cell0 = posFromCell(0);
      Vector3f initialOffset = placed.subtract(cell0);
      s.setUserData("initialOffset", initialOffset);

      s.setUserData("cellIndex", 0);

      RigidBodyControl rb = new RigidBodyControl(1f);
      s.addControl(rb);
      rb.setPhysicsLocation(placed.clone());
      bullet.getPhysicsSpace().add(rb);
      rootNode.attachChild(s);
    }
  }

  public void replicateFichaPosition(int jugadorId, int casillaIndex, MovementAnimator animator) {
    Spatial s = rootNode.getChild("Ficha_J" + jugadorId);
    if (s == null) return;

    int currentIndex = 0;
    Object ciUd = s.getUserData("cellIndex");
    if (ciUd instanceof Integer) currentIndex = (Integer) ciUd;

    s.setUserData("jugadorId", jugadorId);

    if (casillaIndex < currentIndex) {
      casillaIndex += totalCasillas;
    }
    int steps = (casillaIndex - currentIndex) % totalCasillas;
    if (steps == 0) {
      s.setUserData("cellIndex", casillaIndex);
      return;
    }

    List<Vector3f> targets = new ArrayList<>();
    List<Integer> indices = new ArrayList<>();

    if (steps > 0) {
      for (int i = 1; i <= steps; i++) {
        int idx = (currentIndex + i) % totalCasillas;
        Vector3f center = posFromCell(idx);
        targets.add(center);
        indices.add(idx);
      }
    }

    RigidBodyControl rb = s.getControl(RigidBodyControl.class);
    animator.animateMoveAlongPath(s, rb, targets, indices, 0.45f, 0.9f);
  }

  public Vector3f posFromCell(int c) {
    int idx = (c % totalCasillas);

    int sideLocal = idx / side;
    int pos = idx % side;

    float x = 0f;
    float z = 0f;
    switch (sideLocal) {
      case 0:
        cellSize = 1.5385f;
        x =
            -(pos + 1)
                * cellSize; // lograr llegar a ese pos+1 por mas absurdo que parezca me tomo como 2
        // horas de mi vida
        z = 0;
        break;

        // Left side: bottom -> top
      case 1:
        cellSize = 1.357500558970818f;
        x = -boardFirstPosition.getX() * 2f;
        z = -(((pos + 1) * cellSize) + (pos + 1) * 0.23f);
        break;

        // Top side: left -> right
      case 2:
        cellSize = 1.357500558970818f;
        z = -boardFirstPosition.getZ() * 2f;
        if ((pos + 1) == 0) {
          x = (-boardFirstPosition.getX() * 2f) + ((pos + 1) * cellSize);
          break;
        } else {
          x = (-boardFirstPosition.getX() * 2f) + ((pos + 1) * cellSize) + (pos + 1) * 0.24f;
          break;
        }

        // Right side: top -> bottom
      case 3:
        cellSize = 1.357500558970818f;
        if ((pos + 1) == 0) cellSize *= 2f;
        z = (-boardFirstPosition.getZ() * 2f) + ((pos + 1) * cellSize) + (pos + 1) * 0.24f;
        break;
    }

    return new Vector3f(boardFirstPosition.x + x, boardFirstPosition.y, boardFirstPosition.z + z);
  }
}
