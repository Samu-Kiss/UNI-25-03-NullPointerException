package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.repository.IJugadorRepository;
import com.NullPtr.Pontiland.repository.IPartidaRepository;

import java.sql.SQLException;

public class TurnService implements ITurnService{
    IJugadorRepository jugadorRepository;
    IPartidaRepository partidaRepository;

    public TurnService(IJugadorRepository jugadorRepository, IPartidaRepository partidaRepository) {
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

    @Override
    public void movePlayer(int numCasillas, int playerID) {

    }

    @Override
    public void throwDice(int playerID) {

    }

    @Override
    public void buyProperty(int playerID) {

    }

    @Override
    public void payRent(int playerID) {

    }

    @Override
    public boolean canThrowDice() {
        return false;
    }
}
