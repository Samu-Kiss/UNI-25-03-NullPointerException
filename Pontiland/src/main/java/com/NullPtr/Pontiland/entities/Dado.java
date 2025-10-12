package com.NullPtr.Pontiland.entities;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

public class Dado {
  private List<Cara> caras;
  private Spatial spatial;

  public Dado(Spatial spatial) {
    this.spatial = spatial;
    this.caras = new ArrayList<>();
    // Definir las 6 caras del dado con sus normales y valores
    caras.add(new Cara((byte) 2, new Vector3f(0, 1, 0))); // Arriba
    caras.add(new Cara((byte) 5, new Vector3f(0, -1, 0))); // Abajo
    caras.add(new Cara((byte) 3, new Vector3f(1, 0, 0))); // Derecha
    caras.add(new Cara((byte) 4, new Vector3f(-1, 0, 0))); // Izquierda
    caras.add(new Cara((byte) 1, new Vector3f(0, 0, 1))); // Frente
    caras.add(new Cara((byte) 6, new Vector3f(0, 0, -1))); // Atrás
  }

  public byte getCaraSuperior() {
    Vector3f arribaMundo = new Vector3f(0, 1, 0);
    Cara caraArriba = null;
    float maxDot = -Float.MAX_VALUE;

    for (Cara cara : caras) {
      Vector3f normalTransformada = spatial.getWorldRotation().mult(cara.getNormal());
      float dot = normalTransformada.dot(arribaMundo);
      if (dot > maxDot) {
        maxDot = dot;
        caraArriba = cara;
      }
    }
    return caraArriba != null ? caraArriba.getValor() : -1;
  }

  public void lanzar() {
    RigidBodyControl rb = spatial.getControl(RigidBodyControl.class);

    rb.setLinearVelocity(Vector3f.ZERO);
    rb.setAngularVelocity(Vector3f.ZERO);
    rb.clearForces();
    rb.activate();

    Vector3f n = new Vector3f(0, 5, 0);
    float m = rb.getMass();
    Vector3f impulse = n.mult(1.0f * m);
    impulse.y += 0.5f * m;
    rb.applyImpulse(impulse, Vector3f.ZERO);

    rb.applyTorqueImpulse(new Vector3f(1.0f, 0.7f, 1.2f));
  }

  public boolean enMovimiento() {
    RigidBodyControl control = spatial.getControl(RigidBodyControl.class);
    if (control != null) {
      Vector3f velocidad = control.getLinearVelocity();
      return velocidad.length() > 0.02f;
    }
    return false;
  }
}
