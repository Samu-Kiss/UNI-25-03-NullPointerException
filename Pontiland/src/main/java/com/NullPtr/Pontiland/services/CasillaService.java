package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.controllers.IHUDcontroller;
import com.NullPtr.Pontiland.entities.*;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPropiedadRepository;
import com.NullPtr.Pontiland.repository.TarjetaEventoRepository;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CasillaService implements ICasillaService {

  private boolean irACarcel = false;
  private IHUDcontroller hudController;
  private DiceService diceService;
  private IPropiedadRepository propiedadRepository;
  private IAdquisicionService adquisicionService;
  private TarjetaEventoRepository tarjetaEventoRepository;
  private IJugadorRepository jugadorRepository;

  private boolean debugRealizado = false;
  private boolean debugRealizado2 = false;

  // Logger
  private static Logger logger = LogManager.getLogger(CasillaService.class);

  public CasillaService(
      IHUDcontroller hudController,
      DiceService diceService,
      IPropiedadRepository propiedadRepository,
      IAdquisicionService adquisicionService,
      TarjetaEventoRepository tarjetaEventoRepository,
      IJugadorRepository jugadorRepository) {
    this.hudController = hudController;
    this.diceService = diceService;
    this.propiedadRepository = propiedadRepository;
    this.adquisicionService = adquisicionService;
    this.tarjetaEventoRepository = tarjetaEventoRepository;
    this.jugadorRepository = jugadorRepository;
  }

  @Override
  public void interaccion(Jugador jugador, Casilla casilla) {
    logger.debug(
        "{} ha caido en la casilla {}", jugador.getNombreJugador(), casilla.getTipoCasilla());
    switch (casilla.getTipoCasilla()) {
      case PARADALIBRE:
        onParadaLibre(jugador, casilla);
        break;
      case EVENTO:
        onEvento(jugador, casilla);
        break;
      case PROPIEDAD:
        onPropiedad(casilla);
        break;
      case MOVIMIENTO:
        onMovimiento(jugador, casilla);
        break;
      case IRALACARCEL:
        onCarcel(true);
        break;
    }
  }

  @Override
  public void terminarInteraccion(Jugador jugador, Casilla casilla) {
    debugRealizado = false;
    debugRealizado2 = false;
    if (hudController == null) return;

    if (jugador.getDinero() < 0) {
      logger.debug(
          "{} esta endeudado. Se evalua si cuenta con el patrimonio suficiente para continuar el juego {}",
          jugador.getNombreJugador(),
          casilla.getTipoCasilla());

      evaluateDeuda(jugador);

      return;
    }

    switch (casilla.getTipoCasilla()) {
      case PARADALIBRE:
        hudController.terminarTurno();
        break;
      case EVENTO:
        hudController.terminarTurno();
        break;
      case PROPIEDAD:
        assert hudController != null;
        if (hudController.getPuedeComprar()) {
          try {
            adquisicionService.comprarPropiedadPorPosicion(casilla.getPosicionTablero(), jugador);

            hudController.hidePropertyCard();
            hudController.terminarTurno();
          } catch (Exception ex) {
            logger.error("Failed to purchase property: {}", casilla.getNombreCasilla(), ex);
          }
        }

        break;
      case MOVIMIENTO:
        hudController.terminarTurno();
        break;
      case IRALACARCEL:
        assert hudController != null;
        hudController.terminarTurno();
        onCarcel(false);
        break;
    }
  }

  private void evaluateDeuda(Jugador jugador) {
    try {
      List<Propiedad> props = propiedadRepository.getPropiedadesByJugador(jugador.getJugadorId());

      int valorTotalPropiedades =
          propiedadRepository.getPatrimonioTotalJugador(jugador.getJugadorId());

      if (jugador.getDinero() + valorTotalPropiedades < 0) {
        logger.debug(
            "{} no cuenta con el patrimonio suficiente para continuar el juego y termina el juego",
            jugador.getNombreJugador());
        // TODO finalizar el juego

        hudController.terminarTurno();
        return;
      } else {
        logger.debug(
            "{} cuenta con el patrimonio suficiente para continuar el juego, se vende la propiedad mas cara",
            jugador.getNombreJugador());

        while (jugador.getDinero() < 0) {
          jugador = jugadorRepository.getJugadorByID(jugador.getJugadorId());
          Propiedad propiedadMasCara = null;
          for (Propiedad p : props) {
            if (propiedadMasCara == null
                || p.getPrecioCompra() > propiedadMasCara.getPrecioCompra()) {
              propiedadMasCara = p;
            }
          }

          if (propiedadMasCara != null) {
            adquisicionService.venderPropiedad(propiedadMasCara, jugador);
            props.remove(propiedadMasCara);
            logger.debug(
                "{} ha vendido la propiedad {} por {} monedas.",
                jugador.getNombreJugador(),
                propiedadMasCara.getIdPropiedad(),
                propiedadMasCara.getPrecioCompra());
          } else {
            logger.warn(
                "No se encontró ninguna propiedad para vender para el jugador {}",
                jugador.getJugadorId());
            break;
          }
        }
        hudController.hidePropertyCard();
      }

    } catch (SQLException e) {
      logger.error("Error al evaluar la deuda del jugador {}", jugador.getJugadorId(), e);
    }
  }

  private void onParadaLibre(Jugador j, Casilla c) {
    // Para q sonarquba no se queje
    logger.info(
        "{} ha caido en Parada Libre posicion de casilla {}",
        j.getNombreJugador(),
        c.getPosicionTablero());
    if (diceService != null) diceService.enableInteract(false);
  }

  private void onEvento(Jugador j, Casilla c) {
    // Para q sonarquba no se queje
    logger.info(
        "{} ha caido en Evento posicion de casilla {}",
        j.getNombreJugador(),
        c.getPosicionTablero());

    TarjetaEvento evento = tarjetaEventoRepository.getRandomTarjetaEvento();

    if (evento != null) {
      logger.info("Tarjeta Evento obtenida: {}", evento);
    } else {
      logger.warn("No se pudo obtener una tarjeta de evento aleatoria.");
      return;
    }

    if (evento.getAccion() == Accion.GANA_50
        || evento.getAccion() == Accion.GANA_100
        || evento.getAccion() == Accion.GANA_200) {
      hudController.showGoodEvent(evento.getNombre(), evento.getDescripcion());
    } else {
      hudController.showBadEvent(evento.getNombre(), evento.getDescripcion());
    }

    try {
      switch (evento.getAccion()) {
        case Accion.GANA_50:
          accionGana(j, 50);
          break;
        case Accion.GANA_100:
          accionGana(j, 100);
          break;
        case Accion.GANA_200:
          accionGana(j, 200);
          break;
        case Accion.PROPIEDAD_A_NIVEL_1:
          cambiarNivelPropiedad(j, 1);
          break;
        case Accion.PROPIEDAD_A_NIVEL_5:
          cambiarNivelPropiedad(j, 5);
          break;
        case Accion.PROPIEDAD_NIVEL_PLUS_1:
          logger.debug("{} mejora una propiedad en 1 nivel.", j.getNombreJugador());
          mejorarPropiedad(j, 1);
          break;
        case Accion.PROPIEDAD_NIVEL_MINUS_1:
          logger.debug("{} reduce una propiedad en 1 nivel.", j.getNombreJugador());
          mejorarPropiedad(j, -1);
          break;
        case Accion.PIERDE_50_POR_PROPIEDAD:
          pierde50PorPropiedad(j);
          break;
        case Accion.IR_A_LA_CARCEL:
          logger.debug("{} va a la cárcel.", j.getNombreJugador());
          irACarcel = true;
          break;
        default:
          logger.warn("Acción de tarjeta de evento no reconocida.");
          break;
      }
    } catch (SQLException e) {
      logger.error("Error al ejecutar la acción de la tarjeta de evento", e);
    }

    if (diceService != null) diceService.enableInteract(false);
  }

  // Sonarqube me cae mal >:(
  private static String warningNoPropiedades =
      "No hay propiedades para modificar para el jugador {}";

  private void pierde50PorPropiedad(Jugador j) throws SQLException {
    logger.debug("{} pierde 50 monedas por propiedad.", j.getNombreJugador());
    List<Propiedad> propiedades = propiedadRepository.getPropiedadesByJugador(j.getJugadorId());
    if (propiedades == null || propiedades.isEmpty()) {
      logger.warn(warningNoPropiedades, j.getJugadorId());
      return;
    }
    int totalPerdido = propiedades.size() * 50;
    jugadorRepository.updateDinero(j.getJugadorId(), j.getDinero() - totalPerdido);
  }

  private void mejorarPropiedad(Jugador j, int nivel) throws SQLException {
    List<Propiedad> propiedades = propiedadRepository.getPropiedadesByJugador(j.getJugadorId());
    if (propiedades == null || propiedades.isEmpty()) {
      logger.warn(warningNoPropiedades, j.getJugadorId());
      return;
    }
    SecureRandom rnd = new SecureRandom();
    int randomIndex = rnd.nextInt(propiedades.size());
    Propiedad propiedadSeleccionada = propiedades.get(randomIndex);
    if (propiedadSeleccionada.getNivelPropiedad() == 1 && nivel == -1) {
      return;
    }
    propiedadRepository.updateAdquisicionNivel(
        propiedadSeleccionada.getIdPropiedad(),
        j.getJugadorId(),
        propiedadSeleccionada.getNivelPropiedad() + nivel);
  }

  private void cambiarNivelPropiedad(Jugador j, int nivel) throws SQLException {
    logger.debug("{} modifica una propiedad a nivel {}.", j.getNombreJugador(), nivel);
    List<Propiedad> propiedades = propiedadRepository.getPropiedadesByJugador(j.getJugadorId());
    if (propiedades == null || propiedades.isEmpty()) {
      logger.warn(warningNoPropiedades, j.getJugadorId());
      return;
    }
    SecureRandom rnd = new SecureRandom();
    int randomIndex = rnd.nextInt(propiedades.size());
    Propiedad propiedadSeleccionada = propiedades.get(randomIndex);
    propiedadRepository.updateAdquisicionNivel(
        propiedadSeleccionada.getIdPropiedad(), j.getJugadorId(), nivel);
  }

  private void accionGana(Jugador j, int dinero) throws SQLException {
    logger.debug("{} gana {} monedas.", j.getNombreJugador(), dinero);
    jugadorRepository.updateDinero(j.getJugadorId(), j.getDinero() + dinero);
  }

  private void onPropiedad(Casilla casilla) {
    Propiedad prop = null;
    if (propiedadRepository != null) {
      try {
        prop = propiedadRepository.getPropiedadByPosition(casilla.getPosicionTablero());
      } catch (Exception ex) {
        logger.error("Failed to read Propiedad from repository: {}", ex.getMessage(), ex);
      }
    }

    String name;
    String priceText;
    String[] rentsText;
    int groupIndex;

    if (prop == null) {
      logger.warn(
          "Warning: propiedad is null for casilla at position {}", casilla.getPosicionTablero());

      return;
    }

    name = casilla.getNombreCasilla();
    priceText = String.valueOf(prop.getPrecioCompra());
    rentsText = prop.getRentasText();
    groupIndex = prop.getGrupo();
    logger.debug("Nombre Propiedad: {}", name);
    hudController.showPropertyCard(name, priceText, rentsText, groupIndex);

    if (diceService != null) diceService.enableInteract(false);
    hudController.setPuedeComprar(false);
  }

  private void onMovimiento(Jugador j, Casilla c) {
    // Para q sonarquba no se queje
    logger.info(
        "{} ha caido en Movimiento posicion de casilla {}",
        j.getNombreJugador(),
        c.getPosicionTablero());
    // TODO: Hacer la implementacion de la casilla de movimiento
    if (diceService != null) diceService.enableInteract(false);
  }

  private void onCarcel(boolean irACarcel) {
    this.irACarcel = irACarcel;
  }

  @Override
  public boolean getIrACarcel() {
    return irACarcel;
  }

  @Override
  public void updateActivePlayerPropertyTokens(Jugador jugador) {
    if (hudController == null) return;
    if (jugador == null) {
      logger.warn("Jugador activo no encontrado");
      hudController.updatePropertyTokens(new String[0]);
      return;
    }

    List<Propiedad> propiedades = null;
    try {
      propiedades = propiedadRepository.getPropiedadesByJugador(jugador.getJugadorId());
    } catch (SQLException e) {
      logger.error("Error al obtener las propiedades del jugador {}", jugador.getJugadorId(), e);
      hudController.updatePropertyTokens(new String[0]);
      return;
    }

    if (propiedades == null || propiedades.isEmpty()) {
      if (!debugRealizado) {
        logger.debug("El jugador {} no tiene propiedades", jugador.getJugadorId());
        debugRealizado = true;
      }
      hudController.updatePropertyTokens(new String[0]);
      return;
    }

    String[] tokens = new String[propiedades.size()];
    for (int i = 0; i < propiedades.size(); i++) {
      Propiedad p = propiedades.get(i);
      String propNum = String.valueOf(p.getIdPropiedad());
      String nivel = String.valueOf(p.getNivelPropiedad());
      int grupo = p.getGrupo();
      tokens[i] = propNum + "|" + nivel + "|" + grupo;
      if (!debugRealizado2) {
        logger.debug("Token creado -> propiedad={}, nivel= {}, grupo={}", propNum, nivel, grupo);
        debugRealizado2 = true;
      }
    }

    hudController.updatePropertyTokens(tokens);
  }

  // Para q sonarqube no se queje
  public TarjetaEventoRepository getTarjetaEventoRepository() {
    return tarjetaEventoRepository;
  }
}
