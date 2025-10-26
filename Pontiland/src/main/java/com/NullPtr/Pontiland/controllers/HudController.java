package com.NullPtr.Pontiland.controllers;
import com.jme3.input.InputManager;
import com.jme3.scene.Node;
import com.NullPtr.Pontiland.services.ITurnService;
import view.panel.PanelCompra;

public class HudController {

    private final ITurnService turnService; // o interactionService

    private final PanelCompra panelCompra;

    public HudController(InputManager input, Node guiNode, ITurnService turnService) {
        this.turnService = turnService;

        this.panelCompra = new PanelCompra(guiNode);
        this.panelCompra.setController(this);

    }

    public void onComprarDesdeVista() {
    }

    public void onCancelarCompraDesdeVista() {
    }

    public void onSubastarDesdeVista() {
    }
}
