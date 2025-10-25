package com.NullPtr.Pontiland.view.HUD;

import com.NullPtr.Pontiland.controllers.HudController;
import com.NullPtr.Pontiland.entities.Jugador;
import com.NullPtr.Pontiland.services.TurnService;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.BaseStyles;

import java.util.ArrayList;
import java.util.List;

public class Hud extends BaseAppState implements ActionListener {

    private final HudController controller;
    private final List<HudEntry> entries = new ArrayList<>();

    private Node guiNode;
    private Node hudRoot;
    private InputManager inputManager;
    private TurnService turnService;

    private float camWidth;
    private float camHeight;
    private boolean animStarted = false;
    private float delayTimer = 0f;
    private float delayDuration = 0f;

    private HudCompra hudCompra;
    private HudSubasta hudSubasta;
    private HudPropiedades hudPropiedades;

    public Hud(HudController controller) {
        this.controller = controller;
    }

    public void setTurnService(TurnService turnService) {
        this.turnService = turnService;
    }

    @Override
    protected void initialize(Application app) {
        SimpleApplication simpleApp = (SimpleApplication) app;
        guiNode = simpleApp.getGuiNode();
        inputManager = simpleApp.getInputManager();

        if (GuiGlobals.getInstance() == null) {
            GuiGlobals.initialize(simpleApp);
            BaseStyles.loadGlassStyle();
            GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        }

        camWidth = app.getCamera().getWidth();
        camHeight = app.getCamera().getHeight();

        hudRoot = new Node("HUDRoot");
        crearPanelJugadores(simpleApp);

        // Sub-HUDs
        hudCompra = new HudCompra(simpleApp, hudRoot);
        hudSubasta = new HudSubasta(simpleApp, hudRoot);
        hudPropiedades = new HudPropiedades(simpleApp, controller, hudRoot);

        // 📡 Registrar los listeners a través del controlador
        controller.registrarListeners(hudCompra, hudSubasta, hudPropiedades);

        guiNode.attachChild(hudRoot);
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    }


    private void crearPanelJugadores(SimpleApplication app) {
        float yStart = camHeight - 200f;
        float targetX = 50f;
        float casillaWidth = 128f;
        float spacing = 50f;

        List<Jugador> players = controller.getJugadores();
        int numJugadores = players.size();

        switch (numJugadores) {
            case 2 -> delayDuration = 3.5f;
            case 3 -> delayDuration = 6f;
            case 4 -> delayDuration = 8f;
            default -> delayDuration = 2f;
        }

        for (int i = 0; i < numJugadores; i++) {
            Jugador p = players.get(i);
            HudEntry entry = new HudEntry();

            float finalY = yStart - (i * (casillaWidth + spacing));
            entry.startX = -casillaWidth - 200f;
            entry.targetX = targetX;
            entry.highlightX = targetX + 30f;
            entry.currentX = entry.startX;
            entry.y = finalY;

            Container cont = new Container();
            cont.setPreferredSize(new Vector3f(casillaWidth, casillaWidth, 0));
            cont.setLocalTranslation(entry.startX, finalY, 0);

            String rutaFondo = String.format("graphics/sprites/casilla%d.png", i + 1);
            QuadBackgroundComponent fondo = new QuadBackgroundComponent(
                    app.getAssetManager().loadTexture(rutaFondo)
            );
            cont.setBackground(fondo);

            Label icono = new Label("ICONO " + p.getIconoId());
            icono.setColor(ColorRGBA.Black);
            Label nombre = new Label(p.getNombreJugador());
            nombre.setColor(ColorRGBA.Black);
            Label dinero = new Label("$" + p.getDinero());
            dinero.setColor(ColorRGBA.Black);

            float centerX = casillaWidth / 2f - 40f;
            icono.setLocalTranslation(centerX, casillaWidth * -0.25f, 1);
            nombre.setLocalTranslation(centerX, casillaWidth * -0.55f, 1);
            dinero.setLocalTranslation(centerX, casillaWidth * -0.75f, 1);

            cont.attachChild(icono);
            cont.attachChild(nombre);
            cont.attachChild(dinero);

            hudRoot.attachChild(cont);

            entry.container = cont;
            entry.iconoLabel = icono;
            entry.nombreLabel = nombre;
            entry.dineroLabel = dinero;
            entries.add(entry);
        }
    }
    public void highlightActivePlayer(int playerIndex) {
        int correctedIndex = (playerIndex - 1 + entries.size()) % entries.size();
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).highlighted = (i == correctedIndex);
        }
        hudPropiedades.highlightActivePlayer(correctedIndex);
    }

    public void onJugadorCaeEnCasilla(int numCasilla) {
        if (hudCompra == null || hudSubasta == null) return;
        hudSubasta.ocultar();

        if (numCasilla == 11) {
            if (turnService != null) turnService.lockDiceByUI(true);
            System.out.println("Jugador enviado a la cárcel.");
            return;
        }

        hudCompra.mostrarConCasilla(numCasilla);
        if (turnService != null) turnService.lockDiceByUI(true);
    }

    @Override
    public void update(float tpf) {
        if (!animStarted) {
            delayTimer += tpf;
            if (delayTimer >= delayDuration) animStarted = true;
            else return;
        }

        for (HudEntry e : entries) {
            if (!e.animDone) {
                e.animTime += tpf;
                float progress = Math.min(e.animTime / 1.2f, 1f);
                if (progress >= 1f) e.animDone = true;
                float eased = easeOutCubic(progress);
                e.currentX = e.startX + (e.targetX - e.startX) * eased;
            } else {
                float target = e.highlighted ? e.highlightX : e.targetX;
                e.currentX += (target - e.currentX) * Math.min(tpf * 5f, 1f);
            }
            e.container.setLocalTranslation(e.currentX, e.y, 0);
        }

        hudPropiedades.update(tpf);
        hudCompra.update(tpf);
        hudSubasta.update(tpf);
    }

    private float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }

    @Override protected void onEnable() { inputManager.addListener(this, "Click"); }
    @Override protected void onDisable() { inputManager.removeListener(this); }
    @Override protected void cleanup(Application app) {}
    @Override public void onAction(String name, boolean isPressed, float tpf) {}
}





