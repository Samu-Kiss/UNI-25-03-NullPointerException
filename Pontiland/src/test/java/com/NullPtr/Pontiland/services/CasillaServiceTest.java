package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;

import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Tipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias completas para CasillaService. */
class CasillaServiceTest {

  private CasillaService service;
  private Jugador jugador;

  @BeforeEach
  void setUp() {
    service = new CasillaService();
    jugador = new Jugador(500, "JugadorTest", 1);
  }

  @Test
  void testAllSwitchCases() {
    // PARADALIBRE
    Casilla libre = new Casilla(1, "Parada Libre", Tipo.PARADALIBRE);
    service.interaccion(jugador, libre);
    assertFalse(service.getIrACarcel(), "PARADALIBRE no debería activar irACarcel");

    // EVENTO
    Casilla evento = new Casilla(2, "Evento Sorpresa", Tipo.EVENTO);
    service.interaccion(jugador, evento);
    assertFalse(service.getIrACarcel(), "EVENTO no debería activar irACarcel");

    // PROPIEDAD
    Casilla propiedad = new Casilla(3, "Propiedad Central", Tipo.PROPIEDAD);
    service.interaccion(jugador, propiedad);
    assertFalse(service.getIrACarcel(), "PROPIEDAD no debería activar irACarcel");

    // MOVIMIENTO
    Casilla movimiento = new Casilla(4, "Movimiento Especial", Tipo.MOVIMIENTO);
    service.interaccion(jugador, movimiento);
    assertFalse(service.getIrACarcel(), "MOVIMIENTO no debería activar irACarcel");

    // IRALACARCEL
    Casilla carcel = new Casilla(5, "Ir a la cárcel", Tipo.IRALACARCEL);
    service.interaccion(jugador, carcel);
    assertTrue(service.getIrACarcel(), "IRALACARCEL debe activar irACarcel");
  }

  @Test
  void testIrACarcelInitiallyFalse() {
    assertFalse(service.getIrACarcel(), "Por defecto irACarcel debe ser falso");
  }
}
