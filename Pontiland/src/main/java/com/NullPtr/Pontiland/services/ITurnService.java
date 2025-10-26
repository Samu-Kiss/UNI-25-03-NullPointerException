package com.NullPtr.Pontiland.services;

public interface ITurnService {

    void nextTurn();

    void update();

    void buyProperty();

    void payRent();

    int[] consumeLastMove();

    boolean hasMovePending();
}