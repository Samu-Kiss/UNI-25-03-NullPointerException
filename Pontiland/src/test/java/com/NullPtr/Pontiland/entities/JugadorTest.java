package com.NullPtr.Pontiland.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test unitario para la clase {@link Jugador}.
 * Se prueban constructores, getters, setters y validaciones de argumentos inválidos.
 */
class JugadorTest {

  /**
   * Verifica el constructor completo con lista de propiedades.
   */
  @Test
  void fullConstructorWithPropertiesTest() {
    Propiedad mockProp = Mockito.mock(Propiedad.class);
    List<Propiedad> props = new ArrayList<>();
    props.add(mockProp);

    Jugador jugador = new Jugador(1, "Ignacito", 5, false, 500, props);

    assertEquals(1, jugador.getJugadorId());
    assertEquals("Ignacito", jugador.getNombreJugador());
    assertEquals(5, jugador.getPosicion());
    assertFalse(jugador.getEstado());
    assertEquals(500, jugador.getDinero());
    assertEquals(props, jugador.getPropiedades());
  }

  /**
   * Verifica el constructor por defecto.
   */
  @Test
  void defaultConstructorTest() {
    Jugador jugador = new Jugador(1000, "Pontiland", 2);

    assertEquals(2, jugador.getJugadorId());
    assertEquals("Pontiland", jugador.getNombreJugador());
    assertEquals(1000, jugador.getDinero());
    assertEquals(1, jugador.getPosicion());
    assertFalse(jugador.getEstado());
    assertNotNull(jugador.getPropiedades());
    assertTrue(jugador.getPropiedades().isEmpty());
  }

  /**
   * Verifica el constructor completo con todos los parámetros.
   */
  @Test
  void fullConstructorAllParamsTest() {
    Jugador jugador = new Jugador(3, 2, "Caramelo", 1, 10, true, 500, 123L);

    assertEquals(3, jugador.getJugadorId());
    assertEquals(2, jugador.getNumJugador());
    assertEquals("Caramelo", jugador.getNombreJugador());
    assertEquals(10, jugador.getPosicion());
    assertTrue(jugador.getEstado());
    assertEquals(500, jugador.getDinero());
    assertEquals(123L, jugador.getPartida());
  }

  /**
   * Verifica setters y getters con valores válidos.
   */
  @Test
  void settersAndGettersTest() {
    Jugador jugador = new Jugador(500, "Fantasma", 1);

    jugador.setNombreJugador("NuevoNombre");
    assertEquals("NuevoNombre", jugador.getNombreJugador());

    jugador.setPosicion(15);
    assertEquals(15, jugador.getPosicion());

    jugador.setEstado(true);
    assertTrue(jugador.getEstado());

    jugador.setDinero(750);
    assertEquals(750, jugador.getDinero());

    List<Propiedad> props = new ArrayList<>();
    props.add(Mockito.mock(Propiedad.class));
    jugador.setPropiedades(props);
    assertEquals(props, jugador.getPropiedades());
  }

  /**
   * Verifica que los constructores y setters lancen excepciones con argumentos inválidos.
   */
  @Test
  void invalidArgumentsTest() {
    // Constructor por defecto inválido
    assertThrows(IllegalArgumentException.class, () -> new Jugador(-1, "Nombre", 1));
    assertThrows(IllegalArgumentException.class, () -> new Jugador(100, "", 1));
    assertThrows(IllegalArgumentException.class, () -> new Jugador(100, "Nombre", 5));

    // Constructor completo inválido
    assertThrows(IllegalArgumentException.class,
            () -> new Jugador(1, 2, "", 1, 10, false, 100, 100L));
    assertThrows(IllegalArgumentException.class,
            () -> new Jugador(1, 0, "Nombre", 1, 10, false, 100, 100L));
    assertThrows(IllegalArgumentException.class,
            () -> new Jugador(1, 5, "Nombre", 1, 10, false, 100, 100L));
    assertThrows(IllegalArgumentException.class,
            () -> new Jugador(1, 2, "Nombre", 0, 10, false, 100, 100L));
    assertThrows(IllegalArgumentException.class,
            () -> new Jugador(1, 2, "Nombre", 8, 10, false, 100, 100L));
    assertThrows(IllegalArgumentException.class,
            () -> new Jugador(1, 2, "Nombre", 1, 0, false, 100, 100L));
    assertThrows(IllegalArgumentException.class,
            () -> new Jugador(1, 2, "Nombre", 1, 41, false, 100, 100L));
    assertThrows(IllegalArgumentException.class,
            () -> new Jugador(1, 2, "Nombre", 1, 10, false, -1, 100L));
    assertThrows(IllegalArgumentException.class,
            () -> new Jugador(1, 2, "Nombre", 1, 10, false, 100, -1L));

    // Setters inválidos
    Jugador jugador = new Jugador(100, "ValidName", 1);
    assertThrows(IllegalArgumentException.class, () -> jugador.setNombreJugador(""));
    assertThrows(IllegalArgumentException.class, () -> jugador.setPosicion(0));
    assertThrows(IllegalArgumentException.class, () -> jugador.setPosicion(41));
    assertThrows(IllegalArgumentException.class, () -> jugador.setPropiedades(null));
    assertThrows(IllegalArgumentException.class, () -> jugador.setJugadorId((byte) -1));
    assertThrows(IllegalArgumentException.class, () -> jugador.setJugadorId((byte) 5));
  }
}
