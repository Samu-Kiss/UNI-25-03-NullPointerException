package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.ICasillaRepository;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.view.IScene;
import java.sql.SQLException;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TurnService implements ITurnService {
  private IJugadorRepository jugadorRepository;
  private IPartidaRepository partidaRepository;
  private ICasillaService casillaService;
  private DiceService diceService;
  private ICasillaRepository casillaRepository;
  private IHUDcontroller hudController;
  private IScene scene;
  private boolean terminarTurno = false;
  private int tiradas = 1;
  private ISubastaService subastaService;
  private static Logger logger = LogManager.getLogger(CasillaService.class);

  private enum TurnState {
    AWAIT_ROLL,
    MOVING,
    INTERACT,
    END_TURN,
    NEXT_TURN
  }

  private TurnState state = TurnState.AWAIT_ROLL;
  private Byte lastD1 = null;
  private Byte lastD2 = null;
  private int pendingMovement = 0;
  private boolean lanzamientoDoble = false;

  private boolean enabled = false;

  public TurnService(
      IJugadorRepository jugadorRepository,
      IPartidaRepository partidaRepository,
      DiceService diceService,
      ICasillaRepository casillaRepository,
      ICasillaService casillaService,
      IHUDcontroller hudController,
      ISubastaService subastaService) {
    this.casillaService = casillaService;
    this.casillaRepository = casillaRepository;
    this.diceService = diceService;
    this.jugadorRepository = jugadorRepository;
    this.partidaRepository = partidaRepository;
    this.hudController = hudController;
    this.subastaService = subastaService;
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
    int posicionAnterior;
    try {
      jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
      posicionAnterior = jugadorActual.getPosicion();
      nuevaPosicion = (posicionAnterior - 1 + numCasillas) % 40 + 1;

      boolean pasaPorSalida = (posicionAnterior + numCasillas) > 40 || nuevaPosicion == 1;
      jugadorActual.setPosicion(nuevaPosicion);

      if (pasaPorSalida) {
        int nuevoDinero = jugadorActual.getDinero() + 200;
        jugadorActual.setDinero(nuevoDinero);
        jugadorRepository.updateDinero(jugadorActual.getJugadorId(), nuevoDinero);
          logger.info("El jugador {} pasa por la salida y cobra 200. Dinero actual: {}",
                jugadorActual.getJugadorId(),
                nuevoDinero);
      }
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
      jugadorRepository.updatePosition(jugadorActual.getJugadorId(), jugadorActual.getPosicion());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    scene.replicateFichaPosition(jugadorActual.getJugadorId(), nuevaPosicion - 1);
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled) {
      try {
        casillaService.updateActivePlayerPropertyTokens(
            jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer()));
      } catch (SQLException e) {
        System.out.println(
            "Error actualizando property tokens al habilitar TurnService: " + e.getMessage());
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void terminarTurno() {
    this.terminarTurno = true;
    if (diceService != null) diceService.enableInteract(true);
  }

  @Override
  public void update() {
    if (!enabled) return;
    try {
      gameFSM();
    } catch (Exception e) {
      System.out.println(Arrays.toString(e.getStackTrace()));
      throw new RuntimeException(e);
    }
  }

  private void gameFSM() throws SQLException {
    switch (state) {
      case AWAIT_ROLL:
        terminarTurno = false;
        lanzamientoDoble = false;
        scene.resetCamera();

        Byte[] dados = diceService.getResultados();

        if (dados[0] != null && dados[1] != null) {
          lastD1 = dados[0];
          lastD2 = dados[1];
          pendingMovement = dados[0] + dados[1];
          dados[0] = null;
          dados[1] = null;
          state = TurnState.MOVING;
        }
        break;

      case MOVING:
        updateHUDAndTokens();
        movePlayer(pendingMovement);
        pendingMovement = 0;

        if (!diceService.getCanInteract()) {
          return;
        }

        state = TurnState.INTERACT;
        break;

      case INTERACT:
        if (lastD1.equals(lastD2)) {
          if (tiradas >= 3) {
            System.out.println("3 dobles seguidos, vas a la cárcel!");
            lastD1 = null;
            lastD2 = null;
            moveToJail();
            state = TurnState.END_TURN;
          } else {
            tiradas++;
            System.out.println("Doble! Tira de nuevo");
            lastD1 = null;
            lastD2 = null;
            lanzamientoDoble = true;
            state = TurnState.END_TURN;
          }
        } else {
          System.out.println("No doble: finalizar turno y cambiar jugador");
          lastD1 = null;
          lastD2 = null;
          state = TurnState.END_TURN;
        }

        if (diceService.getCanInteract()) {
          performCasillaInteraccion();
        }
        break;

      case END_TURN:
        if (casillaService.getIrACarcel()) {
          System.out.println("La casilla envia a la cárcel después del movimiento");
          moveToJail();
        }

        if (terminarTurno) {
          state = TurnState.NEXT_TURN;
        } else {
          terminarInteraccionActual();
        }

        break;

      case NEXT_TURN:
        updateHUDAndTokens();
        terminarTurno = false;
        if (!lanzamientoDoble) {
          tiradas = 1;
          nextTurn();
        }
        state = TurnState.AWAIT_ROLL;
        break;
    }
  }

  private void performCasillaInteraccion() {
    try {
      Jugador jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
      casillaService.interaccion(
          jugadorActual, casillaRepository.casillaFromPosition(jugadorActual.getPosicion()));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void terminarInteraccionActual() {
    try {
      Jugador jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
      casillaService.terminarInteraccion(
          jugadorActual, casillaRepository.casillaFromPosition(jugadorActual.getPosicion()));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void updateHUDAndTokens() throws SQLException {
    for (int i = jugadorRepository.getPlayerCount(); i > 0; i--) {
      Jugador jugador =
          jugadorRepository.getJugadorByID(jugadorRepository.getPlayerIdByNumJugador(i));
      hudController.updatePlayerCard(
          jugador.getNombreJugador(), String.valueOf(jugador.getDinero()), jugador.getEstado(), i);
    }

    try {
      casillaService.updateActivePlayerPropertyTokens(
          jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer()));
    } catch (SQLException e) {
      System.out.println("Error actualizando property tokens: " + e.getMessage());
    }
  }


  @Override
  public void buyProperty() {}

  @Override
  public void payRent() {}

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

      scene.replicateFichaPosition(jugadorActual.getJugadorId(), jailPosition - 1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean iniciarSubasta() {
    return subastaService != null && subastaService.iniciarSubasta();
  }

  @Override
  public boolean increaseAuction(int delta) {
    if (delta <= 0) return false;
    return subastaService != null && subastaService.aumentarPrecio(delta);
  }

  @Override
  public void exitAuction() {
    subastaService.salirSubasta();
  }
}
