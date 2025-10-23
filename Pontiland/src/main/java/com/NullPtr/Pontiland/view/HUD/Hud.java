package com.NullPtr.Pontiland.view.HUD;

import com.NullPtr.Pontiland.services.TurnService;
import com.NullPtr.Pontiland.controllers.HudController;
import com.NullPtr.Pontiland.entities.Jugador;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.List;

public class Hud extends BaseAppState implements ActionListener {

    private static class HudEntry {
        Geometry casilla;
        com.jme3.font.BitmapText personajeText;
        com.jme3.font.BitmapText nameText;
        com.jme3.font.BitmapText dineroText;
        com.jme3.font.BitmapText estadoText;
        float startX;
        float targetX;
        float highlightX;
        float currentX;
        float y;
        float animTime = 0;
        boolean animDone = false;
        boolean highlighted = false;
    }

    private final HudController controller;
    private final List<HudEntry> entries = new ArrayList<>();

    private Node guiNode;
    private Node hudRoot;
    private Node propiedadesPanel;
    private InputManager inputManager;
    private BitmapFont font;
    private TurnService turnService;

    private float camWidth;
    private float camHeight;
    private boolean animStarted = false;
    private float delayTimer = 0f;
    private float delayDuration = 0f;

    private HudCompra hudCompra;
    private HudSubasta hudSubasta;

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
        font = simpleApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        camWidth = app.getCamera().getWidth();
        camHeight = app.getCamera().getHeight();

        hudRoot = new Node("HUDRoot");
        crearPanelJugadores(simpleApp);
        crearPanelPropiedades(simpleApp);

        hudCompra = new HudCompra(simpleApp, hudRoot);
        hudSubasta = new HudSubasta(simpleApp, hudRoot);

        hudCompra.setListener(new HudCompra.HudCompraListener() {
            @Override
            public void onSubastaSolicitada() {
                if (!hudSubasta.estaVisible()) hudSubasta.mostrar();
            }

            @Override
            public void onFinalizarTurno() {
                if (turnService != null) {
                    turnService.lockDiceByUI(false);
                    turnService.setCanThrowDice(true);
                    turnService.nextTurn();
                }
            }
        });

        guiNode.attachChild(hudRoot);
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    }

    private void crearPanelJugadores(SimpleApplication app) {
        float yStart = camHeight - 200f;
        float targetX = 50f;
        float casillaSize = 128f;
        float spacing = 50f;

        List<Jugador> players = controller.getJugadores();
        int numJugadores = players.size();

        switch (numJugadores) {
            case 2 -> delayDuration = 3f;
            case 3 -> delayDuration = 6f;
            case 4 -> delayDuration = 8f;
            default -> delayDuration = 2f;
        }

        String[] casillaPaths = {
                "graphics/sprites/casilla1.png",
                "graphics/sprites/casilla2.png",
                "graphics/sprites/casilla3.png",
                "graphics/sprites/casilla4.png"
        };

        for (int i = 0; i < numJugadores; i++) {
            Jugador p = players.get(i);
            HudEntry entry = new HudEntry();

            float finalY = yStart - (i * (casillaSize + spacing));
            entry.startX = -casillaSize - 200f;
            entry.targetX = targetX;
            entry.highlightX = targetX + 30f;
            entry.currentX = entry.startX;
            entry.y = finalY;

            var tex = app.getAssetManager().loadTexture(casillaPaths[i % casillaPaths.length]);
            var quad = new Quad(casillaSize, casillaSize);
            var casilla = new Geometry("casilla" + i, quad);
            var mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setTexture("ColorMap", tex);
            casilla.setMaterial(mat);
            casilla.setLocalTranslation(entry.startX, finalY, 0);

            var personajeText = new com.jme3.font.BitmapText(font);
            personajeText.setText("ICONO " + p.getIconoId());
            personajeText.setColor(ColorRGBA.Black);
            centerText(personajeText, entry.startX, finalY, casillaSize, 0);

            var nameText = new com.jme3.font.BitmapText(font);
            nameText.setText(p.getNombreJugador());
            nameText.setColor(ColorRGBA.Black);
            centerText(nameText, entry.startX, finalY, casillaSize, -25);

            var dineroText = new com.jme3.font.BitmapText(font);
            dineroText.setText("$" + p.getDinero());
            dineroText.setColor(ColorRGBA.Black);
            centerText(dineroText, entry.startX, finalY, casillaSize, -50);

            var estadoText = new com.jme3.font.BitmapText(font);
            estadoText.setText(p.getEstado() ? "Encerrado" : "Libre");
            estadoText.setColor(p.getEstado() ? ColorRGBA.Red : ColorRGBA.Green);
            centerText(estadoText, entry.startX, finalY, casillaSize, -80);

            hudRoot.attachChild(casilla);
            hudRoot.attachChild(personajeText);
            hudRoot.attachChild(nameText);
            hudRoot.attachChild(dineroText);
            hudRoot.attachChild(estadoText);

            entry.casilla = casilla;
            entry.personajeText = personajeText;
            entry.nameText = nameText;
            entry.dineroText = dineroText;
            entry.estadoText = estadoText;
            entries.add(entry);
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) { }

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

            e.casilla.setLocalTranslation(e.currentX, e.y, 0);
            centerText(e.personajeText, e.currentX, e.y, 128f, 0);
            centerText(e.nameText, e.currentX, e.y, 128f, -25);
            centerText(e.dineroText, e.currentX, e.y, 128f, -50);
            centerText(e.estadoText, e.currentX, e.y, 128f, -80);
        }

        hudCompra.update(tpf);
        hudSubasta.update(tpf);
    }

    public void highlightActivePlayer(int playerIndex) {

        int correctedIndex = (playerIndex - 1 + entries.size()) % entries.size();

        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).highlighted = (i == correctedIndex);
        }
    }

    public void onJugadorCaeEnCasilla(int numCasilla) {
        if (hudCompra == null || hudSubasta == null) return;
        hudSubasta.ocultar();
        hudCompra.mostrarConCasilla(numCasilla);
        if (turnService != null) {
            turnService.lockDiceByUI(true);
        }
    }

    private void crearPanelPropiedades(SimpleApplication app) {
        propiedadesPanel = new Node("PropiedadesPanel");
        Quad fondo = new Quad(camWidth * 0.6f, 100f);
        Geometry fondoGeo = new Geometry("propFondo", fondo);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.4f));
        fondoGeo.setMaterial(mat);
        fondoGeo.setLocalTranslation(camWidth * 0.2f, 20f, 0);
        propiedadesPanel.attachChild(fondoGeo);

        var titulo = new com.jme3.font.BitmapText(font);
        titulo.setText("Propiedades del Jugador");
        titulo.setColor(ColorRGBA.White);
        titulo.setLocalTranslation(camWidth * 0.45f, 100f, 1f);
        propiedadesPanel.attachChild(titulo);

        hudRoot.attachChild(propiedadesPanel);
    }

    private void centerText(com.jme3.font.BitmapText text, float x, float y, float size, float offsetY) {
        float width = text.getLineWidth();
        float textX = x + (size - width) / 2f;
        float textY = y + (size / 2f) + offsetY + 20f;
        text.setLocalTranslation(textX, textY, 10f);
    }

    private float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }

    @Override protected void onEnable() { inputManager.addListener(this, "Click"); }
    @Override protected void onDisable() { inputManager.removeListener(this); }
    @Override protected void cleanup(Application app) { }
}
