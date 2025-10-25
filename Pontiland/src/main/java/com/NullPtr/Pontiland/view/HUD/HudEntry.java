package com.NullPtr.Pontiland.view.HUD;

import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;

/**
 * Clase que representa una entrada del HUD de jugador.
 * Contiene referencias a los elementos Lemur y variables de animación.
 */
public class HudEntry {

    public Container container;
    public Label iconoLabel;
    public Label nombreLabel;
    public Label dineroLabel;

    public float startX;
    public float targetX;
    public float highlightX;
    public float currentX;
    public float y;
    public float animTime = 0;
    public boolean animDone = false;
    public boolean highlighted = false;
}

