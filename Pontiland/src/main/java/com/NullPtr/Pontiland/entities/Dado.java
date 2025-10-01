package com.NullPtr.Pontiland.entities;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Matrix3f;
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
        caras.add(new Cara((byte)2, new Vector3f(0, 1, 0))); // Arriba
        caras.add(new Cara((byte)5, new Vector3f(0, -1, 0))); // Abajo
        caras.add(new Cara((byte)3, new Vector3f(1, 0, 0))); // Derecha
        caras.add(new Cara((byte)4, new Vector3f(-1, 0, 0))); // Izquierda
        caras.add(new Cara((byte)1, new Vector3f(0, 0, 1))); // Frente
        caras.add(new Cara((byte)6, new Vector3f(0, 0, -1))); // Atrás
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

    public void moverDado(){
        RigidBodyControl control = spatial.getControl(RigidBodyControl.class);
        control.applyImpulse(new Vector3f(0, 5, 0), Vector3f.ZERO);
        control.applyTorqueImpulse(new Vector3f((float)Math.random()*0.4f, (float)Math.random()*0.1f, (float)Math.random()*0.2f));
        //spatial.setLocalTranslation(0f, 10f, 0f);
        spatial.setLocalRotation(new Matrix3f((float)Math.random(), (float)Math.random(), (float)Math.random(),
                                        (float)Math.random(), 0, 0,
                                        (float)Math.random(), 0, (float)Math.random()));
        control.activate();

    }

    public boolean estaMoviendose() {
        RigidBodyControl control = spatial.getControl(RigidBodyControl.class);
        if (control != null) {
            Vector3f velocidad = control.getLinearVelocity();
            return velocidad.length() > 0.02f; // Umbral para considerar que se está moviendo
        }
        return false;
    }
}