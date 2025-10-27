package com.NullPtr.Pontiland.view;

public interface IPanelCompra {

  /**
   * Firma: mostrarCompra(int casillaId, String nombre, int precio, Integer duenioId) Propósito:
   * Mostrar el panel de compra con la información provista. Parámetros: - casillaId: identificador
   * de casilla - nombre: nombre de la propiedad - precio: costo de compra - duenioId: id del dueño
   * o null si no tiene Retorno: void
   */
  void mostrarCompra(int casillaId, String nombre, int precio, Integer duenioId);

  /** Firma: ocultarCompra() Propósito: Ocultar el panel de compra. Parámetros: N/A Retorno: void */
  void ocultarCompra();

  /**
   * Firma: estaVisible() Propósito: Consultar visibilidad actual del panel. Parámetros: N/A
   * Retorno: boolean (true si visible)
   */
  boolean estaVisible();
}
