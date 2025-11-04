package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.ICasillaRepository;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.view.IScene;
import java.sql.SQLException;

public class TurnService implements ITurnService {
  private IJugadorRepository jugadorRepository;
  private IPartidaRepository partidaRepository;
  private ICasillaService casillaService;
  private DiceService diceService;
  private ICasillaRepository casillaRepository;
  private IScene scene;
  private boolean interactionStarted = false;
  private boolean terminarTurno = false;
  private int tiradas = 1;

  private enum TurnState {
    AWAIT_ROLL,
    MOVING,
    INTERACT,
    POST_MOVE_CHECK,
    END_TURN
  }

  private TurnState state = TurnState.AWAIT_ROLL;
  private Byte lastD1 = null;
  private Byte lastD2 = null;
  private int pendingMovement = 0;

  private boolean enabled = false;

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
  public void setScene(IScene scene) {
    this.scene = scene;
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
      nuevaPosicion = (jugadorActual.getPosicion() - 1 + numCasillas) % 40 + 1;
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

    markLastMove(jugadorActual.getJugadorId(), nuevaPosicion - 1);
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void setTerminarTurno(boolean terminarTurno) {
    this.terminarTurno = terminarTurno;
  }

  @Override
  public void update() {
    if (!enabled) return;

    try {
      switch (state) {
        case AWAIT_ROLL:
          terminarTurno = false;
          if (scene != null) scene.resetCamera();
          Byte[] dados = diceService.getResultados();
          if (dados == null) return;

          if (dados[0] != null && dados[1] != null) {
            lastD1 = dados[0];
            lastD2 = dados[1];
            pendingMovement = dados[0] + dados[1];
            dados[0] = dados[1] = null;

            state = TurnState.MOVING;
          }
          break;

        case MOVING:
          if (pendingMovement > 0) {
            movePlayer(pendingMovement);
            pendingMovement = 0;
            interactionStarted = false;
          }

          if (!diceService.getCanInteract()) {
            return;
          }

          state = TurnState.INTERACT;

          break;

        case INTERACT:
          if (!interactionStarted) {
            if (diceService.getCanInteract()) {
              try {
                Jugador jugadorActual =
                    jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
                casillaService.interaccion(
                    jugadorActual,
                    casillaRepository.casillaFromPosition(jugadorActual.getPosicion()));
                jugadorRepository.updateJugador(jugadorActual);
                interactionStarted = true;
              } catch (SQLException e) {
                throw new RuntimeException(e);
              }
            }

          } else {
            if (diceService.getCanInteract()) {
              interactionStarted = false;
              state = TurnState.POST_MOVE_CHECK;
              break;
            }
          }

        case POST_MOVE_CHECK:
          try {
            Jugador jugadorActual =
                jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
            casillaService.interaccion(
                jugadorActual, casillaRepository.casillaFromPosition(jugadorActual.getPosicion()));
            jugadorRepository.updateJugador(jugadorActual);
            interactionStarted = true;
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }

          if (casillaService.getIrACarcel()) {
            System.out.println("La casilla pedido enviar a la cárcel después del movimiento");
            moveToJail();
            tiradas = 1;
            state = TurnState.END_TURN;
            break;
          }

          if (lastD1 != null && lastD1.equals(lastD2)) {
            if (tiradas >= 3) {
              System.out.println("3 dobles seguidos, vas a la cárcel!");
              tiradas = 1;
              moveToJail();
              state = TurnState.END_TURN;
            } else {
              tiradas++;
              System.out.println("Doble! Tira de nuevo");
              lastD1 = lastD2 = null;
              state = TurnState.AWAIT_ROLL;
            }
          } else {
            tiradas = 1;
            System.out.println("No doble: finalizar turno y cambiar jugador");
            lastD1 = lastD2 = null;
            state = TurnState.END_TURN;
          }
          break;

        case END_TURN:
          if (!terminarTurno) return;
          try {
            Jugador jugadorActual =
                jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
            casillaService.terminarInteraccion(
                jugadorActual, casillaRepository.casillaFromPosition(jugadorActual.getPosicion()));
            jugadorRepository.updateJugador(jugadorActual);
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
          nextTurn();
          state = TurnState.AWAIT_ROLL;
          break;

        default:
          state = TurnState.AWAIT_ROLL;
          break;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void buyProperty() {}

  @Override
  public void payRent() {}

  private void markLastMove(int jugadorId, int nuevaPos) {

    scene.replicateFichaPosition(jugadorId, nuevaPos);
  }

  public void moveToJail() {
    int jailPosition = 11;
    Jugador jugadorActual;
    try {
      jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
      jugadorRepository.goToJail(
          jugadorRepository.getNumJugadorByPlayerId(jugadorActual.getJugadorId()));

      System.out.println(
          "El jugador "
              + jugadorActual.getJugadorId()
              + " ha sido enviado a la cárcel en la posición "
              + jailPosition);
      markLastMove(jugadorActual.getJugadorId(), jailPosition - 1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
