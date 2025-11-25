package com.NullPtr.Pontiland.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test unitario para la clase {@link Propiedad}. Se prueban constructor, getters, setters y
 * validaciones.
 */
class PropiedadTest {

  /** Verifica que el constructor con valores válidos inicializa correctamente los campos. */
  @Test
  void validConstructorTest() {
    int[] rents = {10, 20, 30, 40, 50};
    Propiedad propiedad = new Propiedad(5, "Arca", 1, 1, 1, 100, rents);

    assertEquals(1, propiedad.getIdPropiedad());
    assertEquals(1, propiedad.getNivelPropiedad());
    assertEquals(100, propiedad.getPrecioCompra());
    assertArrayEquals(rents, propiedad.getRentaPorNivel());

    assertNull(
        propiedad.getNombreCasilla(),
        "El constructor no debe asignar el nombre de la casilla, por lo que debe quedar null");
  }

  /**
   * Verifica que el constructor lance IllegalArgumentException cuando se proporcionan parámetros
   * inválidos de cualquier tipo.
   *
   * <p>Casos cubiertos: - idPropiedad fuera de rango - idGrupo fuera de rango - nivelPropiedad
   * fuera de rango - precioCompra negativo - arreglo de rentas con longitud incorrecta
   */
  @Test
  void invalidConstructorTest() {
    int[] invalidRents = {10, 20};

    // idPropiedad menor que el mínimo permitido
    assertThrows(
        IllegalArgumentException.class,
        () -> new Propiedad(5, "Av. Central", 0, 1, 1, 100, new int[] {10, 20, 30, 40, 50}),
        "Debe lanzar excepción cuando idPropiedad es menor que 1");

    // idGrupo menor que el mínimo permitido
    assertThrows(
        IllegalArgumentException.class,
        () -> new Propiedad(5, "Av. Central", 1, 0, 1, 100, new int[] {10, 20, 30, 40, 50}),
        "Debe lanzar excepción cuando idGrupo es menor que 1");

    // nivelPropiedad menor que el mínimo permitido
    assertThrows(
        IllegalArgumentException.class,
        () -> new Propiedad(5, "Av. Central", 1, 1, 0, 100, new int[] {10, 20, 30, 40, 50}),
        "Debe lanzar excepción cuando nivelPropiedad es menor que 1");

    // precioCompra negativo
    assertThrows(
        IllegalArgumentException.class,
        () -> new Propiedad(5, "Av. Central", 1, 1, 1, -1, new int[] {10, 20, 30, 40, 50}),
        "Debe lanzar excepción cuando el precio de compra es negativo");

    // rentas con longitud incorrecta
    assertThrows(
        IllegalArgumentException.class,
        () -> new Propiedad(5, "Av. Central", 1, 1, 1, 100, invalidRents),
        "Debe lanzar excepción cuando el arreglo de rentas no tiene longitud 5");
  }

  /**
   * Verifica que los getters y setters de la clase Propiedad funcionen correctamente cuando se
   * asignan valores válidos.
   */
  @Test
  void gettersAndSettersTest() {
    int[] rents = {10, 20, 30, 40, 50};
    Propiedad propiedad = new Propiedad(5, "Av. Central", 1, 1, 1, 100, rents);

    propiedad.setNombreCasilla("Nueva Calle");
    assertEquals(
        "Nueva Calle",
        propiedad.getNombreCasilla(),
        "El nombre de la casilla debe actualizarse correctamente con setNombreCasilla");

    propiedad.setTipoCasilla(Tipo.PROPIEDAD);
    assertEquals(
        Tipo.PROPIEDAD,
        propiedad.getTipoCasilla(),
        "El tipo de la casilla debe actualizarse correctamente con setTipoCasilla");

    propiedad.setPosicionTablero((byte) 10);
    assertEquals(
        10,
        propiedad.getPosicionTablero(),
        "La posición en el tablero debe actualizarse correctamente con setPosicionTablero");

    propiedad.setNivelPropiedad(3);
    assertEquals(
        3,
        propiedad.getNivelPropiedad(),
        "El nivel de propiedad debe actualizarse correctamente con setNivelPropiedad");

    propiedad.setPrecioCompra(200);
    assertEquals(
        200,
        propiedad.getPrecioCompra(),
        "El precio de compra debe actualizarse correctamente con setPrecioCompra");

    int[] newRents = {15, 25, 35, 45, 55};
    propiedad.setRentaPorNivel(newRents);
    assertArrayEquals(
        newRents,
        propiedad.getRentaPorNivel(),
        "El arreglo de rentas por nivel debe actualizarse correctamente con setRentaPorNivel");

    Jugador jugadorMock = new Jugador(1000, "Jugador1", 1);
    propiedad.setDueno(jugadorMock);
    assertEquals(
        jugadorMock,
        propiedad.getDueno(),
        "El dueño de la propiedad debe actualizarse correctamente con setDueno");
  }

  /**
   * Verifica que los setters de la clase Propiedad lancen IllegalArgumentException cuando se les
   * asignan valores inválidos.
   */
  @Test
  void invalidSettersTest() {
    int[] rents = {10, 20, 30, 40, 50};
    Propiedad propiedad = new Propiedad(5, "Av. Central", 1, 1, 1, 100, rents);

    assertThrows(
        IllegalArgumentException.class,
        () -> propiedad.setNombreCasilla(""),
        "Debe lanzar excepción cuando se asigna un nombre de casilla vacío");

    assertThrows(
        IllegalArgumentException.class,
        () -> propiedad.setNombreCasilla(null),
        "Debe lanzar excepción cuando se asigna un nombre de casilla nulo");

    assertThrows(
        IllegalArgumentException.class,
        () -> propiedad.setNivelPropiedad(0),
        "Debe lanzar excepción cuando nivelPropiedad es menor que 1");

    assertThrows(
        IllegalArgumentException.class,
        () -> propiedad.setNivelPropiedad(6),
        "Debe lanzar excepción cuando nivelPropiedad es mayor que 5");

    assertThrows(
        IllegalArgumentException.class,
        () -> propiedad.setPrecioCompra(-10),
        "Debe lanzar excepción cuando el precio de compra es negativo");

    assertThrows(
        IllegalArgumentException.class,
        () -> propiedad.setRentaPorNivel(new int[] {1, 2}),
        "Debe lanzar excepción cuando el arreglo de rentas no tiene longitud 5");

    assertThrows(
        IllegalArgumentException.class,
        () -> propiedad.setRentaPorNivel(null),
        "Debe lanzar excepción cuando el arreglo de rentas es null");
  }

  /** Verifica que setRentaPorNivel funcione correctamente con valores válidos. */
  @Test
  void validSetRentaPorNivelTest() {
    int[] rents = {10, 20, 30, 40, 50};
    Propiedad propiedad = new Propiedad(5, "Av. Central", 1, 1, 1, 100, rents);

    int[] newRents = {1, 2, 3, 4, 5};
    propiedad.setRentaPorNivel(newRents);

    assertArrayEquals(
        newRents,
        propiedad.getRentaPorNivel(),
        "Debe actualizar correctamente el arreglo de rentas por nivel con un arreglo válido");
  }

  /** Verifica que setIdPropiedad acepte valores válidos dentro del rango permitido. */
  @Test
  void validSetIdPropiedadTest() {
    int[] rents = {10, 20, 30, 40, 50};
    Propiedad propiedad = new Propiedad(5, "Av. Central", 1, 1, 1, 100, rents);

    propiedad.setIdPropiedad(5);
    assertEquals(
        5,
        propiedad.getIdPropiedad(),
        "El idPropiedad debe actualizarse correctamente cuando se asigna un valor válido");

    propiedad.setIdPropiedad(24);
    assertEquals(
        24,
        propiedad.getIdPropiedad(),
        "El idPropiedad debe aceptar correctamente el valor máximo permitido");
  }

  /** Verifica que setIdPropiedad lance excepciones para valores fuera del rango permitido. */
  @Test
  void invalidSetIdPropiedadTest() {
    int[] rents = {10, 20, 30, 40, 50};
    Propiedad propiedad = new Propiedad(5, "Av. Central", 1, 1, 1, 100, rents);

    assertThrows(
        IllegalArgumentException.class,
        () -> propiedad.setIdPropiedad(0),
        "Debe lanzar excepción si idPropiedad es menor que 1");

    assertThrows(
        IllegalArgumentException.class,
        () -> propiedad.setIdPropiedad(25),
        "Debe lanzar excepción si idPropiedad es mayor que 24");
  }
}
