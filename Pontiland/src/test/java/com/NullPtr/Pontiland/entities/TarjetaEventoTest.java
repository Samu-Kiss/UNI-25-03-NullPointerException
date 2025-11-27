package com.NullPtr.Pontiland.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test unitario para la clase {@link TarjetaEvento}. Se prueba constructor, validaciones y
 * asignación de acción.
 */
class TarjetaEventoTest {

  /** Verifica que el constructor funcione correctamente con valores válidos. */
  @Test
  void validConstructor() {
    TarjetaEvento tarjeta = new TarjetaEvento("Avanza", "Avanza 3 casillas", Accion.GANA_50);

    assertNotNull(tarjeta, "La tarjeta no debe ser null");
  }

  /** Verifica que el constructor lance excepción si el nombre es nulo o vacío. */
  @Test
  void invalidNombre() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new TarjetaEvento(null, "Descripción", Accion.GANA_50),
        "Debe lanzar excepción si el nombre es null");

    assertThrows(
        IllegalArgumentException.class,
        () -> new TarjetaEvento("", "Descripción", Accion.GANA_50),
        "Debe lanzar excepción si el nombre está vacío");
  }

  /** Verifica que el constructor lance excepción si la descripción es nula o vacía. */
  @Test
  void invalidDescripcion() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new TarjetaEvento("Nombre", null, Accion.GANA_50),
        "Debe lanzar excepción si la descripción es null");

    assertThrows(
        IllegalArgumentException.class,
        () -> new TarjetaEvento("Nombre", "", Accion.GANA_50),
        "Debe lanzar excepción si la descripción está vacía");
  }
}
