package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.view.IPanelCompra;

public interface IInteractionService {

  void InteractionService(IPanelCompra panelCompraView);

  void mostrarCompra(int jugadorId, int posicionTablero);

  void onAccionComprarActual();

  void onAccionPagarRentaActual();

  /* Utilidades de HUD */
  void ocultarPanelCompra();
}
