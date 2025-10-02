package com.NullPtr.Pontiland.entities;

/** Clase que representa una propiedad en el juego */
public class Propiedad extends Casilla {
  private byte idPropiedad = -1;
  private byte idGrupo = -1;
  private byte nivelPropiedad = 1;
  private int precioCompra = -1;
  private int[] rentaPorNivel = null;
  private Jugador dueno = null;

  /**
   * Constructor de la clase Propiedad
   *
   * @param posicionTablero Posicion de la propiedad en el tablero (0-39)
   * @param nombreCasilla Nombre de la propiedad
   * @param idPropiedad Identificador unico de la propiedad (1-24)
   * @param idGrupo Identificador del grupo al que pertenece la propiedad (1-8)
   * @param nivelPropiedad Nivel inicial de la propiedad (1-5)
   * @param precioCompra Precio de compra de la propiedad (>=0)
   * @param rentaPorNivel Array con las rentas por nivel (longitud 5)
   * @throws IllegalArgumentException Si los identificadores, nivel, precio o renta son invalidos
   */
  public Propiedad(
      byte posicionTablero,
      String nombreCasilla,
      byte idPropiedad,
      byte idGrupo,
      byte nivelPropiedad,
      int precioCompra,
      int[] rentaPorNivel) {
    super(posicionTablero, nombreCasilla);
    if (idPropiedad < 1 || idPropiedad > 24 || idGrupo < 1 || idGrupo > 8) {
      throw new IllegalArgumentException("Identificador(es) invalidos para la propiedad");
    }
    if (nivelPropiedad < 1 || nivelPropiedad > 5) {
      throw new IllegalArgumentException("Nivel de propiedad invalido");
    }
    if (precioCompra < 0 || rentaPorNivel == null || rentaPorNivel.length != 5) {
      throw new IllegalArgumentException("Precio de compra o renta por nivel invalido");
    }
    this.idPropiedad = idPropiedad;
    this.idGrupo = idGrupo;
    this.nivelPropiedad = nivelPropiedad;
    this.precioCompra = precioCompra;
    this.rentaPorNivel = rentaPorNivel;
  }
}
