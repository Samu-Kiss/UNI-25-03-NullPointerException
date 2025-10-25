package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.view.HUD.Hud;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TurnService implements ITurnService {

    private IJugadorRepository jugadorRepository;
    private IPartidaRepository partidaRepository;
    private DiceService diceService;
    private Hud hud;

    private int playerID = 0;
    private boolean canThrowDice = true;
    private boolean movePending = false;
    private int lastMovedJugadorId = -1;
    private int lastMovedPos = -1;
    private int tiradas = 1;

    // Nuevo: bloqueo del HUD
    private boolean uiLock = false;

    // Nuevo: pool de hilos para retrasar HUD
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TurnService(IJugadorRepository jugadorRepository, IPartidaRepository partidaRepository,
                       DiceService diceService) {
        this.diceService = diceService;
        this.jugadorRepository = jugadorRepository;
        this.partidaRepository = partidaRepository;
    }

    public void setHud(Hud hud) {
        this.hud = hud;

    }

    public void setCanThrowDice(boolean value) {
        this.canThrowDice = value;
    }

    /** Nuevo: bloqueo controlado por el HUD */
    public void lockDiceByUI(boolean locked) {
        this.uiLock = locked;
        if (locked) this.canThrowDice = false;
    }

    @Override
    public boolean canThrowDice() {
        return canThrowDice && !uiLock;
    }

    @Override
    public void nextTurn() {
        try {
            int numJugadores = partidaRepository.getNumJugadores();
            int nextPlayerNum = (jugadorRepository.getActivePlayer() % numJugadores) + 1;
            playerID = jugadorRepository.getPlayerIdByNumJugador(nextPlayerNum);
            jugadorRepository.changeActivePlayer(playerID);

            // Se Notifica al HUD
            if (hud != null) {
                hud.highlightActivePlayer(nextPlayerNum - 1); // índice 0-based
            }

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

        System.out.println("Moviendo al jugador " + jugadorActual.getJugadorId() +
                " a la posición = " + jugadorActual.getPosicion());

        // TODO: LOGICA DE LA CARCEL
        if (nuevaPosicion == 11) {
            try {
                jugadorActual.setEstado(true); // Estado encarcelado
                jugadorRepository.updateJugador(jugadorActual); // Guardar en DB
                System.out.println("Jugador " + jugadorActual.getNombreJugador() + " encarcelado!");

                // Bloquear interacción
                canThrowDice = false;
                uiLock = true;

                // Notificar visualmente en HUD
                if (hud != null) {
                    scheduler.schedule(() -> {
                        hud.onJugadorCaeEnCasilla(nuevaPosicion);
                    }, 600, TimeUnit.MILLISECONDS); // leve delay visual
                }

                // Pasar turno automáticamente después de un segundo
                scheduler.schedule(() -> {
                    System.out.println("Turno terminado por encarcelamiento de " + jugadorActual.getNombreJugador());
                    canThrowDice = true;
                    uiLock = false;
                    nextTurn();
                }, 1500, TimeUnit.MILLISECONDS);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // se guarda la última jugada
            markLastMove(jugadorActual.getJugadorId(), nuevaPosicion);
            return; // Detenemos aquí, no seguimos con HUD de compra, etc.
        }

        // Movimiento comun
        try {
            jugadorRepository.updateJugador(jugadorActual);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Delay proporcional al número de casillas
        if (hud != null && nuevaPosicion != 1 && nuevaPosicion != 11) {
            canThrowDice = false;
            double delaySeconds = Math.max(0.4 * numCasillas, 0.5);
            System.out.printf("Mostrando HUD en %.2f segundos...%n", delaySeconds);

            scheduler.schedule(() -> {
                hud.onJugadorCaeEnCasilla(nuevaPosicion);
            }, (long) (delaySeconds * 1000), TimeUnit.MILLISECONDS);
        }

        markLastMove(jugadorActual.getJugadorId(), nuevaPosicion);
    }


    @Override
    public void update() {
        Byte[] dados = diceService.getResultados();
        if (dados == null) return;

        Byte d1 = dados[0];
        Byte d2 = dados[1];

        if (!canThrowDice() || d1 == null || d2 == null) return;

        int movimiento = d1 + d2;
        System.out.println("Resultados dados: [" + d1 + ", " + d2 + "]");
        canThrowDice = false;

        movePlayer(movimiento);

        if (d1.equals(d2)) {
            if (tiradas == 2) {
                System.out.println("3 dobles seguidos, vas a la cárcel!");
                tiradas = 1;
                nextTurn();
            } else {
                tiradas++;
                System.out.println("¡Doble! Tira de nuevo");
                canThrowDice = !uiLock;
            }
        } else {
            tiradas = 1;
            if (!uiLock) {

                canThrowDice = true;
                nextTurn();
            }
        }

        dados[0] = dados[1] = null;
    }

    @Override public void buyProperty() {}
    @Override public void payRent() {}
    @Override public boolean hasMovePending() { return movePending; }

    @Override
    public int[] consumeLastMove() {
        if (!movePending) return null;
        movePending = false;
        return new int[]{lastMovedJugadorId, lastMovedPos};
    }

    private void markLastMove(int jugadorId, int nuevaPos) {
        movePending = true;
        lastMovedJugadorId = jugadorId;
        lastMovedPos = nuevaPos;
    }
}



