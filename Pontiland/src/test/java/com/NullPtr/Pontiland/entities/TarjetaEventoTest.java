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
    TarjetaEvento tarjeta = new TarjetaEvento("Avanza", "Avanza 3 casillas");

    assertNotNull(tarjeta, "La tarjeta no debe ser null");
    // No hay getters para nombre y descripción, solo verificamos que el objeto se creó
  }

  /** Verifica que el constructor lance excepción si el nombre es nulo o vacío. */
  @Test
  void invalidNombre() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new TarjetaEvento(null, "Descripción"),
        "Debe lanzar excepción si el nombre es null");

    assertThrows(
        IllegalArgumentException.class,
        () -> new TarjetaEvento("", "Descripción"),
        "Debe lanzar excepción si el nombre está vacío");
  }

  /** Verifica que el constructor lance excepción si la descripción es nula o vacía. */
  @Test
  void invalidDescripcion() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new TarjetaEvento("Nombre", null),
        "Debe lanzar excepción si la descripción es null");

    assertThrows(
        IllegalArgumentException.class,
        () -> new TarjetaEvento("Nombre", ""),
        "Debe lanzar excepción si la descripción está vacía");
  }

  /** Verifica que se pueda asignar correctamente una acción a la tarjeta. */
  @Test
  void setAccionValid() {
    TarjetaEvento tarjeta = new TarjetaEvento("Avanza", "Avanza 3 casillas");

    tarjeta.setAccion("GANA_50"); // Ahora usamos un valor existente del enum Accion
    assertEquals(
        Accion.GANA_50, tarjeta.getAccion(), "La acción asignada debe coincidir con la esperada");
  }

  /** Verifica que se lance excepción si se intenta asignar una acción inválida. */
  @Test
  void setAccionInvalid() {
    TarjetaEvento tarjeta = new TarjetaEvento("Avanza", "Avanza 3 casillas");

    assertThrows(
        IllegalArgumentException.class,
        () -> tarjeta.setAccion("INEXISTENTE"),
        "Debe lanzar excepción si la acción no existe en el enum Accion");
  }
}
