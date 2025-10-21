package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.controllers.HudController;
import com.NullPtr.Pontiland.controllers.HudController.PlayerHudData;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista responsable de dibujar el HUD (Head-Up Display) del juego.
 * Muestra el estado actual de los jugadores: nombre, personaje, dinero y estado (Libre/Encerrado).
 *
 * <p>Además, aplica una animación de entrada con movimiento lateral para que las casillas
 * aparezcan suavemente desde la izquierda de la pantalla.
 */
public class Hud extends BaseAppState {

    /** Estructura auxiliar que agrupa los elementos visuales de cada jugador en el HUD. */
    private static class HudEntry {
        Geometry casilla;
        BitmapText personajeText;
        BitmapText nameText;
        BitmapText dineroText;
        BitmapText estadoText;
        float startX;
        float targetX;
        float y;
        float animTime = 0;
        boolean animDone = false;
    }

    private final HudController controller;
    private final List<HudEntry> entries = new ArrayList<>();

    private Node guiNode;
    private Node hudRoot;
    private BitmapFont font;
    private float camWidth;
    private float camHeight;
    private boolean animStarted = false;
    private float delayTimer = 0f;
    private float delayDuration = 0f;

    public Hud(HudController controller) {
        this.controller = controller;
    }

    @Override
    protected void initialize(Application app) {
        SimpleApplication simpleApp = (SimpleApplication) app;
        guiNode = simpleApp.getGuiNode();
        font = simpleApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        camWidth = app.getCamera().getWidth();
        camHeight = app.getCamera().getHeight();
        hudRoot = new Node("HUDRoot");

        float yStart = camHeight - 200f;
        float targetX = 50f;
        float casillaSize = 128f;
        float spacing = 50f;

        List<PlayerHudData> players = controller.getPlayersData();
        int numJugadores = players.size();

        // Retraso inicial según cantidad de jugadores (efecto escalonado)
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

        // Crear una casilla animada para cada jugador
        for (int i = 0; i < numJugadores; i++) {
            PlayerHudData p = players.get(i);
            HudEntry entry = new HudEntry();

            float finalY = yStart - (i * (casillaSize + spacing));
            entry.startX = -casillaSize - 200f;
            entry.targetX = targetX;
            entry.y = finalY;

            Texture tex = simpleApp.getAssetManager().loadTexture(casillaPaths[i % casillaPaths.length]);
            tex.setWrap(Texture.WrapMode.Clamp);

            Quad quad = new Quad(casillaSize, casillaSize);
            Geometry casilla = new Geometry("casilla" + i, quad);
            Material mat = new Material(simpleApp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setTexture("ColorMap", tex);
            mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
            casilla.setMaterial(mat);
            casilla.setLocalTranslation(entry.startX, finalY, 0);

            // Crear los textos (nombre, personaje, dinero, estado)
            BitmapText personajeText = new BitmapText(font);
            personajeText.setText(p.personaje);
            personajeText.setColor(ColorRGBA.Black);
            centerText(personajeText, entry.startX, finalY, casillaSize, 0);

            BitmapText nameText = new BitmapText(font);
            nameText.setText(p.nombre);
            nameText.setColor(ColorRGBA.Black);
            centerText(nameText, entry.startX, finalY, casillaSize, -25);

            BitmapText dineroText = new BitmapText(font);
            dineroText.setText("$" + p.dinero);
            dineroText.setColor(ColorRGBA.Black);
            centerText(dineroText, entry.startX, finalY, casillaSize, -50);

            BitmapText estadoText = new BitmapText(font);
            estadoText.setText(p.enCarcel ? "Encerrado" : "Libre");
            estadoText.setColor(p.enCarcel ? ColorRGBA.Red : ColorRGBA.Green);
            centerText(estadoText, entry.startX, finalY, casillaSize, -80);

            // Agregar a la raíz del HUD
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

        guiNode.attachChild(hudRoot);
    }

    @Override
    public void update(float tpf) {
        // Espera antes de iniciar animación (sincronización)
        if (!animStarted) {
            delayTimer += tpf;
            if (delayTimer >= delayDuration) animStarted = true;
            else return;
        }

        boolean allDone = true;
        for (HudEntry e : entries) {
            if (e.animDone) continue;
            e.animTime += tpf;
            float progress = Math.min(e.animTime / 1.2f, 1f);
            if (progress >= 1f) e.animDone = true;
            else allDone = false;

            float eased = easeOutCubic(progress);
            float newX = e.startX + (e.targetX - e.startX) * eased;

            // Actualiza posición y textos
            e.casilla.setLocalTranslation(newX, e.y, 0);
            centerText(e.personajeText, newX, e.y, 128f, 0);
            centerText(e.nameText, newX, e.y, 128f, -25);
            centerText(e.dineroText, newX, e.y, 128f, -50);
            centerText(e.estadoText, newX, e.y, 128f, -80);
        }

        if (allDone) animStarted = false;
    }

    /** Centra un texto horizontalmente dentro de una casilla. */
    private void centerText(BitmapText text, float x, float y, float size, float offsetY) {
        float width = text.getLineWidth();
        float textX = x + (size - width) / 2f;
        float textY = y + (size / 2f) + offsetY + 20f;
        text.setLocalTranslation(textX, textY, 10f);
    }

    /** Efecto de suavizado cúbico para animaciones. */
    private float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }

    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
    @Override protected void cleanup(Application app) {}

}











