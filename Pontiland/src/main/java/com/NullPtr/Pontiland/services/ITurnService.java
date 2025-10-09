package com.NullPtr.Pontiland.services;

public interface ITurnService {

    void nextTurn();
    void movePlayer(int numCasillas, int playerID);


    void throwDice(int playerID);

    void buyProperty(int playerID);
    void payRent(int playerID);

    boolean canThrowDice();

}