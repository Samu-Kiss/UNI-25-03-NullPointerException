// File: HudPropiedades.java
package com.NullPtr.Pontiland.view.HUD;

import com.NullPtr.Pontiland.controllers.HudController;
import com.NullPtr.Pontiland.entities.Jugador;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.BaseStyles;

import java.util.*;

public class HudPropiedades {

    private final HudController controller;
    private final Node hudRoot;
    private final float camWidth;
    private final float camHeight;

    // map jugadorId -> lista de casillas
    private final Map<Integer, List<Integer>> propiedadesPorJugador = new HashMap<>();
    // map jugadorId -> Container Lemur
    private final Map<Integer, Container> panelesPorJugador = new HashMap<>();

    private int jugadorActivo = 0; // índice en la lista controller.getJugadores()
    private Container panelVisibleActual = null;
    private Container panelEntrante = null;
    private boolean animandoPanel = false;
    private float animTimer = 0f;
    private static final float ANIM_DURACION = 0.5f;

    public HudPropiedades(SimpleApplication app, HudController controller, Node hudRoot) {
        this.controller = controller;
        this.hudRoot = hudRoot;
        this.camWidth = app.getCamera().getWidth();
        this.camHeight = app.getCamera().getHeight();

        if (GuiGlobals.getInstance() == null) {
            GuiGlobals.initialize(app);
            BaseStyles.loadGlassStyle();
            GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        }

        crearPaneles();
    }

    private void crearPaneles() {
        List<Jugador> jugadores = controller.getJugadores();

        for (Jugador j : jugadores) {
            Container panel = new Container();
            panel.setBackground(new QuadBackgroundComponent(new ColorRGBA(0, 0, 0, 0.4f)));
            panel.setPreferredSize(new Vector3f(camWidth * 0.6f, 150f, 0));

            // Posición inicial (fuera de pantalla inferior)
            panel.setLocalTranslation(camWidth * 0.2f, -300f, 0);

            Label titulo = new Label("Propiedades de " + j.getNombreJugador());
            titulo.setColor(ColorRGBA.White);
            titulo.setFontSize(22);
            panel.addChild(titulo);

            Container lista = new Container();
            lista.setName("PropiedadesContenido_" + j.getJugadorId());
            lista.setBackground(null);
            panel.addChild(lista);

            // oculto inicialmente
            panel.setCullHint(Node.CullHint.Always);

            hudRoot.attachChild(panel);
            panelesPorJugador.put(j.getJugadorId(), panel);
            propiedadesPorJugador.put(j.getJugadorId(), new ArrayList<>());
        }
    }


    public void agregarPropiedadJugadorActivo(int numCasilla) {
        List<Jugador> jugadores = controller.getJugadores();
        if (jugadorActivo < 0 || jugadorActivo >= jugadores.size()) return;

        int jugadorId = jugadores.get(jugadorActivo).getJugadorId();
        agregarPropiedadAJugador(jugadorId, numCasilla);
    }

    /** añade la propiedad al jugador identificado por jugadorId (ID real). */
    public void agregarPropiedadAJugador(int jugadorId, int numCasilla) {
        propiedadesPorJugador.computeIfAbsent(jugadorId, k -> new ArrayList<>()).add(numCasilla);
        // Mostrar/actualizar el panel del jugador (sin cambiar turnos)
        mostrarPanelDeJugador(jugadorId);
    }

    /** Selecciona visualmente el jugador por índice (0-based) */
    public void highlightActivePlayer(int playerIndex) {
        this.jugadorActivo = playerIndex;
        // actualizar la vista para ese jugador
        List<Jugador> jugadores = controller.getJugadores();
        if (playerIndex < 0 || playerIndex >= jugadores.size()) return;
        int jugadorId = jugadores.get(playerIndex).getJugadorId();
        mostrarPanelDeJugador(jugadorId);
    }

    /** Muestra (y rellena) el panel correspondiente al jugadorId.
     *  Hace la animación si corresponde.
     */
    private void mostrarPanelDeJugador(int jugadorId) {
        // establecer jugadorActivo como índice en la lista (si existe)
        List<Jugador> jugadores = controller.getJugadores();
        int idx = -1;
        for (int i = 0; i < jugadores.size(); i++) {
            if (jugadores.get(i).getJugadorId() == jugadorId) { idx = i; break; }
        }
        if (idx >= 0) this.jugadorActivo = idx;

        Container nuevoPanel = panelesPorJugador.get(jugadorId);
        if (nuevoPanel == null) return;

        // rellenar contenido
        Container contenido = (Container) nuevoPanel.getChild("PropiedadesContenido_" + jugadorId);
        if (contenido != null) clearContainer(contenido);

        List<Integer> propiedades = propiedadesPorJugador.getOrDefault(jugadorId, List.of());
        for (int num : propiedades) {
            Label propLabel = new Label("• Propiedad " + num);
            propLabel.setColor(ColorRGBA.White);
            propLabel.setFontSize(16);
            contenido.addChild(propLabel);
        }

        // si ya visible, simplemente aseguramos que esté en pantalla
        if (panelVisibleActual == nuevoPanel) {
            panelVisibleActual.setCullHint(Node.CullHint.Inherit);
            return;
        }

        // iniciar animación de intercambio
        animandoPanel = true;
        animTimer = 0f;
        panelEntrante = nuevoPanel;

        if (panelVisibleActual != null) panelVisibleActual.setCullHint(Node.CullHint.Inherit);
        panelEntrante.setCullHint(Node.CullHint.Inherit);
    }

    /** Limpia children de un Container de forma segura. */
    private void clearContainer(Container c) {
        // Container hereda de Panel -> Node, usamos detachChild hasta vaciar
        while (!c.getChildren().isEmpty()) {
            c.detachChild(c.getChild(0));
        }
    }

    public void update(float tpf) {
        if (animandoPanel && panelEntrante != null) {
            animTimer += tpf;
            float progress = Math.min(animTimer / ANIM_DURACION, 1f);
            float eased = easeOutCubic(progress);

            float targetY = camHeight * 0.25f; // destino visible
            float hiddenY = -300f; // fuera de pantalla inferior

            if (panelVisibleActual != null) {
                float yOld = targetY - (targetY - hiddenY) * eased;
                panelVisibleActual.setLocalTranslation(camWidth * 0.2f, yOld, 0);
                if (progress >= 1f) panelVisibleActual.setCullHint(Node.CullHint.Always);
            }

            float yNew = hiddenY + (targetY - hiddenY) * eased;
            panelEntrante.setLocalTranslation(camWidth * 0.2f, yNew, 0);

            if (progress >= 1f) {
                animandoPanel = false;
                panelVisibleActual = panelEntrante;
                panelEntrante = null;
            }
        }
    }

    private float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }
}

