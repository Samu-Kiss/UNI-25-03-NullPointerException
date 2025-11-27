package com.NullPtr.Pontiland.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para la clase {@link Casilla}.
 *
 * <p>Se testean todas las ramas del constructor, getters y setters, incluyendo la validación de
 * parámetros inválidos y el uso del enum {@link Tipo}.
 */
class CasillaTest {

  /** Verifica que el constructor crea correctamente una casilla válida. */
  @Test
  void testConstructorCreatesValidCasilla() {
    Casilla casilla = new Casilla(5, "Salida", Tipo.PARADALIBRE);

    assertEquals(
        5, casilla.getPosicionTablero(), "La posición debe coincidir con la proporcionada");
    assertEquals(
        "Salida", casilla.getNombreCasilla(), "El nombre debe coincidir con el proporcionado");
    assertEquals(
        Tipo.PARADALIBRE, casilla.getTipoCasilla(), "El tipo debe coincidir con el proporcionado");
  }

  /** Verifica que el constructor lanza excepción si la posición es menor a 1. */
  @Test
  void testConstructorThrowsForPositionTooLow() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> new Casilla(0, "Carcel", Tipo.IRALACARCEL));
    assertEquals("Posición de casilla inválida", exception.getMessage());
  }

  /** Verifica que el constructor lanza excepción si la posición es mayor a 40. */
  @Test
  void testConstructorThrowsForPositionTooHigh() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> new Casilla(41, "Carcel", Tipo.IRALACARCEL));
    assertEquals("Posición de casilla inválida", exception.getMessage());
  }

  /** Verifica que el constructor lanza excepción si el nombre de la casilla es nulo. */
  @Test
  void testConstructorThrowsForNullName() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new Casilla(10, null, Tipo.EVENTO));
    assertEquals("Nombre de casilla inválido", exception.getMessage());
  }

  /** Verifica que el constructor lanza excepción si el nombre de la casilla está vacío. */
  @Test
  void testConstructorThrowsForEmptyName() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new Casilla(10, "", Tipo.EVENTO));
    assertEquals("Nombre de casilla inválido", exception.getMessage());
  }

  /** Verifica que el constructor lanza excepción si el tipo de casilla es nulo. */
  @Test
  void testConstructorThrowsForNullTipo() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new Casilla(10, "Estacion", null));
    assertEquals("Tipo de casilla inválido", exception.getMessage());
  }

  /** Verifica que el setter de posición modifica correctamente la posición. */
  @Test
  void testSetPosicionTablero() {
    Casilla casilla = new Casilla(1, "Salida", Tipo.PARADALIBRE);
    casilla.setPosicionTablero(20);

    assertEquals(20, casilla.getPosicionTablero());
  }

  /** Verifica que el setter de tipo modifica correctamente el tipo. */
  @Test
  void testSetTipoCasilla() {
    Casilla casilla = new Casilla(2, "Carcel", Tipo.IRALACARCEL);
    casilla.setTipoCasilla(Tipo.MOVIMIENTO);

    assertEquals(Tipo.MOVIMIENTO, casilla.getTipoCasilla());
  }

  /** Verifica que el setter de nombre modifica correctamente el nombre. */
  @Test
  void testSetNombreCasilla() {
    Casilla casilla = new Casilla(3, "Suerte", Tipo.EVENTO);
    casilla.setNombreCasilla("Caja de Suerte");

    assertEquals("Caja de Suerte", casilla.getNombreCasilla());
  }

  /** Verifica que cada tipo de casilla devuelve correctamente su acción asociada. */
  @Test
  void testTipoEnumGetAction() {
    assertEquals("ParadaLibre", Tipo.PARADALIBRE.getAction());
    assertEquals("Evento", Tipo.EVENTO.getAction());
    assertEquals("Propiedad", Tipo.PROPIEDAD.getAction());
    assertEquals("Movimiento", Tipo.MOVIMIENTO.getAction());
    assertEquals("IrALaCarcel", Tipo.IRALACARCEL.getAction());
  }
}
