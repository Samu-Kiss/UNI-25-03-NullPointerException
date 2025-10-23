package com.NullPtr.Pontiland.view.HUD;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.simsilica.lemur.*;

public class HudSubasta {

    private final Node panel;
    private final float camWidth, camHeight;

    private final Label auctionLabel;
    private final Label titulo;
    private final Button plus20, plus50, plus100, retirarse;

    private boolean visible = false;
    private float animProgress = 0f;
    private int auctionValue = 0;

    private float startY, targetY;

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

        // ✅ Asegurar transparencia de la imagen (sin bordes negros)
        mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        geo.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Gui);

        geo.setMaterial(mat);

        // Centrar imagen en pantalla
        float fondoX = camWidth * 0.5f - fondoWidth / 2f;
        float fondoY = camHeight * 0.5f - fondoHeight / 2f;
        geo.setLocalTranslation(fondoX, fondoY, 0);
        panel.attachChild(geo);

        // --- Configurar estilos base ---
        float centerX = camWidth * 0.5f;
        float baseY = fondoY + fondoHeight - 120; // Desde la parte superior del fondo

        // --- Título ---
        titulo = new Label("SUBASTA ACTIVA");
        titulo.setColor(ColorRGBA.Black);
        titulo.setFontSize(32);
        titulo.setLocalTranslation(centerX - (titulo.getPreferredSize().x / 2f), baseY, 1f);
        panel.attachChild(titulo);

        // --- Valor actual ---
        auctionLabel = new Label("Valor actual: $" + auctionValue);
        auctionLabel.setColor(ColorRGBA.Black);
        auctionLabel.setFontSize(28);
        auctionLabel.setLocalTranslation(centerX - 120, baseY - 60, 1f);
        panel.attachChild(auctionLabel);

        // --- Botones centrados horizontalmente ---
        float buttonY = fondoY + 240;

        plus20 = crearBoton("[ +20 ]", ColorRGBA.Green, centerX - 180, buttonY);
        plus50 = crearBoton("[ +50 ]", ColorRGBA.Orange, centerX - 50, buttonY);
        plus100 = crearBoton("[ +100 ]", ColorRGBA.Blue, centerX + 80, buttonY);
        retirarse = crearBoton("[ RETIRARSE ]", ColorRGBA.Red, centerX - 70, buttonY - 80);

        panel.attachChild(plus20);
        panel.attachChild(plus50);
        panel.attachChild(plus100);
        panel.attachChild(retirarse);

        // --- Animación ---
        startY = -700;
        targetY = 0;

        panel.setCullHint(Node.CullHint.Always);
        parent.attachChild(panel);
    }

    private Button crearBoton(String texto, ColorRGBA color, float x, float y) {
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
            if (texto.contains("+20")) {
                auctionValue += 20;
            } else if (texto.contains("+50")) {
                auctionValue += 50;
            } else if (texto.contains("+100")) {
                auctionValue += 100;
            } else if (texto.contains("RETIRARSE")) {
                System.out.println("Jugador se retiró de la subasta.");
                ocultar();
            }
            actualizarValor();
        });

        return btn;
    }

    private void actualizarValor() {
        auctionLabel.setText("Valor actual: $" + auctionValue);
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
        float speed = 2f;
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

    private float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }

    public boolean estaVisible() {
        return visible;
    }
}





