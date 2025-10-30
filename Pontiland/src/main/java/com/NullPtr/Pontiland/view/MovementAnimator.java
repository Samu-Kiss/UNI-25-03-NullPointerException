package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.controllers.LanzamientoDadosController;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/** Encapsula la animación de mover una Spatial a lo largo de una lista de objetivos con salto. */
public class MovementAnimator {
  private Spatial mvSpatial;
  private RigidBodyControl mvRb;
  private final List<Vector3f> mvTargets = new ArrayList<>();
  private final List<Integer> mvIndices = new ArrayList<>();
  private int mvSeg = -1;
  private float mvTimer = 0f;
  private float mvDuration = 0.45f;
  private float mvJump = 0.9f;
  private final Vector3f mvStart = new Vector3f();

  private final LanzamientoDadosController lanzamientoController;
  private final CameraController cameraController;

  public MovementAnimator(
      LanzamientoDadosController lanzamientoController, CameraController cameraController) {
    this.lanzamientoController = lanzamientoController;
    this.cameraController = cameraController;
  }

  public void update(float tpf) {
    if (mvSeg >= 0 && mvSeg < mvTargets.size() && mvSpatial != null) {
      mvTimer += tpf;
      float t = Math.min(1f, mvTimer / mvDuration);

      Vector3f target = mvTargets.get(mvSeg);
      float nx = mvStart.x + (target.x - mvStart.x) * t;
      float nz = mvStart.z + (target.z - mvStart.z) * t;
      float ny = mvStart.y + (float) Math.sin(Math.PI * t) * mvJump;

      Vector3f pos = new Vector3f(nx, ny, nz);

      if (mvRb != null) {
        mvRb.setPhysicsLocation(pos);
        mvRb.setLinearVelocity(Vector3f.ZERO);
        mvRb.setAngularVelocity(Vector3f.ZERO);
      }
      mvSpatial.setLocalTranslation(pos);

      Object jid = mvSpatial.getUserData("jugadorId");
      if (jid instanceof Integer) {
        cameraController.focusOnFicha((Integer) jid);
      }

      if (t >= 1f) {
        if (mvRb != null) {
          mvRb.setPhysicsLocation(target);
          mvRb.setLinearVelocity(Vector3f.ZERO);
          mvRb.setAngularVelocity(Vector3f.ZERO);
        }
        mvSpatial.setLocalTranslation(target);

        Integer finalIdx = mvIndices.get(mvSeg);
        mvSpatial.setUserData("cellIndex", finalIdx);

        mvSeg++;
        mvTimer = 0f;
        if (mvSeg < mvTargets.size()) {
          mvStart.set(target);
        } else {
          mvSeg = -1;
          lanzamientoController.enableThrow(true);
        }
      }
    }
  }

  public void animateMoveAlongPath(
      Spatial s,
      RigidBodyControl rb,
      List<Vector3f> targets,
      List<Integer> indices,
      float durationSeconds,
      float jumpHeight) {
    this.mvSpatial = s;
    this.mvRb = rb;

    this.mvTargets.clear();
    this.mvTargets.addAll(targets);

    this.mvIndices.clear();
    this.mvIndices.addAll(indices);

    this.mvDuration = durationSeconds;
    this.mvJump = jumpHeight;

    Vector3f start =
        (rb != null && rb.getPhysicsLocation() != null)
            ? rb.getPhysicsLocation()
            : s.getLocalTranslation();
    this.mvStart.set(start);

    this.mvSeg = (targets.isEmpty() ? -1 : 0);
    this.mvTimer = 0f;
  }

  public boolean isAnimating() {
    return mvSeg >= 0;
  }
}
