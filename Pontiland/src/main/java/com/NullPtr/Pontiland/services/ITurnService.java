package com.NullPtr.Pontiland.services;

import com.NullPtr.Pontiland.view.HUD.Hud;

public interface ITurnService {

    void nextTurn();

    void update();

    void buyProperty();
    void payRent();

    boolean canThrowDice();
    int[] consumeLastMove();
    boolean hasMovePending();

    void setHud(Hud hud);
}