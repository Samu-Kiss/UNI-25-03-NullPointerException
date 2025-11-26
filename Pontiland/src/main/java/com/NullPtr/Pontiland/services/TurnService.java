package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.Casilla;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.ICasillaRepository;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.view.IScene;
import java.sql.SQLException;
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

  private static Logger logger = LogManager.getLogger(TurnService.class);

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
      logger.error("Error cambiando al siguiente jugador", e);
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
        logger.info(
            "El jugador {} pasa por la salida y cobra 200. Dinero actual: {}",
            jugadorActual.getJugadorId(),
            nuevoDinero);
      }
    } catch (SQLException e) {
      logger.error("Error moviendo al jugador", e);
      return;
    }

    try {
      Casilla casillaLlegada = casillaRepository.casillaFromPosition(posicionAnterior);

      logger.debug(
          "El jugador {} se mueve a la posición {}",
          jugadorActual.getJugadorId(),
          jugadorActual.getPosicion());

      logger.debug(
          "El jugador {} ha llegado a la casilla {} ({})",
          jugadorActual.getJugadorId(),
          casillaLlegada.getNombreCasilla(),
          casillaLlegada.getTipoCasilla());

    } catch (SQLException e) {
      logger.error("Error obteniendo la casilla de llegada", e);
      return;
    }

    try {
      jugadorRepository.updatePosition(jugadorActual.getJugadorId(), jugadorActual.getPosicion());
    } catch (SQLException e) {
      logger.error("Error actualizando la posición del jugador en la base de datos", e);
      return;
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
        logger.error("Error actualizando property tokens al habilitar TurnService", e);
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
      logger.fatal("Error en la máquina de estados en el turno de un jugador", e);
    }
  }

  private void gameFSM() throws SQLException {
    switch (state) {
      case AWAIT_ROLL:
        terminarTurno = false;
        lanzamientoDoble = false;
        if (scene != null) scene.resetCamera();

        Byte[] dados = diceService.getResultados();
        if (dados == null) return;

        if (dados[0] != null && dados[1] != null) {
          lastD1 = dados[0];
          lastD2 = dados[1];
          pendingMovement = datosToInt(dados[0]) + datosToInt(dados[1]);
          //FIXME: Esto se usa para pruebas
          //pendingMovement = 2;
          logger.debug("Dados lanzados: {} + {} = {}", lastD1, lastD2, pendingMovement);
          dados[0] = null;
          dados[1] = null;
          state = TurnState.MOVING;
        }
        break;

      case MOVING:
        updateHUDAndTokens();
        if (pendingMovement > 0) {
          movePlayer(pendingMovement);
          pendingMovement = 0;
        }

        if (!diceService.getCanInteract()) {
          return;
        }

        state = TurnState.INTERACT;
        break;

      case INTERACT:
        boolean esDoble = lastD1 != null && lastD1.equals(lastD2);
        if (esDoble) {
          if (tiradas >= 3) {
            logger.info("3 dobles seguidos, vas a la cárcel!");
            lastD1 = null;
            lastD2 = null;
            moveToJail();
            state = TurnState.END_TURN;
          } else {
            tiradas++;
            logger.info("Doble! Tira de nuevo");
            lastD1 = null;
            lastD2 = null;
            lanzamientoDoble = true;
            state = TurnState.END_TURN;
          }
        } else {
          logger.info("No doble: finalizar turno y cambiar jugador");
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
          logger.debug("La casilla envia a la cárcel después del movimiento");
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

  private int datosToInt(Byte b) {
    return (b == null) ? 0 : b.intValue();
  }

  private void performCasillaInteraccion() {
    try {
      Jugador jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
      casillaService.interaccion(
          jugadorActual, casillaRepository.casillaFromPosition(jugadorActual.getPosicion()));
    } catch (SQLException e) {
      logger.fatal("Error realizando la interacción de la casilla", e);
    }
  }

  private void terminarInteraccionActual() {
    try {
      Jugador jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
      casillaService.terminarInteraccion(
          jugadorActual, casillaRepository.casillaFromPosition(jugadorActual.getPosicion()));
    } catch (SQLException e) {
      logger.fatal("Error terminando la interacción de la casilla", e);
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
      logger.error("Error actualizando property tokens", e);
    }
  }

  @Override
  public void buyProperty() {
    // TODO Auto-generated method stub
  }

  @Override
  public void payRent() {
    // TODO Auto-generated method stub
  }

  public void moveToJail() {
    int jailPosition = 11;
    Jugador jugadorActual;
    try {
      jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
      jugadorRepository.goToJail(
          jugadorRepository.getNumJugadorByPlayerId(jugadorActual.getJugadorId()));

      logger.info(
          "El jugador {} ha sido enviado a la cárcel en la posición {}",
          jugadorActual.getJugadorId(),
          jailPosition);

      scene.replicateFichaPosition(jugadorActual.getJugadorId(), jailPosition - 1);
    } catch (SQLException e) {
      logger.error("Error al enviar al jugador a la cárcel", e);
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
