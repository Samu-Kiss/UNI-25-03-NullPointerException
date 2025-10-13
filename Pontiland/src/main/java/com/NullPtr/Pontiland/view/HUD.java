package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.entities.Jugador;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD (Heads-Up Display) del gameplay principal.
 *
 * - Muestra información de jugadores.
 * - Anima su entrada lateral desde la izquierda.
 * - Permite mostrar una carta de propiedad que entra desde abajo
 *   al presionar la tecla N.
 */
public class HUD extends BaseAppState {

    /** Estructura interna que agrupa los elementos visuales asociados a un jugador. */
    private static class HudEntry {
        Geometry bg, bg2;
        BitmapText nameText;
        BitmapText personajeText;
    }

    /** Nombres predefinidos para los personajes disponibles. */
    private static final String[] CHARACTER_NAMES = {
            "Kiwi", "Balon", "Maleta", "Pescadito", "Carnet", "Ignacito", "Nave"
    };

    // Lista de jugadores y sus respectivos personajes seleccionados
    private final List<Jugador> jugadores;
    private final List<Integer> personajeIds;

    // Referencias a los nodos gráficos y fuentes
    private final List<HudEntry> entries = new ArrayList<>();
    private Node guiNode;
    private Node hudRoot;
    private BitmapFont font;

    // Variables de animación del HUD lateral
    private float animTime = 0f;
    private boolean animating = false;
    private float camWidth;

    // ---- Carta de propiedad ----
    private Geometry cartaPropiedad;
    private boolean cartaVisible = false;
    private boolean cartaAnimando = false;
    private float cartaAnimTime = 0f;
    private float camHeight;
    private InputManager inputManager;

    public HUD(List<Jugador> jugadores, List<Integer> personajeIds) {
        this.jugadores = jugadores;
        this.personajeIds = personajeIds;
    }

    @Override
    protected void initialize(Application app) {
        SimpleApplication simpleApp = (SimpleApplication) app;
        guiNode = simpleApp.getGuiNode();
        font = simpleApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        inputManager = simpleApp.getInputManager();

        camWidth = app.getCamera().getWidth();
        camHeight = app.getCamera().getHeight();
        hudRoot = new Node("HUDRoot");

        float yStart = camHeight - 150f;
        float x = 10f;
        float width = 180f;
        float height = 140f;
        float spacing = 80f;

        // Construcción de las casillas de jugador
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            HudEntry entry = new HudEntry();

            float finalY = yStart - (i * (height + spacing));

            Geometry bg = new Geometry("bg" + i, new Quad(width, height));
            bg.setMaterial(makeColor(getColorForPlayer(i)));

            Geometry bg2 = new Geometry("label" + i, new Quad(width, height * 0.4f));
            bg2.setMaterial(makeColor(new ColorRGBA(0.85f, 0.85f, 0.85f, 1f)));

            BitmapText nameText = new BitmapText(font);
            nameText.setText(j.getNombreJugador());
            nameText.setColor(ColorRGBA.Black);

            BitmapText personajeText = new BitmapText(font);
            String personajeNombre = getPersonajeNombre(i);
            personajeText.setText(personajeNombre);
            personajeText.setColor(ColorRGBA.Black);

            bg.setLocalTranslation(x, finalY, 0);
            bg2.setLocalTranslation(x, finalY - height + 110, 0);
            nameText.setLocalTranslation(x + 10, finalY - height + 120 + (height * 0.4f * 0.8f), 0);

            float textHeight = personajeText.getLineHeight();
            float yCenter = finalY + (height / 2f) + (textHeight / 2f) - 10f;
            personajeText.setLocalTranslation(x + 10, yCenter, 0);

            hudRoot.attachChild(bg);
            hudRoot.attachChild(bg2);
            hudRoot.attachChild(nameText);
            hudRoot.attachChild(personajeText);

            entry.bg = bg;
            entry.bg2 = bg2;
            entry.nameText = nameText;
            entry.personajeText = personajeText;
            entries.add(entry);
        }

        // ---- Carta de propiedad ----
        Texture tex = simpleApp.getAssetManager().loadTexture("graphics/sprites/propiedad.png");
        float cartaWidth = 400f;
        float cartaHeight = 500f;
        Quad cartaQuad = new Quad(cartaWidth, cartaHeight);
        cartaPropiedad = new Geometry("CartaPropiedad", cartaQuad);

        Material cartaMat = new Material(simpleApp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        cartaMat.setTexture("ColorMap", tex);
        cartaPropiedad.setMaterial(cartaMat);

        // Posicion inicial: fuera de pantalla (abajo)
        float startY = -cartaHeight - 50f;
        float centerX = (camWidth - cartaWidth) / 2f;
        cartaPropiedad.setLocalTranslation(centerX, startY, 0);

        hudRoot.attachChild(cartaPropiedad);

        // Empieza con HUD fuera de pantalla
        hudRoot.setLocalTranslation(-camWidth * 1.5f, 0, 0);
        guiNode.attachChild(hudRoot);

        // Registrar input
        inputManager.addMapping("toggleCarta", new KeyTrigger(KeyInput.KEY_N));
        inputManager.addListener(actionListener, "toggleCarta");
    }

    /** Escucha la tecla N para mostrar/ocultar la carta. */
    private final ActionListener actionListener = (name, isPressed, tpf) -> {
        if (name.equals("toggleCarta") && !isPressed) {
            cartaAnimando = true;
            cartaAnimTime = 0f;
            cartaVisible = !cartaVisible;
        }
    };

    private String getPersonajeNombre(int i) {
        if (personajeIds == null || personajeIds.size() <= i) return "???";
        int id = personajeIds.get(i);
        if (id <= 0 || id > CHARACTER_NAMES.length) return "???";
        return CHARACTER_NAMES[id - 1];
    }

    @Override
    protected void onEnable() {
        animTime = 0f;
        animating = true;
        hudRoot.setLocalTranslation(-camWidth, 0, 0);
    }

    @Override
    public void update(float tpf) {
        // ---- HUD lateral ----
        if (animating) {
            animTime += tpf;
            float progress = Math.min(animTime / 1.2f, 1f);
            float eased = easeOutCubic(progress);

            float startX = -camWidth * 1.5f;
            float targetX = 0f;
            float currentX = startX + (targetX - startX) * eased;

            hudRoot.setLocalTranslation(currentX, 0, 0);

            if (progress >= 1f) animating = false;
        }

        // ---- Animación de carta ----
        if (cartaAnimando) {
            cartaAnimTime += tpf;
            float progress = Math.min(cartaAnimTime / 1.2f, 1f);
            float eased = easeOutCubic(progress);

            float cartaWidth = 400f;
            float cartaHeight = 500f;
            float centerX = (camWidth - cartaWidth) / 2f;
            float hiddenY = -cartaHeight - 50f;
            float visibleY = (camHeight - cartaHeight) / 2f;

            float currentY = cartaVisible
                    ? hiddenY + (visibleY - hiddenY) * eased
                    : visibleY + (hiddenY - visibleY) * eased;

            cartaPropiedad.setLocalTranslation(centerX, currentY, 0);

            if (progress >= 1f) cartaAnimando = false;
        }
    }

    private float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }

    private Material makeColor(ColorRGBA color) {
        Material mat = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        return mat;
    }

    private ColorRGBA getColorForPlayer(int index) {
        switch (index) {
            case 0: return new ColorRGBA(1f, 0.4f, 0.75f, 1f);
            case 1: return new ColorRGBA(0.6f, 0.4f, 1f, 1f);
            case 2: return new ColorRGBA(0f, 0.8f, 1f, 1f);
            case 3: return new ColorRGBA(0f, 1f, 0.5f, 1f);
            default: return ColorRGBA.White;
        }
    }

    @Override
    protected void cleanup(Application app) {
        if (inputManager != null) {
            inputManager.deleteMapping("toggleCarta");
            inputManager.removeListener(actionListener);
        }
    }

    @Override
    protected void onDisable() {}
}








