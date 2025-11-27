package com.NullPtr.Pontiland.entities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para la clase {@link Dado}.
 *
 * <p>Se testean todas las ramas principales de los métodos, usando mocks para Spatial y
 * RigidBodyControl de JME3.
 */
class DadoTest {

  private Spatial spatialMock;
  private RigidBodyControl rigidBodyMock;
  private Dado dado;

  @BeforeEach
  void setUp() {
    spatialMock = mock(Spatial.class);
    rigidBodyMock = mock(RigidBodyControl.class);
    when(spatialMock.getControl(RigidBodyControl.class)).thenReturn(rigidBodyMock);

    // Por defecto, la rotación del spatial será identidad
    when(spatialMock.getWorldRotation()).thenReturn(new Quaternion());

    dado = new Dado(spatialMock);
  }

  /** Verifica que getCaraSuperior devuelve un valor entre 1 y 6. */
  @Test
  void testGetCaraSuperiorReturnsValidValue() {
    byte cara = dado.getCaraSuperior();
    assertTrue(cara >= 1 && cara <= 6, "La cara superior debe estar entre 1 y 6");
  }

  /** Verifica que lanzar invoca los métodos esperados de RigidBodyControl. */
  @Test
  void testLanzarAplicaImpulsos() {
    dado.lanzar();

    verify(rigidBodyMock).setLinearVelocity(Vector3f.ZERO);
    verify(rigidBodyMock).setAngularVelocity(Vector3f.ZERO);
    verify(rigidBodyMock).clearForces();
    verify(rigidBodyMock).activate();
    verify(rigidBodyMock).applyImpulse(any(Vector3f.class), eq(Vector3f.ZERO));
    verify(rigidBodyMock).applyTorqueImpulse(any(Vector3f.class));
  }

  /** Verifica que enMovimiento devuelve true cuando la velocidad es mayor que 0.02f. */
  @Test
  void testEnMovimientoTrue() {
    when(rigidBodyMock.getLinearVelocity()).thenReturn(new Vector3f(0.1f, 0, 0));
    assertTrue(dado.enMovimiento());
  }

  /** Verifica que enMovimiento devuelve false cuando la velocidad es menor o igual a 0.02f. */
  @Test
  void testEnMovimientoFalse() {
    when(rigidBodyMock.getLinearVelocity()).thenReturn(new Vector3f(0.01f, 0, 0));
    assertFalse(dado.enMovimiento());
  }

  /** Verifica que enMovimiento devuelve false si no hay RigidBodyControl. */
  @Test
  void testEnMovimientoWithoutRigidBodyControl() {
    when(spatialMock.getControl(RigidBodyControl.class)).thenReturn(null);
    assertFalse(dado.enMovimiento());
  }
}
