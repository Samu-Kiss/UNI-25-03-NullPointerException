package com.NullPtr.Pontiland.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.NullPtr.Pontiland.utils.PropertiesReader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Unit tests for the {@link Ficha} class.
 *
 * <p>Se prueban los siguientes comportamientos:
 *
 * <ul>
 *   <li>Constructor y getters
 *   <li>Setter de jugador
 *   <li>Representación en cadena (toString)
 *   <li>Comportamiento cuando la propiedad de la ruta es nula
 * </ul>
 */
class FichaTest {

  /**
   * Verifica que el constructor inicializa correctamente los atributos y que los getters devuelven
   * los valores esperados.
   */
  @Test
  void constructorAndGetters() {
    try (MockedStatic<PropertiesReader> mocked = Mockito.mockStatic(PropertiesReader.class)) {
      mocked
          .when(() -> PropertiesReader.getProperty("ModeloIgnacito"))
          .thenReturn("ruta/ignacito.j3o");

      Ficha ficha = new Ficha(1, 10, "Ignacito");

      assertEquals(1, ficha.getIdFicha());
      assertEquals(10, ficha.getJugadorId());
      assertEquals("Ignacito", ficha.getNombreFicha());
      assertEquals("ruta/ignacito.j3o", ficha.getRutaFicha());
      assertNull(ficha.getSpatial());
    }
  }

  /** Verifica que el setter de jugador actualiza correctamente el ID del jugador. */
  @Test
  void setJugadorIdUpdatesValue() {
    Ficha ficha = new Ficha(2, 20, "Pontiland");
    ficha.setJugadorId(30);
    assertEquals(30, ficha.getJugadorId());
  }

  /** Verifica que el método {@link Ficha#toString()} genera la cadena esperada. */
  @Test
  void toStringReturnsExpectedFormat() {
    try (MockedStatic<PropertiesReader> mocked = Mockito.mockStatic(PropertiesReader.class)) {
      mocked
          .when(() -> PropertiesReader.getProperty("ModeloCaramelo"))
          .thenReturn("ruta/caramelo.j3o");
      Ficha ficha = new Ficha(3, 40, "Caramelo");

      String expected = "Ficha{nombreFicha='Caramelo', rutaFicha=ruta/caramelo.j3o}";
      assertEquals(expected, ficha.toString());
    }
  }

  /**
   * Verifica que si {@link PropertiesReader} devuelve null, el atributo rutaFicha también es null.
   */
  @Test
  void rutaNullIfPropertyDoesNotExist() {
    try (MockedStatic<PropertiesReader> mocked = Mockito.mockStatic(PropertiesReader.class)) {
      mocked.when(() -> PropertiesReader.getProperty("ModeloFantasma")).thenReturn(null);

      Ficha ficha = new Ficha(4, 50, "Fantasma");
      assertNull(ficha.getRutaFicha());
    }
  }
}
