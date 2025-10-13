package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.entities.Jugador;
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
import java.util.ArrayList;
import java.util.List;

/**
 * HUD (Heads-Up Display) del gameplay principal.
 *
 * <p>Responsabilidades clave:
 * - Renderizar la información de los jugadores en pantalla (nombre + figura elegida).
 * - Animar la aparición del HUD mediante un barrido lateral desde la izquierda.
 * - Asignar colores únicos a cada jugador y mostrar sus datos de manera estructurada.
 *
 * <p>El HUD está compuesto por:
 * - Un rectángulo de color principal (bg) asociado a cada jugador.
 * - Un rectángulo gris inferior (bg2) donde se ubica el nombre del jugador.
 * - El nombre del jugador (BitmapText).
 * - El nombre del personaje elegido (BitmapText), centrado verticalmente dentro del rectángulo de color.
 *
 * <p>La animación de entrada utiliza una interpolación cúbica (ease-out) y se logra desplazando un
 * nodo contenedor (hudRoot) que agrupa todos los elementos del HUD.
 */
public class HUD extends BaseAppState {

    /** Estructura interna que agrupa los elementos visuales asociados a un jugador. */
    private static class HudEntry {
        Geometry bg, bg2;
        BitmapText nameText;
        BitmapText personajeText;
    }

    /** Nombres predefinidos para los personajes disponibles (mientras no existan texturas). */
    private static final String[] CHARACTER_NAMES = {
            "Kiwi", "Balon", "Maleta", "Pescadito", "Carnet", "Ignacito", "Nave"
    };

    // Lista de jugadores y sus respectivos personajes seleccionados
    private final List<Jugador> jugadores;
    private final List<Integer> personajeIds;

    // Referencias a los nodos gráficos y fuentes
    private final List<HudEntry> entries = new ArrayList<>();
    private Node guiNode;
    private Node hudRoot; // nodo raíz del HUD (se anima completo)
    private BitmapFont font;

    // Variables de animación
    private float animTime = 0f;
    private boolean animating = false;
    private float camWidth;

    /**
     * Constructor del HUD.
     *
     * @param jugadores lista de jugadores activos en la partida
     * @param personajeIds lista con los IDs de las figuras seleccionadas por cada jugador
     */
    public HUD(List<Jugador> jugadores, List<Integer> personajeIds) {
        this.jugadores = jugadores;
        this.personajeIds = personajeIds;
    }

    /**
     * Inicializa el HUD al cargarse la pantalla del gameplay.
     *
     * <p>Responsabilidades:
     * - Crear los paneles de jugador (color + gris).
     * - Posicionar los textos de nombre y figura.
     * - Crear el nodo raíz (hudRoot) que será animado.
     * - Iniciar el HUD fuera de pantalla (a la izquierda).
     */
    @Override
    protected void initialize(Application app) {
        SimpleApplication simpleApp = (SimpleApplication) app;
        guiNode = simpleApp.getGuiNode();
        font = simpleApp.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        camWidth = app.getCamera().getWidth();
        hudRoot = new Node("HUDRoot");

        float yStart = app.getCamera().getHeight() - 150f;
        float x = 10f;
        float width = 180f;
        float height = 140f;
        float spacing = 80f;

        // Construcción de cada casilla de jugador
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            HudEntry entry = new HudEntry();

            float finalY = yStart - (i * (height + spacing));

            // Fondo principal (color asociado al jugador)
            Geometry bg = new Geometry("bg" + i, new Quad(width, height));
            bg.setMaterial(makeColor(getColorForPlayer(i)));

            // Fondo gris inferior
            Geometry bg2 = new Geometry("label" + i, new Quad(width, height * 0.4f));
            bg2.setMaterial(makeColor(new ColorRGBA(0.85f, 0.85f, 0.85f, 1f)));

            // Nombre del jugador
            BitmapText nameText = new BitmapText(font);
            nameText.setText(j.getNombreJugador());
            nameText.setColor(ColorRGBA.Black);

            // Nombre del personaje (centrado verticalmente en el rectángulo de color)
            BitmapText personajeText = new BitmapText(font);
            String personajeNombre = getPersonajeNombre(i);
            personajeText.setText(personajeNombre);
            personajeText.setColor(ColorRGBA.Black);

            // Posicionamiento de los elementos
            bg.setLocalTranslation(x, finalY, 0);
            bg2.setLocalTranslation(x, finalY - height + 110, 0);
            nameText.setLocalTranslation(
                    x + 10, finalY - height + 120 + (height * 0.4f * 0.8f), 0);

            // Calcular centro vertical del texto de personaje
            float textHeight = personajeText.getLineHeight();
            float yCenter = finalY + (height / 2f) + (textHeight / 2f) - 10f;
            personajeText.setLocalTranslation(x + 10, yCenter, 0);

            // Agregar al nodo del HUD
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

        // Posición inicial fuera de pantalla (barrido desde la izquierda)
        hudRoot.setLocalTranslation(-camWidth * 1.5f, 0, 0);
        guiNode.attachChild(hudRoot);
    }

    /**
     * Retorna el nombre textual del personaje a partir del ID almacenado en {@link #personajeIds}.
     *
     * @param i índice del jugador
     * @return nombre del personaje o "???" si no es válido
     */
    private String getPersonajeNombre(int i) {
        if (personajeIds == null || personajeIds.size() <= i) return "???";
        int id = personajeIds.get(i);
        if (id <= 0 || id > CHARACTER_NAMES.length) return "???";
        return CHARACTER_NAMES[id - 1];
    }

    /**
     * Activa la animación de entrada del HUD cuando el estado se habilita.
     *
     * <p>Inicializa el nodo raíz fuera de pantalla para iniciar el desplazamiento hacia su posición
     * final.
     */
    @Override
    protected void onEnable() {
        animTime = 0f;
        animating = true;
        hudRoot.setLocalTranslation(-camWidth, 0, 0);
    }

    /**
     * Actualiza la animación del HUD en cada frame.
     *
     * <p>Usa interpolación cúbica (ease-out) para lograr una aceleración suave al inicio y
     * desaceleración al final.
     *
     * @param tpf tiempo por frame (segundos)
     */
    @Override
    public void update(float tpf) {
        if (!animating) return;

        animTime += tpf;
        float progress = Math.min(animTime / 1.2f, 1f); // duración ≈ 1.2s
        float eased = easeOutCubic(progress);

        float startX = -camWidth * 1.5f;
        float targetX = 0f;
        float currentX = startX + (targetX - startX) * eased;

        hudRoot.setLocalTranslation(currentX, 0, 0);

        if (progress >= 1f) animating = false;
    }

    /**
     * Función de interpolación cúbica (ease-out): desacelera progresivamente hacia el final.
     *
     * @param t valor normalizado (0–1)
     * @return valor interpolado suavizado
     */
    private float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }

    /**
     * Crea un material sin sombreado (Unshaded) con color sólido para elementos de HUD.
     *
     * @param color color RGBA a aplicar
     * @return material configurado
     */
    private Material makeColor(ColorRGBA color) {
        Material mat =
                new Material(
                        getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        return mat;
    }

    /**
     * Devuelve un color único por jugador, en orden fijo.
     *
     * @param index índice del jugador (0–3)
     * @return color asociado
     */
    private ColorRGBA getColorForPlayer(int index) {
        switch (index) {
            case 0:
                return new ColorRGBA(1f, 0.4f, 0.75f, 1f);
            case 1:
                return new ColorRGBA(0.6f, 0.4f, 1f, 1f);
            case 2:
                return new ColorRGBA(0f, 0.8f, 1f, 1f);
            case 3:
                return new ColorRGBA(0f, 1f, 0.5f, 1f);
            default:
                return ColorRGBA.White;
        }
    }

    /** Limpia el HUD al salir del estado de juego. */
    @Override
    protected void cleanup(Application app) {}

    /** Método requerido por {@link BaseAppState}, no implementa lógica adicional. */
    @Override
    protected void onDisable() {}
}








