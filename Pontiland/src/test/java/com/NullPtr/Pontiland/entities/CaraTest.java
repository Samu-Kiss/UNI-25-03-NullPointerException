package com.NullPtr.Pontiland.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.jme3.math.Vector3f;
import org.junit.jupiter.api.Test;

/**
 * Clase de pruebas unitarias para {@link Cara}.
 *
 * <p>Estas pruebas verifican que la clase {@code Cara} inicializa correctamente sus valores,
 * mantiene las referencias adecuadas y diferencia correctamente entre instancias distintas.
 */
class CaraTest {

  /**
   * Prueba que el constructor de {@link Cara} almacena correctamente el valor y la normal, y que
   * los getters devuelven exactamente lo que fue asignado.
   */
  @Test
  void testConstructorAndGetters() {
    byte valor = 5;
    Vector3f normal = new Vector3f(1, 0, 0);

    Cara cara = new Cara(valor, normal);

    assertEquals(valor, cara.getValor());
    assertEquals(normal, cara.getNormal());
  }

  /**
   * Verifica que la referencia al objeto {@link Vector3f} pasada al constructor se almacena sin
   * realizar una copia. Esto asegura que la clase mantiene la misma referencia.
   */
  @Test
  void testNormalReference() {
    Vector3f normal = new Vector3f(0, 1, 0);
    Cara cara = new Cara((byte) 2, normal);

    assertSame(normal, cara.getNormal());
  }

  /**
   * Prueba que dos instancias distintas de {@link Cara} con valores y normales diferentes no sean
   * consideradas iguales al comparar sus atributos mediante los getters.
   */
  @Test
  void testDifferentValues() {
    Cara cara1 = new Cara((byte) 1, new Vector3f(1, 1, 1));
    Cara cara2 = new Cara((byte) 2, new Vector3f(2, 2, 2));

    assertNotEquals(cara1.getValor(), cara2.getValor());
    assertNotEquals(cara1.getNormal(), cara2.getNormal());
  }
}
