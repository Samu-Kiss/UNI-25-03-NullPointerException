package com.NullPtr.Pontiland.view;

import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.math.Vector3f;

/**
 * Controlador responsable de la cámara: objetivos, interpolación y posicionamiento.
 */
public class CameraController {
  private final Camera cam;
  private final Node rootNode;

  private final Vector3f camLookTarget = new Vector3f(0, 0, 0);
  private final Vector3f camLookCurr = new Vector3f(0, 0, 0);
  private final Vector3f camPosTarget = new Vector3f(15, 15, 15);
  private final Vector3f camPosCurr = new Vector3f(15, 15, 15);

  private float camLerpSpeed = 6f;

  public CameraController(Camera cam, Node rootNode) {
    this.cam = cam;
    this.rootNode = rootNode;
  }

  public void setLerpSpeed(float speed) {
    this.camLerpSpeed = speed;
  }

  public void setupCamera() {
    cam.setLocation(new Vector3f(15, 15, 15));
    cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    cam.setFrustumPerspective(60f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 500f);
  }

  public void update(float tpf) {
    float a = Math.min(1f, camLerpSpeed * tpf);
    camPosCurr.interpolateLocal(camPosTarget, a);
    camLookCurr.interpolateLocal(camLookTarget, a);
    cam.setLocation(camPosCurr);
    cam.lookAt(camLookCurr, Vector3f.UNIT_Y);
  }

  public void resetCamera() {
    Vector3f offset = new Vector3f(0, 7, 3);

    Spatial d1 = rootNode.getChild("Dice1");
    if (d1 == null) d1 = rootNode.getChild("FallbackDice1");
    Spatial d2 = rootNode.getChild("Dice2");
    if (d2 == null) d2 = rootNode.getChild("FallbackDice2");

    if (d1 != null && d2 != null) {
      Vector3f mid = d1.getWorldTranslation().add(d2.getWorldTranslation()).multLocal(0.5f);
      setCamTarget(mid, offset);
    }
  }

  public void focusOnFicha(int jugadorId) {
    Spatial s = rootNode.getChild("Ficha_J" + jugadorId);
    if (s != null) {
      Vector3f fichaPos = s.getLocalTranslation();
      setCamTarget(fichaPos, new Vector3f(5, 5, 5));
    }
  }

  public void setCamTarget(Vector3f lookAt, Vector3f offset) {
    camLookTarget.set(lookAt);
    camPosTarget.set(lookAt).addLocal(offset);
  }
}

