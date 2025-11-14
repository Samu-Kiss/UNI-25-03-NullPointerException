package com.NullPtr.Pontiland.services;

import static org.junit.jupiter.api.Assertions.*;

import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.entities.Tipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CasillaServiceTest {

  private CasillaService casillaService;
  private Jugador jugador;

  @BeforeEach
  void setUp() {
    casillaService = new CasillaService();
    jugador = new Jugador(500, "JugadorTest", 1);
  }

  @Test
  void testInteraccionConParadaLibre_NoActivaCarcel() {
    Casilla casilla = new Casilla(5, "Parada Libre", Tipo.PARADALIBRE);
    casillaService.interaccion(jugador, casilla);
    assertFalse(casillaService.getIrACarcel(), "No debería activar irACarcel en PARADALIBRE");
  }

  @Test
  void testInteraccionConEvento_NoActivaCarcel() {
    Casilla casilla = new Casilla(10, "Evento Sorpresa", Tipo.EVENTO);
    casillaService.interaccion(jugador, casilla);
    assertFalse(casillaService.getIrACarcel(), "No debería activar irACarcel en EVENTO");
  }

  @Test
  void testInteraccionConPropiedad_NoActivaCarcel() {
    Casilla casilla = new Casilla(15, "Propiedad Central", Tipo.PROPIEDAD);
    casillaService.interaccion(jugador, casilla);
    assertFalse(casillaService.getIrACarcel(), "No debería activar irACarcel en PROPIEDAD");
  }

  @Test
  void testInteraccionConMovimiento_NoActivaCarcel() {
    Casilla casilla = new Casilla(20, "Movimiento Especial", Tipo.MOVIMIENTO);
    casillaService.interaccion(jugador, casilla);
    assertFalse(casillaService.getIrACarcel(), "No debería activar irACarcel en MOVIMIENTO");
  }

  @Test
  void testInteraccionConIrALaCarcel_ActivaCarcel() {
    Casilla casilla = new Casilla(30, "Ir a la cárcel", Tipo.IRALACARCEL);
    casillaService.interaccion(jugador, casilla);
    assertTrue(casillaService.getIrACarcel(), "Debe activar irACarcel cuando cae en IRALACARCEL");
  }

  @Test
  void testGetIrACarcelInicialmenteFalso() {
    assertFalse(casillaService.getIrACarcel(), "Por defecto, irACarcel debe ser falso");
  }
}
