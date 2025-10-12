package com.NullPtr.Pontiland.services;

public interface ITurnService {

    void nextTurn();

    void update();

    void buyProperty();
    void payRent();

    boolean canThrowDice();
    public int[] consumeLastMove();
    public boolean hasMovePending();
}