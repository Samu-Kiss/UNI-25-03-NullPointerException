package com.NullPtr.Pontiland.view.HUD;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.simsilica.lemur.*;
import com.simsilica.lemur.style.BaseStyles;

public class HudCompra {

    public interface HudCompraListener {
        void onSubastaSolicitada();
        void onComprarPropiedad(int numCasilla);
        void onFinalizarTurno();
    }

    private HudCompraListener listener;
    public void setListener(HudCompraListener listener) { this.listener = listener; }

    private final Node panel;
    private final float camWidth, camHeight;
    private final Button comprarBtn;
    private final Button subastarBtn;
    private final Label titulo;

    private boolean visible = false;
    private float animProgress = 0f;
    private float startX, targetX;
    private int currentCasilla = -1;

    public HudCompra(SimpleApplication app, Node parent) {
        if (GuiGlobals.getInstance() == null) {
            GuiGlobals.initialize(app);
            BaseStyles.loadGlassStyle();
            GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        }

        camWidth = app.getCamera().getWidth();
        camHeight = app.getCamera().getHeight();

        panel = new Node("HudCompra");

        float fondoWidth = 420;
        float fondoHeight = 520;

        Quad fondoQuad = new Quad(fondoWidth, fondoHeight);
        Geometry fondoGeo = new Geometry("PlantillaPropiedad", fondoQuad);

        Material fondoMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = app.getAssetManager().loadTexture("graphics/sprites/Plantillapropiedad.png");
        tex.setWrap(Texture.WrapMode.Clamp);
        tex.setMagFilter(Texture.MagFilter.Bilinear);
        tex.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
        fondoMat.setTexture("ColorMap", tex);
        fondoMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        fondoGeo.setQueueBucket(RenderQueue.Bucket.Gui);
        fondoGeo.setMaterial(fondoMat);
        float fondoY = camHeight * 0.5f - fondoHeight / 2f + 40f;
        fondoGeo.setLocalTranslation(0, fondoY, 0);
        panel.attachChild(fondoGeo);

        titulo = new Label("TARJETA DE PROPIEDAD");
        titulo.setColor(ColorRGBA.Black);
        titulo.setFontSize(24);
        titulo.setLocalTranslation(90, fondoY + fondoHeight - 40, 2f);
        panel.attachChild(titulo);

        float botonesYBase = fondoY - 60f;
        comprarBtn = crearBoton("[ COMPRAR ]", ColorRGBA.Green, 80, botonesYBase);
        subastarBtn = crearBoton("[ SUBASTAR ]", ColorRGBA.Yellow, 80, botonesYBase - 60f);

        panel.attachChild(comprarBtn);
        panel.attachChild(subastarBtn);

        startX = camWidth + 50;
        targetX = camWidth - fondoWidth - 50;

        panel.setCullHint(Node.CullHint.Always);
        parent.attachChild(panel);
    }

    private Button crearBoton(String texto, ColorRGBA color, float x, float y) {
        Button btn = new Button(texto);
        btn.setColor(color);
        btn.setFontSize(20);
        btn.setLocalTranslation(x, y, 2f);

        btn.addClickCommands(source -> {
            if (texto.contains("COMPRAR")) {
                ocultar();
                System.out.println("HUD: COMPRAR clicado.");
                if (listener != null) {
                    listener.onComprarPropiedad(currentCasilla);
                    listener.onFinalizarTurno();
                }
            } else if (texto.contains("SUBASTAR")) {
                ocultar();
                System.out.println("HUD: SUBASTAR clicado.");
                if (listener != null) listener.onSubastaSolicitada();
            }
        });

        return btn;
    }

    public void mostrarConCasilla(int numCasilla) {
        this.currentCasilla = numCasilla;
        titulo.setText("PROPIEDAD " + numCasilla);
        mostrar();
    }

    public void mostrar() {
        panel.setCullHint(Node.CullHint.Inherit);
        visible = true;
        animProgress = 0;
    }

    public void ocultar() {
        visible = false;
        animProgress = 0;
        panel.setCullHint(Node.CullHint.Inherit);
    }

    public void update(float tpf) {
        float speed = 2f;
        if (visible) {
            if (animProgress < 1f) {
                animProgress += tpf * speed;
                float eased = easeOutCubic(animProgress);
                float newX = startX + (targetX - startX) * eased;
                panel.setLocalTranslation(newX, 0, 0);
            }
        } else {
            if (animProgress < 1f) {
                animProgress += tpf * speed;
                float eased = easeOutCubic(animProgress);
                float newX = targetX + (startX - targetX) * eased;
                panel.setLocalTranslation(newX, 0, 0);
            } else {
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









