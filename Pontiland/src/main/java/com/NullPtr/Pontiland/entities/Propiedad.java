package com.NullPtr.Pontiland.entities;

/** Clase que representa una propiedad en el juego */
public class Propiedad {
  private int idPropiedad = -1;
  // REVIEW: Id grupo podría ser eliminado si se usa una matriz de propiedades para separarlas por
  //    grupo
  private int idGrupo = -1;
  private Tipo tipoCasilla = Tipo.PROPIEDAD;
  private String nombreCasilla = null;
  private Byte posicionTablero = -1;
  private int nivelPropiedad = 1;
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
      int posicionTablero,
      String nombreCasilla,
      int idPropiedad,
      int idGrupo,
      int nivelPropiedad,
      int precioCompra,
      int[] rentaPorNivel) {

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

  public int getIdPropiedad() {
    return idPropiedad;
  }

  public void setIdPropiedad(int idPropiedad) {
    if (idPropiedad < 1 || idPropiedad > 24) {
      throw new IllegalArgumentException("Identificador de propiedad invalido");
    }
    this.idPropiedad = idPropiedad;
  }

  public int getNivelPropiedad() {
    return nivelPropiedad;
  }

  public void setNivelPropiedad(int nivelPropiedad) {
    if (nivelPropiedad < 1 || nivelPropiedad > 5) {
      throw new IllegalArgumentException("Nivel de propiedad invalido");
    }
    this.nivelPropiedad = nivelPropiedad;
  }

  public int getPrecioCompra() {
    return precioCompra;
  }

  public void setPrecioCompra(int precioCompra) {
    if (precioCompra < 0) {
      throw new IllegalArgumentException("Precio de compra invalido");
    }
    this.precioCompra = precioCompra;
  }

  public int[] getRentaPorNivel() {
    return rentaPorNivel;
  }

  public void setRentaPorNivel(int[] rentaPorNivel) {
    if (rentaPorNivel == null || rentaPorNivel.length != 5) {
      throw new IllegalArgumentException("Renta por nivel invalida");
    }
    this.rentaPorNivel = rentaPorNivel;
  }

  public Jugador getDueno() {
    return dueno;
  }

  public void setDueno(Jugador dueno) {
    this.dueno = dueno;
  }

  public void setPosicionTablero(Byte posicionTablero) {
    this.posicionTablero = posicionTablero;
  }

  public int getPosicionTablero() {
    return posicionTablero;
  }

  public void setNombreCasilla(String nombreCasilla) {
    if (nombreCasilla == null || nombreCasilla.trim().isEmpty()) {
      throw new IllegalArgumentException("El nombre de la casilla no puede ser nulo o vacío");
    }
    this.nombreCasilla = nombreCasilla;
  }

  public String getNombreCasilla() {
    return this.nombreCasilla;
  }

  public void setTipoCasilla(Tipo tipoCasilla) {
    this.tipoCasilla = tipoCasilla;
  }

  public Tipo getTipoCasilla() {
    return this.tipoCasilla;
  }

  public int getGrupo() {
    return this.idGrupo;
  }

  public String[] getRentasText() {
    if (rentaPorNivel == null) return new String[0];
    String[] out = new String[rentaPorNivel.length];
    for (int i = 0; i < rentaPorNivel.length; i++) {
      out[i] = String.valueOf(rentaPorNivel[i]);
    }
    return out;
  }
}
