package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.repository.IJugadorRepository;

public class TurnService implements ITurnService{
    IJugadorRepository jugadorRepository;
    long partidaID;

    public TurnService(IJugadorRepository jugadorRepository, long partidaID) {
        this.jugadorRepository = jugadorRepository;
        this.partidaID = partidaID;
    }


    @Override
    public void nextTurn() {

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
