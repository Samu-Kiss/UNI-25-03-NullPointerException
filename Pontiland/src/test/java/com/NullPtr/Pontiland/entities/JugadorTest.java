package com.NullPtr.Pontiland.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test unitario para la clase {@link Jugador}. Se prueban constructores, getters, setters y
 * validaciones de argumentos inválidos.
 */
class JugadorTest {

  /** Verifica el constructor completo con lista de propiedades. */
  @Test
  void fullConstructorWithPropertiesTest() {
    Propiedad mockProp = Mockito.mock(Propiedad.class);
    List<Propiedad> props = new ArrayList<>();
    props.add(mockProp);

    Jugador jugador = new Jugador(1, "Ignacito", 5, false, 500, props);

    // Verificación de cada atributo
    assertEquals(1, jugador.getJugadorId(), "El jugadorId debe ser 1");
    assertEquals("Ignacito", jugador.getNombreJugador(), "El nombre del jugador debe ser Ignacito");
    assertEquals(5, jugador.getPosicion(), "La posición debe ser 5");
    assertFalse(jugador.getEstado(), "El jugador no debe estar en cárcel");
    assertEquals(500, jugador.getDinero(), "El dinero debe ser 500");
    assertEquals(props, jugador.getPropiedades(), "La lista de propiedades debe coincidir");
  }

  /** Verifica el constructor por defecto. */
  @Test
  void defaultConstructorTest() {
    Jugador jugador = new Jugador(1000, "Pontiland", 2);

    assertEquals(2, jugador.getJugadorId(), "El jugadorId debe ser 2");
    assertEquals("Pontiland", jugador.getNombreJugador(), "El nombre debe ser Pontiland");
    assertEquals(1000, jugador.getDinero(), "El dinero debe ser 1000");
    assertEquals(1, jugador.getPosicion(), "La posición inicial debe ser 1");
    assertFalse(jugador.getEstado(), "El jugador no debe estar en cárcel");
    assertNotNull(jugador.getPropiedades(), "La lista de propiedades no debe ser nula");
    assertTrue(jugador.getPropiedades().isEmpty(), "La lista de propiedades debe estar vacía");
  }

  /** Verifica el constructor completo con todos los parámetros. */
  @Test
  void fullConstructorAllParamsTest() {
    Jugador jugador = new Jugador(3, 2, "Caramelo", 1, 10, true, 500, 123L);

    assertEquals(3, jugador.getJugadorId(), "El jugadorId debe ser 3");
    assertEquals(2, jugador.getNumJugador(), "El numJugador debe ser 2");
    assertEquals("Caramelo", jugador.getNombreJugador(), "El nombre debe ser Caramelo");
    assertEquals(10, jugador.getPosicion(), "La posición debe ser 10");
    assertTrue(jugador.getEstado(), "El jugador debe estar activo");
    assertEquals(500, jugador.getDinero(), "El dinero debe ser 500");
    assertEquals(123L, jugador.getPartida(), "El ID de partida debe ser 123");
  }

  /** Verifica setters y getters con valores válidos. */
  @Test
  void settersAndGettersTest() {
    Jugador jugador = new Jugador(500, "Fantasma", 1);

    jugador.setNombreJugador("NuevoNombre");
    assertEquals("NuevoNombre", jugador.getNombreJugador(), "El nombre del jugador debe actualizarse");

    jugador.setPosicion(15);
    assertEquals(15, jugador.getPosicion(), "La posición debe actualizarse a 15");

    jugador.setEstado(true);
    assertTrue(jugador.getEstado(), "El estado del jugador debe ser true");

    jugador.setDinero(750);
    assertEquals(750, jugador.getDinero(), "El dinero debe actualizarse a 750");

    List<Propiedad> props = new ArrayList<>();
    props.add(Mockito.mock(Propiedad.class));
    jugador.setPropiedades(props);
    assertEquals(props, jugador.getPropiedades(), "La lista de propiedades debe actualizarse");
  }

  /** Verifica que setNombreJugador lance excepción si se pasa null */
  @Test
  void setNombreJugadorNullTest() {
    Jugador jugador = new Jugador(100, "NombreValido", 1);

    IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> jugador.setNombreJugador(null),
                    "Debe lanzar excepción si el nombre es null");
    assertEquals("El nombre del jugador no puede estar vacío", exception.getMessage(),
            "El mensaje de excepción debe coincidir");
  }

  /** Verifica que setJugadorId lance excepción si el ID es menor que 0 */
  @Test
  void setJugadorIdBelowZeroTest() {
    Jugador jugador = new Jugador(100, "NombreValido", 1);

    IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> jugador.setJugadorId((byte) -1),
                    "Debe lanzar excepción si el jugadorId es menor que 0");
    assertEquals("Identificador del jugador es inválido", exception.getMessage(),
            "El mensaje de excepción debe coincidir");
  }

  /** Verifica que setJugadorId lance excepción si el ID es mayor que 4 */
  @Test
  void setJugadorIdAboveFourTest() {
    Jugador jugador = new Jugador(100, "NombreValido", 1);

    IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> jugador.setJugadorId((byte) 5),
                    "Debe lanzar excepción si el jugadorId es mayor que 4");
    assertEquals("Identificador del jugador es inválido", exception.getMessage(),
            "El mensaje de excepción debe coincidir");
  }

  /** Verifica que setJugadorId funcione con valores válidos (0 a 4) */
  @Test
  void setJugadorIdValidTest() {
    Jugador jugador = new Jugador(100, "NombreValido", 1);

    for (byte id = 0; id <= 4; id++) {
      jugador.setJugadorId(id);
      assertEquals(id, jugador.getJugadorId(), "El jugadorId debe establecerse correctamente a " + id);
    }
  }

  /** Verifica que setNombreJugador lance excepción si el nombre es vacío */
  @Test
  void setNombreJugadorEmptyTest() {
    Jugador jugador = new Jugador(100, "NombreValido", 1);

    IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> jugador.setNombreJugador(""),
                    "Debe lanzar excepción si el nombre está vacío");
    assertEquals("El nombre del jugador no puede estar vacío", exception.getMessage(),
            "El mensaje de excepción debe coincidir");
  }

  /** Verifica que setNombreJugador funcione con un nombre válido */
  @Test
  void setNombreJugadorValidTest() {
    Jugador jugador = new Jugador(100, "NombreValido", 1);

    jugador.setNombreJugador("NuevoNombre");
    assertEquals("NuevoNombre", jugador.getNombreJugador(), "El nombre debe actualizarse correctamente");
  }

  /** Verifica que los constructores y setters lancen excepciones con argumentos inválidos. */
  @Test
  void invalidArgumentsTest() {
    Jugador jugador = new Jugador(100, "ValidName", 1);

    // Constructor por defecto inválido
    assertThrows(IllegalArgumentException.class, () -> new Jugador(-1, "Nombre", 1),
            "Debe lanzar excepción si el dinero es negativo");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(100, "", 1),
            "Debe lanzar excepción si el nombre está vacío");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(100, "Nombre", 5),
            "Debe lanzar excepción si el jugadorId está fuera de rango");

    // Constructor completo inválido
    assertThrows(IllegalArgumentException.class, () -> new Jugador(1, 2, "", 1, 10, false, 100, 100L),
            "Debe lanzar excepción si nombre está vacío");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(1, 0, "Nombre", 1, 10, false, 100, 100L),
            "Debe lanzar excepción si numJugador es menor que 1");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(1, 5, "Nombre", 1, 10, false, 100, 100L),
            "Debe lanzar excepción si numJugador es mayor que 4");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(1, 2, "Nombre", 0, 10, false, 100, 100L),
            "Debe lanzar excepción si posición es menor que 1");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(1, 2, "Nombre", 8, 10, false, 100, 100L),
            "Debe lanzar excepción si posición es mayor que 7");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(1, 2, "Nombre", 1, 0, false, 100, 100L),
            "Debe lanzar excepción si dinero es menor que 1");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(1, 2, "Nombre", 1, 10, false, -1, 100L),
            "Debe lanzar excepción si dinero es negativo");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(1, 2, "Nombre", 1, 10, false, 100, -1L),
            "Debe lanzar excepción si partida es negativa");

    // Setters inválidos
    assertThrows(IllegalArgumentException.class, () -> jugador.setNombreJugador(""), "Nombre no puede ser vacío");
    assertThrows(IllegalArgumentException.class, () -> jugador.setPosicion(0), "Posición no puede ser menor que 1");
    assertThrows(IllegalArgumentException.class, () -> jugador.setPosicion(41), "Posición no puede ser mayor que 40");
    assertThrows(IllegalArgumentException.class, () -> jugador.setPropiedades(null), "Lista de propiedades no puede ser null");
    assertThrows(IllegalArgumentException.class, () -> jugador.setJugadorId((byte) -1), "JugadorId no puede ser menor que 0");
    assertThrows(IllegalArgumentException.class, () -> jugador.setJugadorId((byte) 5), "JugadorId no puede ser mayor que 4");
  }

  /** Verifica que el constructor por defecto lanza excepción si jugadorId es menor que 1 o mayor que 4 */
  @Test
  void defaultConstructorJugadorIdFueraDeRango() {
    assertThrows(IllegalArgumentException.class, () -> new Jugador(100, "Jugador", 0),
            "JugadorId no puede ser menor que 1");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(100, "Jugador", 5),
            "JugadorId no puede ser mayor que 4");
  }

  /** Verifica que el constructor por defecto lanza excepción si nombreJugador es null o vacío */
  @Test
  void defaultConstructorNombreJugadorInvalido() {
    assertThrows(IllegalArgumentException.class, () -> new Jugador(100, null, 1),
            "El nombre del jugador no puede ser null");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(100, "", 1),
            "El nombre del jugador no puede estar vacío");
  }

  /** Verifica que setNombreJugador lanza excepción si se pasa null o vacío */
  @Test
  void setNombreJugadorInvalido() {
    Jugador jugador = new Jugador(100, "Valido", 1);
    assertThrows(IllegalArgumentException.class, () -> jugador.setNombreJugador(null), "Nombre no puede ser null");
    assertThrows(IllegalArgumentException.class, () -> jugador.setNombreJugador(""), "Nombre no puede estar vacío");
  }

  /** Verifica que el constructor y setter de nombre cubren todas las ramas */
  @Test
  void setNombreJugadorBranchCoverage() {
    Jugador jugador = new Jugador(100, "Valido", 1);

    assertThrows(IllegalArgumentException.class, () -> jugador.setNombreJugador(null), "Nombre no puede ser null");
    assertThrows(IllegalArgumentException.class, () -> jugador.setNombreJugador(""), "Nombre no puede estar vacío");

    jugador.setNombreJugador("OtroNombre");
    assertEquals("OtroNombre", jugador.getNombreJugador(), "Nombre actualizado correctamente");
  }

  @Test
  void constructorNombreJugadorBranchCoverage() {

    assertThrows(IllegalArgumentException.class, () -> new Jugador(100, null, 1), "Nombre no puede ser null");
    assertThrows(IllegalArgumentException.class, () -> new Jugador(100, "", 1), "Nombre no puede estar vacío");

    Jugador jugador = new Jugador(100, "Valido", 1);
    assertEquals("Valido", jugador.getNombreJugador(), "Nombre inicial correcto");
  }
}
