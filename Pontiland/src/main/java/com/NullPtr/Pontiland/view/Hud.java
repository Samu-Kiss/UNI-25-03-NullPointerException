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

public class Hud extends BaseAppState {

    private static class HudEntry {
        Geometry casilla;
        BitmapText personajeText;
        BitmapText nameText;
        BitmapText dineroText;
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
    private int activePlayerIndex = -1;
    private float turnAnimTime = 0f;
    private boolean isReturning = false;

    // Movimiento lateral del jugador activo
    private final float TURN_SHIFT = 40f;
    private final float TURN_ANIM_DURATION = 0.5f; // medio segundo




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

        // 🔹 Ajustar delay según número de jugadores
        switch (numJugadores) {
            case 2 -> delayDuration = 3f;
            case 3 -> delayDuration = 6f;
            case 4 -> delayDuration = 8f;
            default -> delayDuration = 2f; // por si acaso
        }

        // Rutas de las casillas
        String[] casillaPaths = {
                "graphics/sprites/casilla1.png",
                "graphics/sprites/casilla2.png",
                "graphics/sprites/casilla3.png",
                "graphics/sprites/casilla4.png"
        };

        for (int i = 0; i < numJugadores; i++) {
            PlayerHudData p = players.get(i);
            HudEntry entry = new HudEntry();

            float finalY = yStart - (i * (casillaSize + spacing));
            entry.startX = -casillaSize - 200f; // fuera de la pantalla a la izquierda
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

            // Textos centrados en la casilla
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

            hudRoot.attachChild(casilla);
            hudRoot.attachChild(personajeText);
            hudRoot.attachChild(nameText);
            hudRoot.attachChild(dineroText);

            entry.casilla = casilla;
            entry.personajeText = personajeText;
            entry.nameText = nameText;
            entry.dineroText = dineroText;

            entries.add(entry);
        }

        guiNode.attachChild(hudRoot);

        // La animación empezará después del delay configurado
        animStarted = false;
        delayTimer = 0f;
    }


    @Override
    public void update(float tpf) {
        // Esperar delay antes de animar
        if (!animStarted) {
            delayTimer += tpf;
            if (delayTimer >= delayDuration) {
                animStarted = true;
            } else {
                return; // todavía esperando
            }
        }

        boolean allDone = true;
        for (HudEntry e : entries) {
            if (e.animDone) continue;

            e.animTime += tpf;
            float progress = e.animTime / 1.2f; // duración animación
            if (progress >= 1f) {
                progress = 1f;
                e.animDone = true;
            } else {
                allDone = false;
            }

            float eased = easeOutCubic(progress);
            // Mover suavemente hacia la posición objetivo actual (para animaciones dinámicas)
            float newX = e.startX + (e.targetX - e.startX) * eased;


            e.casilla.setLocalTranslation(newX, e.y, 0);
            centerText(e.personajeText, newX, e.y, 128f, 0);
            centerText(e.nameText, newX, e.y, 128f, -25);
            centerText(e.dineroText, newX, e.y, 128f, -50);






        }

        if (activePlayerIndex != -1) {
            turnAnimTime += tpf;
            float progress = Math.min(turnAnimTime / TURN_ANIM_DURATION, 1f);
            float eased = easeOutCubic(progress);

            HudEntry entry = entries.get(activePlayerIndex);
            float newX = entry.casilla.getLocalTranslation().x;
            float target = entry.targetX;

            newX = newX + (target - newX) * eased;

            entry.casilla.setLocalTranslation(newX, entry.y, 0);
            centerText(entry.personajeText, newX, entry.y, 128f, 0);
            centerText(entry.nameText, newX, entry.y, 128f, -25);
            centerText(entry.dineroText, newX, entry.y, 128f, -50);

            // Si está regresando y terminó
            if (isReturning && progress >= 1f) {
                isReturning = false;
            }
        }

        if (allDone) animStarted = false;
    }

    private void centerText(BitmapText text, float x, float y, float size, float offsetY) {
        float width = text.getLineWidth();
        float textX = x + (size - width) / 2f;
        float textY = y + (size / 2f) + offsetY + 20f;
        text.setLocalTranslation(textX, textY, 0);
    }

    private float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }

    @Override
    protected void onEnable() {
        delayTimer = 0f;
        animStarted = false;
        for (HudEntry e : entries) {
            e.animTime = 0;
            e.animDone = false;
            e.casilla.setLocalTranslation(e.startX, e.y, 0);
        }
    }

    @Override protected void cleanup(Application app) {}
    @Override protected void onDisable() {}

    public void refreshFromController() {
        List<PlayerHudData> players = controller.getPlayersData();
        for (int i = 0; i < players.size() && i < entries.size(); i++) {
            PlayerHudData p = players.get(i);
            HudEntry entry = entries.get(i);
            entry.dineroText.setText("$" + p.dinero);
        }
    }

}











