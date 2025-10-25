package com.NullPtr.Pontiland.view.HUD;

import com.NullPtr.Pontiland.entities.Jugador;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.simsilica.lemur.*;

import java.util.ArrayList;
import java.util.List;

public class HudSubasta {

    public interface HudSubastaListener {
        void onJugadorPuja(Jugador jugador, int nuevaOferta);
        void onJugadorSeRetira(Jugador jugador);
        void onSubastaTerminada(Jugador ganador, int ofertaFinal);
    }

    private HudSubastaListener listener;
    public void setListener(HudSubastaListener listener) {
        this.listener = listener;
    }

    private final Node panel;
    private final float camWidth, camHeight;

    private final Label auctionLabel;
    private final Label titulo;
    private final Label turnoLabel;
    private final Button plus20, plus50, plus100, retirarse;

    private boolean visible = false;
    private float animProgress = 0f;
    private int auctionValue = 0;

    private float startY, targetY;

    // 🔁 Lógica de subasta
    private List<Jugador> participantes = new ArrayList<>();
    private Jugador jugadorActual;
    private int indiceActual = 0;
    private int propiedadSubastada = -1;

    public HudSubasta(SimpleApplication app, Node parent) {
        camWidth = app.getCamera().getWidth();
        camHeight = app.getCamera().getHeight();
        panel = new Node("HudSubasta");

        // --- Fondo con imagen ---
        float fondoWidth = 500;
        float fondoHeight = 625;

        Quad fondo = new Quad(fondoWidth, fondoHeight);
        Geometry geo = new Geometry("subastaFondo", fondo);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");

        Texture tex = app.getAssetManager().loadTexture("graphics/sprites/Subasta.png");
        tex.setWrap(Texture.WrapMode.Clamp);
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        geo.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Gui);
        geo.setMaterial(mat);

        float fondoX = camWidth * 0.5f - fondoWidth / 2f;
        float fondoY = camHeight * 0.5f - fondoHeight / 2f;
        geo.setLocalTranslation(fondoX, fondoY, 0);
        panel.attachChild(geo);

        float centerX = camWidth * 0.5f;
        float baseY = fondoY + fondoHeight - 120;

        // --- Título ---
        titulo = new Label("SUBASTA ACTIVA");
        titulo.setColor(ColorRGBA.Black);
        titulo.setFontSize(32);
        titulo.setLocalTranslation(centerX - 140, baseY, 1f);
        panel.attachChild(titulo);

        // --- Valor actual ---
        auctionLabel = new Label("Valor actual: $" + auctionValue);
        auctionLabel.setColor(ColorRGBA.Black);
        auctionLabel.setFontSize(28);
        auctionLabel.setLocalTranslation(centerX - 120, baseY - 60, 1f);
        panel.attachChild(auctionLabel);

        // --- Turno ---
        turnoLabel = new Label("Turno de: ---");
        turnoLabel.setColor(ColorRGBA.DarkGray);
        turnoLabel.setFontSize(26);
        turnoLabel.setLocalTranslation(centerX - 120, baseY - 110, 1f);
        panel.attachChild(turnoLabel);

        // --- Botones ---
        float buttonY = fondoY + 240;
        plus20 = crearBoton("[ +20 ]", ColorRGBA.Green, centerX - 180, buttonY, 20);
        plus50 = crearBoton("[ +50 ]", ColorRGBA.Orange, centerX - 50, buttonY, 50);
        plus100 = crearBoton("[ +100 ]", ColorRGBA.Blue, centerX + 80, buttonY, 100);
        retirarse = crearBoton("[ RETIRARSE ]", ColorRGBA.Red, centerX - 70, buttonY - 80, 0);

        panel.attachChild(plus20);
        panel.attachChild(plus50);
        panel.attachChild(plus100);
        panel.attachChild(retirarse);

        // --- Animación ---
        startY = -700;
        targetY = 0;;

        panel.setCullHint(Node.CullHint.Always);
        parent.attachChild(panel);
    }

    /** Inicia la subasta con los jugadores y la casilla dada */
    public void iniciarSubasta(List<Jugador> jugadores, int propiedad) {
        this.participantes = new ArrayList<>(jugadores);
        this.propiedadSubastada = propiedad;
        this.indiceActual = 0;
        this.jugadorActual = participantes.get(0);
        this.auctionValue = 0;
        this.visible = true;
        this.animProgress = 0;
        actualizarTurno();
        panel.setCullHint(Node.CullHint.Inherit);
    }

    private Button crearBoton(String texto, ColorRGBA color, float x, float y, int incremento) {
        Button btn = new Button(texto);
        btn.setColor(color);
        btn.setFontSize(26);
        btn.setLocalTranslation(x, y, 1f);

        btn.addCommands(Button.ButtonAction.HighlightOn, s -> {
            btn.setLocalScale(1.15f);
            btn.setColor(color.mult(1.2f));
        });
        btn.addCommands(Button.ButtonAction.HighlightOff, s -> {
            btn.setLocalScale(1f);
            btn.setColor(color);
        });

        btn.addClickCommands(source -> {
            if (texto.contains("RETIRARSE")) {
                jugadorSeRetira();
            } else {
                jugadorPuja(incremento);
            }
        });

        return btn;
    }

    private void jugadorPuja(int incremento) {
        if (jugadorActual == null) return;
        auctionValue += incremento;
        actualizarValor();

        if (listener != null)
            listener.onJugadorPuja(jugadorActual, auctionValue);

        siguienteJugador();
    }

    private void jugadorSeRetira() {
        if (jugadorActual == null) return;

        if (listener != null)
            listener.onJugadorSeRetira(jugadorActual);

        participantes.remove(jugadorActual);

        if (participantes.size() == 1) {
            Jugador ganador = participantes.get(0);
            if (listener != null)
                listener.onSubastaTerminada(ganador, auctionValue);
            ocultar();
            return;
        }

        if (indiceActual >= participantes.size())
            indiceActual = 0;

        jugadorActual = participantes.get(indiceActual);
        actualizarTurno();
    }

    private void siguienteJugador() {
        if (participantes.isEmpty()) return;
        indiceActual = (indiceActual + 1) % participantes.size();
        jugadorActual = participantes.get(indiceActual);
        actualizarTurno();
    }

    private void actualizarValor() {
        auctionLabel.setText("Valor actual: $" + auctionValue);
    }

    private void actualizarTurno() {
        turnoLabel.setText("Turno de: " + jugadorActual.getNombreJugador());
    }

    public void mostrar() {
        panel.setCullHint(Node.CullHint.Inherit);
        visible = true;
        animProgress = 0;
    }

    public void ocultar() {
        visible = false;
        animProgress = 0;
    }

    public void update(float tpf) {
        float speed = 2.5f;
        if (visible) {
            if (animProgress < 1f) {
                animProgress += tpf * speed;
                float eased = easeOutCubic(animProgress);
                float newY = startY + (targetY - startY) * eased;
                panel.setLocalTranslation(0, newY, 0);
            }
        } else {
            if (animProgress < 1f) {
                animProgress += tpf * speed;
                float eased = easeOutCubic(animProgress);
                float newY = targetY + (startY - targetY) * eased;
                panel.setLocalTranslation(0, newY, 0);
                if (animProgress >= 1f)
                    panel.setCullHint(Node.CullHint.Always);
            }
        }
    }

    public int getPropiedadSubastada() {
        return propiedadSubastada;
    }


    private float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }

}




