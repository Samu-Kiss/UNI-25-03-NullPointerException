package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;
import com.NullPtr.Pontiland.repository.JugadorRepository;

import java.sql.SQLException;

public class TurnService implements ITurnService{
    private IJugadorRepository jugadorRepository;
    private IPartidaRepository partidaRepository;
    private DiceService diceService;
    private int playerID = 0; // ID del jugador actual
    private boolean canThrowDice = true;

    public TurnService(IJugadorRepository jugadorRepository, IPartidaRepository partidaRepository, DiceService diceService) {
        this.diceService = diceService;
        this.jugadorRepository = jugadorRepository;
        this.partidaRepository = partidaRepository;
    }


    @Override
    public void nextTurn() {
        try {
            jugadorRepository.changeActivePlayer(partidaRepository.numJugadores());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void movePlayer(int numCasillas) {
        try {
            Jugador jugadorActual = jugadorRepository.getJugadorByID(jugadorRepository.getPlayerIdByNumJugador(2));
            int nuevaPosicion = (jugadorActual.getPosicion() + numCasillas) % 40;
            jugadorActual.setPosicion(nuevaPosicion);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update() {
        Byte[] dados = diceService.getResultados();
        if(dados[0] != null && dados[1] != null){
            int movimiento = dados[0] + dados[1];

            System.out.println("Resultados dados: [" + dados[0] + ", " + dados[1] + "]");
            canThrowDice = false;

            movePlayer(movimiento);
        }
    }

    @Override
    public void buyProperty() {

    }

    @Override
    public void payRent() {

    }

    @Override
    public boolean canThrowDice() {
        return canThrowDice;
    }
}
