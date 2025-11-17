package com.NullPtr.Pontiland.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test unitario para la clase {@link SavedGame}. Se prueba constructor y validaciones de argumentos
 * nulos.
 */
class SavedGameTest {

  /** Verifica que el constructor funcione correctamente con valores válidos. */
  @Test
  void validConstructor() {
    SavedGame savedGame = new SavedGame("123", "Partida del 16/11");

    assertEquals("123", savedGame.id, "El ID debe coincidir con el valor dado");
    assertEquals(
        "Partida del 16/11", savedGame.titulo, "El título debe coincidir con el valor dado");
  }

  /** Verifica que el constructor lance excepción si el id es nulo. */
  @Test
  void nullId() {
    assertThrows(
        NullPointerException.class,
        () -> new SavedGame(null, "Título válido"),
        "Debe lanzar NullPointerException si el id es null");
  }

  /** Verifica que el constructor lance excepción si el título es nulo. */
  @Test
  void nullTitulo() {
    assertThrows(
        NullPointerException.class,
        () -> new SavedGame("123", null),
        "Debe lanzar NullPointerException si el título es null");
  }
}
