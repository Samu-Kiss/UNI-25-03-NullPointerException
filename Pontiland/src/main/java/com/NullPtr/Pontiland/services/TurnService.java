package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.ICasillaRepository;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import java.sql.SQLException;

public class TurnService implements ITurnService {
  private IJugadorRepository jugadorRepository;
  private IPartidaRepository partidaRepository;
  private ICasillaService casillaService;
  private DiceService diceService;
  private ICasillaRepository casillaRepository;
  private boolean movePending = false;
  private int lastMovedJugadorId = -1;
  private int lastMovedPos = -1;
  private int tiradas = 1;

  public TurnService(
      IJugadorRepository jugadorRepository,
      IPartidaRepository partidaRepository,
      DiceService diceService,
      ICasillaRepository casillaRepository,
      ICasillaService casillaService) {
    this.casillaService = casillaService;
    this.casillaRepository = casillaRepository;
    this.diceService = diceService;
    this.jugadorRepository = jugadorRepository;
    this.partidaRepository = partidaRepository;
  }

  @Override
  public void nextTurn() {
    try {
      int playerID =
          jugadorRepository.getPlayerIdByNumJugador(
              (jugadorRepository.getActivePlayer() % partidaRepository.getNumJugadores()) + 1);
      jugadorRepository.changeActivePlayer(playerID);

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void movePlayer(int numCasillas) {
    int nuevaPosicion;
    Jugador jugadorActual;
    try {
      jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
      nuevaPosicion = (jugadorActual.getPosicion() + numCasillas) % 40;
      jugadorActual.setPosicion(nuevaPosicion);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    try {
      System.out.println(
          "Movimiendo al jugador "
              + jugadorRepository.getActivePlayer()
              + " a la posición = "
              + jugadorActual.getPosicion());
      System.out.println(
          casillaRepository.casillaFromPosition(jugadorActual.getPosicion()).getNombreCasilla()
              + " de "
              + casillaRepository
                  .casillaFromPosition(jugadorActual.getPosicion())
                  .getTipoCasilla());

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    try {
      jugadorRepository.updateJugador(jugadorActual);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    markLastMove(jugadorActual.getJugadorId(), nuevaPosicion);
  }

  @Override
  public void update() {
    Byte[] dados = diceService.getResultados();
    if (dados == null) return;
    Byte d1 = dados[0];
    Byte d2 = dados[1];

    Jugador jugadorActual = null;
    try {
      if (jugadorRepository.getActivePlayer() != -1)
        jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
    } catch (SQLException e) {
    }
    if (jugadorActual != null) {
      if (diceService.getCanInteract()) {
        casillaService.interaccion(
            jugadorActual, casillaRepository.casillaFromPosition(jugadorActual.getPosicion()));
      }
    }
    if (casillaService.getIrACarcel()) {
        //TODO: Revisar si funciona bien
      System.out.println("sisssisisisisisisiis");
      moveToJail();
    }

    if (d1 != null && d2 != null) {
      int movimiento = d1 + d2;

      System.out.println("Resultados dados: [" + d1 + ", " + d2 + "]");

      movePlayer(movimiento);

      if (d1.equals(d2)) {
        if (tiradas >= 1) {
          System.out.println("3 dobles seguidos, vas a la cárcel!");
            tiradas = 1;
            System.out.println("Antes de ir a carcel");
            moveToJail();
            nextTurn();
          dados[0] = dados[1] = null;
          return;
        } else {
          tiradas++;
          System.out.println("Doble! Tira de nuevo");
          dados[0] = dados[1] = null;
          return;
        }
      } else {
        tiradas = 1;
        System.out.println("Cambiar jugador no dobles");
        nextTurn();
      }

      dados[0] = dados[1] = null;
    }
  }

  @Override
  public void buyProperty() {}

  @Override
  public void payRent() {}

  @Override
  public boolean hasMovePending() {
    return movePending;
  }

  @Override
  public int[] consumeLastMove() {
    if (!movePending) return null;
    movePending = false;
    return new int[] {lastMovedJugadorId, lastMovedPos};
  }

  private void markLastMove(int jugadorId, int nuevaPos) {
    movePending = true;
    lastMovedJugadorId = jugadorId;
    lastMovedPos = nuevaPos;
  }

  @Override
  public void moveToJail() {
    int jailPosition = 11;
    Jugador jugadorActual;
    try {
      jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
      jugadorRepository.goToJail(
          jugadorRepository.getNumJugadorByPlayerId(jugadorActual.getJugadorId())
        );

      System.out.println(
          "El jugador "
              + jugadorActual.getJugadorId()
              + " ha sido enviado a la cárcel en la posición "
              + jailPosition);
      markLastMove(jugadorActual.getJugadorId(), jailPosition);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
