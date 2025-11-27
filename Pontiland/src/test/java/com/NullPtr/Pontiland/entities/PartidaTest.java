package com.NullPtr.Pontiland.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Test unitario para la clase {@link Partida}. Se prueba el constructor. */
class PartidaTest {

  /** Verifica que el constructor funcione correctamente con valores válidos. */
  @Test
  void testConstructorValido() {
    assertDoesNotThrow(
        () -> new Partida("MiPartida", 2),
        "El constructor no debe lanzar excepción con valores válidos");
  }
}
