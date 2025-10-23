package com.NullPtr.Pontiland.view.HUD;

import com.NullPtr.Pontiland.controllers.HudController;
import com.NullPtr.Pontiland.entities.Jugador;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

import java.util.*;

public class HudPropiedades {

    private final HudController controller;
    private final Node hudRoot;
    private final BitmapFont font;
    private final float camWidth;
    private final float camHeight;

    private final Map<Integer, List<Integer>> propiedadesPorJugador = new HashMap<>();
    private final Map<Integer, Node> panelesPorJugador = new HashMap<>();

    private int jugadorActivo = 0;
    private Node panelVisibleActual = null;
    private Node panelEntrante = null;
    private boolean animandoPanel = false;
    private float animTimer = 0f;
    private static final float ANIM_DURACION = 0.5f;

    public HudPropiedades(SimpleApplication app, HudController controller, Node hudRoot) {
        this.controller = controller;
        this.hudRoot = hudRoot;
        this.font = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        this.camWidth = app.getCamera().getWidth();
        this.camHeight = app.getCamera().getHeight();

        crearPaneles(app);
    }

    private void crearPaneles(SimpleApplication app) {
        List<Jugador> jugadores = controller.getJugadores();

        for (Jugador j : jugadores) {
            Node panel = new Node("PanelPropiedadesJugador_" + j.getJugadorId());
            Quad fondo = new Quad(camWidth * 0.6f, 150f);
            Geometry fondoGeo = new Geometry("propFondo", fondo);
            Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.4f));
            fondoGeo.setMaterial(mat);
            fondoGeo.setLocalTranslation(camWidth * 0.2f, 20f, 0);
            panel.attachChild(fondoGeo);

            var titulo = new com.jme3.font.BitmapText(font);
            titulo.setText("Propiedades de " + j.getNombreJugador());
            titulo.setColor(ColorRGBA.White);
            titulo.setLocalTranslation(camWidth * 0.35f, 160f, 1f);
            panel.attachChild(titulo);

            Node contenido = new Node("PropiedadesContenido_" + j.getJugadorId());
            panel.attachChild(contenido);

            panel.setLocalTranslation(0, -200f, 0);
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
        propiedadesPorJugador.computeIfAbsent(jugadorId, k -> new ArrayList<>()).add(numCasilla);
        actualizarPanelPropiedades();
    }

    public void highlightActivePlayer(int playerIndex) {
        jugadorActivo = playerIndex;
        actualizarPanelPropiedades();
    }

    private void actualizarPanelPropiedades() {
        List<Jugador> jugadores = controller.getJugadores();
        if (jugadorActivo < 0 || jugadorActivo >= jugadores.size()) return;

        int jugadorId = jugadores.get(jugadorActivo).getJugadorId();
        Node nuevoPanel = panelesPorJugador.get(jugadorId);
        if (nuevoPanel == null) return;

        Node contenido = (Node) nuevoPanel.getChild("PropiedadesContenido_" + jugadorId);
        contenido.detachAllChildren();
        List<Integer> propiedades = propiedadesPorJugador.getOrDefault(jugadorId, List.of());

        float startX = camWidth * 0.25f;
        float baseY = 60f;
        float offset = 30f;

        for (int i = 0; i < propiedades.size(); i++) {
            int num = propiedades.get(i);
            var texto = new com.jme3.font.BitmapText(font);
            texto.setText("Propiedad " + num);
            texto.setColor(ColorRGBA.White);
            texto.setLocalTranslation(startX, baseY + i * offset, 2f);
            contenido.attachChild(texto);
        }

        if (panelVisibleActual != nuevoPanel) {
            animandoPanel = true;
            animTimer = 0f;
            panelEntrante = nuevoPanel;

            if (panelVisibleActual != null)
                panelVisibleActual.setCullHint(Node.CullHint.Inherit);

            panelEntrante.setCullHint(Node.CullHint.Inherit);
        } else {
            panelVisibleActual.setCullHint(Node.CullHint.Inherit);
        }
    }

    public void update(float tpf) {
        if (animandoPanel && panelEntrante != null) {
            animTimer += tpf;
            float progress = Math.min(animTimer / ANIM_DURACION, 1f);
            float eased = easeOutCubic(progress);

            if (panelVisibleActual != null) {
                panelVisibleActual.setLocalTranslation(0, -200f * eased, 0);
                if (progress >= 1f) panelVisibleActual.setCullHint(Node.CullHint.Always);
            }

            panelEntrante.setLocalTranslation(0, -200f * (1f - eased), 0);

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

