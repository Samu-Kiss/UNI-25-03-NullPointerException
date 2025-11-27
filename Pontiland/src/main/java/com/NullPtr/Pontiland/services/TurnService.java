package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.controllers.IMenuActions;
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
  private int tiradas = 1; // usadas para dobles fuera de cárcel
  private ISubastaService subastaService;
  private boolean irACarcel = false;
  private IAdquisicionService adquisicionService;
  private IMenuActions menuActions;
  private boolean terminarPartida = false;

  private static Logger logger = LogManager.getLogger(TurnService.class);

  // FSM principal de turno
  public enum TurnState {
    AWAIT_ROLL,
    MOVING,
    INTERACT,
    END_TURN,
    NEXT_TURN
  }

  public enum JailState {
    CHECK_ROLLS,
    DECIDE_ACTION,
    ROLL,
    PAY,
    END_TURN
  }

  private TurnState state = TurnState.AWAIT_ROLL;
  private JailState jailState = JailState.CHECK_ROLLS;
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
      ISubastaService subastaService,
      IAdquisicionService adquisicionService) {
    this.casillaService = casillaService;
    this.casillaRepository = casillaRepository;
    this.diceService = diceService;
    this.jugadorRepository = jugadorRepository;
    this.partidaRepository = partidaRepository;
    this.hudController = hudController;
    this.subastaService = subastaService;
    this.adquisicionService = adquisicionService;
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
      Casilla casillaLlegada = casillaRepository.casillaFromPosition(nuevaPosicion);

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
        state = TurnState.AWAIT_ROLL;
        jailState = JailState.CHECK_ROLLS;
        // Actualizar indicador de turno activo al inicio del juego
        int activePlayerId = jugadorRepository.getActivePlayer();
        int activeNumJugador = jugadorRepository.getNumJugadorByPlayerId(activePlayerId);
        hudController.setActivePlayerIndex(activeNumJugador);

        casillaService.updateActivePlayerPropertyTokens(
            jugadorRepository.getJugadorByID(activePlayerId));
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
      int activePlayerId = jugadorRepository.getActivePlayer();
      if (activePlayerId <= 0) {
        return;
      }

      if (terminarPartida) {
        java.util.List<com.NullPtr.Pontiland.entities.Jugador> ranking =
            adquisicionService.obtenerRankingJugadoresDesc();
        logger.info("Ranking de jugadores por capital (desc):");
        for (com.NullPtr.Pontiland.entities.Jugador j : ranking) {
          logger.info("{} -> capital={} (dinero+propiedades)", j.getNombreJugador(), j.getDinero());
        }
        finalizarPartida();
      } else {
        boolean encarcelado = jugadorRepository.getJugadorEstadoByID(activePlayerId);
        if (encarcelado) {
          jailFSM();
        } else {
          gameFSM();
        }
      }

    } catch (Exception e) {
      logger.fatal("Error en la máquina de estados en el turno de un jugador", e);
    }
  }

  /**
   * FSM separada para gestionar el flujo cuando el jugador está en cárcel.
   *
   * <p>- El hecho de estar o no encarcelado se verifica en update(), fuera de esta FSM. - Esta FSM
   * solo se encarga de decidir entre lanzar/pagar y de contar intentos.
   */
  private void jailFSM() throws SQLException {
    switch (jailState) {
      case CHECK_ROLLS:
        {
          if (scene != null) scene.resetCamera();
          tiradas = 1;
          int activePlayerId = jugadorRepository.getActivePlayer();
          int dbRolls = jugadorRepository.getTiradasCarcel(activePlayerId);
          logger.debug(
              "[JAIL] CHECK_ROLLS: playerId={}, tiradasCarcelBD={}", activePlayerId, dbRolls);
          terminarTurno = false;
          lanzamientoDoble = false;
          updateHUDAndTokens();
          if (dbRolls > 3) {
            logger.debug("[JAIL] CHECK_ROLLS -> PAY (dbRolls >= 3)");
            jailState = JailState.PAY;
          } else {
            if (hudController != null) {
              logger.debug("[JAIL] CHECK_ROLLS -> mostrando panel decisión cárcel");
              hudController.showJailDecision();
            }
            jailState = JailState.DECIDE_ACTION;
          }
          break;
        }
      case DECIDE_ACTION:
        {
          lastD1 = null;
          lastD2 = null;
          pendingMovement = 0;
          lanzamientoDoble = false;
          terminarTurno = false;
          if (hudController != null) {
            boolean pay = hudController.getJailPay();
            boolean roll = false;
            if (!pay) {
              roll = hudController.getJailRoll();
            }
            if (pay) {
              logger.debug("[JAIL] DECIDE_ACTION -> PAY (usuario eligió pagar)");
              jailState = JailState.PAY;
              hudController.hideJailDecision();
            } else if (roll) {
              logger.debug("[JAIL] DECIDE_ACTION -> ROLL (usuario eligió lanzar)");
              jailState = JailState.ROLL;
              hudController.hideJailDecision();
            }
          } else {
            logger.debug("[JAIL] DECIDE_ACTION: hudController es null, no se puede leer flags");
          }
          break;
        }
      case ROLL:
        {
          terminarTurno = false;
          lanzamientoDoble = false;
          if (scene != null) scene.resetCamera();
          if (diceService != null && !diceService.getCanInteract()) {
            logger.debug("[JAIL] ROLL: habilitando interacción de dados (estaba deshabilitada)");
            diceService.enableInteract(true);
          }
          Byte[] dados = diceService.getResultados();
          if (dados == null) {
            logger.debug("[JAIL] ROLL: resultados aún null (no se inicializaron)");
            return;
          }
          boolean esDoble = false;
          if (dados[0] != null && dados[1] != null) {
            lastD1 = dados[0];
            lastD2 = dados[1];
            esDoble = lastD1.equals(lastD2);
            logger.debug(
                "[JAIL] ROLL: d1={}, d2={}, suma={}, doble={}",
                lastD1,
                lastD2,
                pendingMovement,
                esDoble);
            dados[0] = null;
            dados[1] = null;
          } else {
            logger.debug("[JAIL] ROLL: dados incompletos d1={}, d2={}", dados[0], dados[1]);
          }
          int activePlayerId = jugadorRepository.getActivePlayer();
          if (lastD1 != null && lastD2 != null) {
            if (esDoble) {
              logger.debug("[JAIL] ROLL: se obtuvo doble -> liberar jugador {}", activePlayerId);
              jugadorRepository.setJugadorLibre(activePlayerId);
              jugadorRepository.resetTiradasCarcel(activePlayerId);
              jugadorRepository.updateDinero(
                  activePlayerId,
                  jugadorRepository.getJugadorByID(activePlayerId).getDinero() - 200);
              updateHUDAndTokens();
              tiradas = 1;
              jailState = JailState.END_TURN;
            } else {
              int prev = tiradas;
              tiradas++;
              logger.debug("[JAIL] ROLL: tiradas local {} -> {}", prev, tiradas);
              if (tiradas > 3) {
                logger.debug(
                    "[JAIL] ROLL: llegó a 3 intentos locales -> incrementar BD y END_TURN");
                jugadorRepository.incrementarTiradasCarcel(activePlayerId);
                jailState = JailState.END_TURN;
                lastD1 = null;
                lastD2 = null;
                tiradas = 1;
              } else {
                logger.debug("[JAIL] ROLL: menos de 3 intentos locales -> END_TURN");
                jailState = JailState.ROLL;
                lastD1 = null;
                lastD2 = null;
              }
            }
          }
          break;
        }
      case PAY:
        {
          int activePlayerId = jugadorRepository.getActivePlayer();
          jugadorRepository.setJugadorLibre(activePlayerId);
          jugadorRepository.resetTiradasCarcel(activePlayerId);
          jugadorRepository.updateDinero(
              activePlayerId, jugadorRepository.getJugadorByID(activePlayerId).getDinero() - 200);
          tiradas = 1;
          if (hudController != null) hudController.hideJailDecision();
          Jugador jugador = jugadorRepository.getJugadorByID(activePlayerId);
          if (jugador.getDinero() < 0) {
            adquisicionService.liquidarDeudaConBanco(jugador);
            jugador = jugadorRepository.getJugadorByID(activePlayerId);
            if (jugador.getDinero() < 0) {
              terminarPartida = true;
              return;
            }
          }
          nextTurn();
          updateHUDAndTokens();
          jailState = JailState.CHECK_ROLLS;
          break;
        }
      case END_TURN:
        {
          logger.debug("[JAIL] END_TURN: nextTurn() y reinicio a CHECK_ROLLS siguiente ciclo");
          nextTurn();
          jailState = JailState.CHECK_ROLLS;
          break;
        }
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
          // FIXME: Esto se usa para pruebas
          // pendingMovement = 2;
          logger.debug("Dados lanzados: {} + {} = {}", lastD1, lastD2, pendingMovement);
          dados[0] = null;
          dados[1] = null;
          state = TurnState.MOVING;
        }
        break;

      case MOVING:
        if (pendingMovement > 0) {
          updateHUDAndTokens();
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
            irACarcel = true;
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

        if (diceService.getCanInteract() && !irACarcel) {
          performCasillaInteraccion();
        }
        break;

      case END_TURN:
        if (casillaService.getIrACarcel()) {
          logger.debug("La casilla envia a la cárcel después del movimiento");
          irACarcel = true;
        }

        if (terminarTurno) {
          state = TurnState.NEXT_TURN;
        } else {
          terminarInteraccionActual();
        }

        break;

      case NEXT_TURN:
        terminarTurno = false;
        if (irACarcel) {
          moveToJail();
          irACarcel = false;
        }
        Jugador jugador = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
        if (jugador.getDinero() < 0) {
          adquisicionService.liquidarDeudaConBanco(jugador);
          jugador = jugadorRepository.getJugadorByID(jugadorRepository.getActivePlayer());
          logger.info(
              "Después de liquidar deudas, el jugador {} tiene dinero: {}",
              jugador.getJugadorId(),
              jugador.getDinero());
          if (jugador.getDinero() < 0) {
            terminarPartida = true;
            return;
          }
        }
        if (!lanzamientoDoble) {
          tiradas = 1;
          nextTurn();
        }
        updateHUDAndTokens();
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
      hudController.updatePlayerCard(jugador, i);
    }

    // Actualizar indicador de turno activo
    int activePlayerId = jugadorRepository.getActivePlayer();
    int activeNumJugador = jugadorRepository.getNumJugadorByPlayerId(activePlayerId);
    hudController.setActivePlayerIndex(activeNumJugador);

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
      // Reiniciar contadores de cárcel
      jugadorRepository.resetTiradasCarcel(jugadorActual.getJugadorId());
      tiradas = 0;
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

  @Override
  public void setMenuActions(IMenuActions menuActions) {
    this.menuActions = menuActions;
  }

  @Override
  public void finalizarPartida() {
    state = TurnState.AWAIT_ROLL;
    jailState = JailState.CHECK_ROLLS;
    logger.info("Finalizando partida - deshabilitando turnos e inputs");
    enabled = false;
    if (diceService != null) {
      diceService.enableInteract(false);
    }
    if (menuActions != null) {
      menuActions.showFinDeJuego();
    } else {
      logger.warn("menuActions es null, no se puede mostrar pantalla de fin de juego");
    }
  }
}
