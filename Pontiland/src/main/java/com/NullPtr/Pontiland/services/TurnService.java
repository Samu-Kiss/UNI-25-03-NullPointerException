package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.ICasillaRepository;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.view.IScene;
import com.NullPtr.Pontiland.view.Scene;

import java.sql.SQLException;

public class TurnService implements ITurnService {
    private IJugadorRepository jugadorRepository;
    private IPartidaRepository partidaRepository;
    private ICasillaService casillaService;
    private DiceService diceService;
    private ICasillaRepository casillaRepository;
    private IScene scene;
    private int tiradas = 1;
    private boolean changeTurn = false;
    private boolean canMove =  false;

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
            nuevaPosicion = (jugadorActual.getPosicion() -1 + numCasillas) % 40 + 1;
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


            casillaService.interaccion(jugadorActual, casillaRepository.casillaFromPosition(jugadorActual.getPosicion()));

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
    public void update() {
        Byte[] dados = diceService.getResultados();
        if (dados == null) return;
        Byte d1 = dados[0];
        Byte d2 = dados[1];

        if (casillaService.getIrACarcel() && diceService.getCanInteract() && canMove) {
            changeTurn = true;
            moveToJail();
        }

        if (d1 != null && d2 != null) {
            int movimiento = d1 + d2;

            System.out.println("Resultados dados: [" + d1 + ", " + d2 + "]");

            movePlayer(movimiento);

            if (d1.equals(d2)) {
                if (tiradas >= 3) {
                    System.out.println("3 dobles seguidos, vas a la cárcel!");
                    tiradas = 1;
                    System.out.println("Antes de ir a carcel");
                    moveToJail();
                    changeTurn = true;
                } else {
                    tiradas++;
                    System.out.println("Doble! Tira de nuevo");
                    changeTurn = false;
                }
            } else {
                tiradas = 1;
                System.out.println("Cambiar jugador no dobles");
                changeTurn = true;
            }

            canMove = true;

            dados[0] = dados[1] = null;

        }
        else canMove = false;

        System.out.println("siguiente turno: " + changeTurn + " - puede interactuar: " + diceService.getCanInteract());

        if(diceService.getCanInteract())
        {
            scene.resetCamera();
            if(changeTurn) {
                nextTurn();
                changeTurn = false;
            }
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