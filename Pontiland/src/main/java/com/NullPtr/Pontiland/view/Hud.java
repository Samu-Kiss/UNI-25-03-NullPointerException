package com.NullPtr.Pontiland.view;

import com.NullPtr.Pontiland.controllers.HudController;
import com.NullPtr.Pontiland.controllers.HudController.PlayerHudData;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD principal del juego, fusionado y con soporte de botones de subasta (+20, +50, +100, RETIRARSE).
 */
public class Hud extends BaseAppState implements ActionListener, AnalogListener {

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
    private Node propiedadesPanel;
    private Node tarjetaPanel;
    private Node subastaPanel;

    private BitmapText comprarBtn;
    private BitmapText subastarBtn;

    // botones de subasta nuevos
    private BitmapText plus20Btn;
    private BitmapText plus50Btn;
    private BitmapText plus100Btn;
    private BitmapText retirarseBtn;
    private BitmapText auctionValueText;
    private int auctionValue = 0;

    private BitmapFont font;
    private InputManager inputManager;

    private float camWidth;
    private float camHeight;
    private boolean animStarted = false;
    private float delayTimer = 0f;
    private float delayDuration = 0f;

    private boolean tarjetaVisible = false;

    public Hud(HudController controller) {
        this.controller = controller;
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
        crearPanelTarjetaPropiedad(simpleApp);
        crearPanelSubasta(simpleApp);

        guiNode.attachChild(hudRoot);

        // mapear entradas (los listeners se añaden/remueven en onEnable/onDisable)
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("ToggleTarjeta", new KeyTrigger(KeyInput.KEY_N));
    }

    /** ---------- PANEL DE JUGADORES (animado) ---------- **/
    private void crearPanelJugadores(SimpleApplication app) {
        float yStart = camHeight - 200f;
        float targetX = 50f;
        float casillaSize = 128f;
        float spacing = 50f;

        List<PlayerHudData> players = controller.getPlayersData();
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
            PlayerHudData p = players.get(i);
            HudEntry entry = new HudEntry();

            float finalY = yStart - (i * (casillaSize + spacing));
            entry.startX = -casillaSize - 200f;
            entry.targetX = targetX;
            entry.y = finalY;

            Texture tex = app.getAssetManager().loadTexture(casillaPaths[i % casillaPaths.length]);
            tex.setWrap(Texture.WrapMode.Clamp);

            Quad quad = new Quad(casillaSize, casillaSize);
            Geometry casilla = new Geometry("casilla" + i, quad);
            Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setTexture("ColorMap", tex);
            mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
            casilla.setMaterial(mat);
            casilla.setLocalTranslation(entry.startX, finalY, 0);

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

    /** ---------- PANEL DE PROPIEDADES ---------- **/
    private void crearPanelPropiedades(SimpleApplication app) {
        propiedadesPanel = new Node("PropiedadesPanel");

        Quad fondo = new Quad(camWidth * 0.6f, 100f);
        Geometry fondoGeo = new Geometry("propFondo", fondo);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.4f));
        fondoGeo.setMaterial(mat);
        fondoGeo.setLocalTranslation(camWidth * 0.2f, 20f, 0);
        propiedadesPanel.attachChild(fondoGeo);

        BitmapText titulo = new BitmapText(font);
        titulo.setText("Propiedades del Jugador");
        titulo.setColor(ColorRGBA.White);
        titulo.setLocalTranslation(camWidth * 0.45f, 100f, 1f);
        propiedadesPanel.attachChild(titulo);

        hudRoot.attachChild(propiedadesPanel);
    }

    /** ---------- PANEL TARJETA DE PROPIEDAD ---------- **/
    private void crearPanelTarjetaPropiedad(SimpleApplication app) {
        tarjetaPanel = new Node("TarjetaPanel");

        Quad tarjeta = new Quad(250, 350);
        Geometry geo = new Geometry("tarjeta", tarjeta);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1, 1, 1, 0.9f));
        geo.setMaterial(mat);
        geo.setLocalTranslation(camWidth - 300, camHeight / 2 - 150, 0);
        tarjetaPanel.attachChild(geo);

        BitmapText titulo = new BitmapText(font);
        titulo.setText("TARJETA DE PROPIEDAD");
        titulo.setColor(ColorRGBA.Black);
        titulo.setLocalTranslation(camWidth - 290, camHeight / 2 + 180, 1f);
        tarjetaPanel.attachChild(titulo);

        comprarBtn = new BitmapText(font);
        comprarBtn.setText("[ COMPRAR ]");
        comprarBtn.setColor(ColorRGBA.Green);
        comprarBtn.setLocalTranslation(camWidth - 260, camHeight / 2 - 200, 1f);
        tarjetaPanel.attachChild(comprarBtn);

        subastarBtn = new BitmapText(font);
        subastarBtn.setText("[ SUBASTAR ]");
        subastarBtn.setColor(ColorRGBA.Yellow);
        subastarBtn.setLocalTranslation(camWidth - 260, camHeight / 2 - 230, 1f);
        tarjetaPanel.attachChild(subastarBtn);

        tarjetaPanel.setCullHint(Node.CullHint.Always);
        hudRoot.attachChild(tarjetaPanel);
    }

    /** ---------- PANEL DE SUBASTA (con botones +20/+50/+100/RETIRARSE) ---------- **/
    private void crearPanelSubasta(SimpleApplication app) {
        subastaPanel = new Node("SubastaPanel");

        Quad fondo = new Quad(400, 250);
        Geometry geo = new Geometry("subastaFondo", fondo);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.1f, 0.1f, 0.1f, 0.8f));
        geo.setMaterial(mat);
        geo.setLocalTranslation(camWidth / 2 - 200, camHeight / 2 - 125, 0);
        subastaPanel.attachChild(geo);

        BitmapText titulo = new BitmapText(font);
        titulo.setText("SUBASTA ACTIVA");
        titulo.setColor(ColorRGBA.White);
        titulo.setLocalTranslation(camWidth / 2 - 100, camHeight / 2 + 100, 1f);
        subastaPanel.attachChild(titulo);

        // Texto de valor actual (se actualiza al clicar)
        auctionValue = 0;
        auctionValueText = new BitmapText(font);
        auctionValueText.setText("Valor actual: $" + auctionValue);
        auctionValueText.setColor(ColorRGBA.Cyan);
        auctionValueText.setLocalTranslation(camWidth / 2 - 100, camHeight / 2 + 60, 1f);
        subastaPanel.attachChild(auctionValueText);

        // Botones de incremento y retirarse (dispuestos horizontalmente)
        plus20Btn = new BitmapText(font);
        plus20Btn.setText("[ +20 ]");
        plus20Btn.setColor(ColorRGBA.Green);
        plus20Btn.setLocalTranslation(camWidth / 2 - 160, camHeight / 2 + 10, 1f);
        subastaPanel.attachChild(plus20Btn);

        plus50Btn = new BitmapText(font);
        plus50Btn.setText("[ +50 ]");
        plus50Btn.setColor(ColorRGBA.Orange);
        plus50Btn.setLocalTranslation(camWidth / 2 - 60, camHeight / 2 + 10, 1f);
        subastaPanel.attachChild(plus50Btn);

        plus100Btn = new BitmapText(font);
        plus100Btn.setText("[ +100 ]");
        plus100Btn.setColor(ColorRGBA.Blue);
        plus100Btn.setLocalTranslation(camWidth / 2 + 40, camHeight / 2 + 10, 1f);
        subastaPanel.attachChild(plus100Btn);

        retirarseBtn = new BitmapText(font);
        retirarseBtn.setText("[ RETIRARSE ]");
        retirarseBtn.setColor(ColorRGBA.Red);
        retirarseBtn.setLocalTranslation(camWidth / 2 - 40, camHeight / 2 - 30, 1f);
        subastaPanel.attachChild(retirarseBtn);

        subastaPanel.setCullHint(Node.CullHint.Always);
        hudRoot.attachChild(subastaPanel);
    }

    /** ---------- MÉTODOS DE VISIBILIDAD ---------- **/
    public void mostrarTarjetaPropiedad() {
        tarjetaPanel.setCullHint(Node.CullHint.Inherit);
        tarjetaVisible = true;
    }

    public void ocultarTarjetaPropiedad() {
        tarjetaPanel.setCullHint(Node.CullHint.Always);
        tarjetaVisible = false;
    }

    public void mostrarSubasta() {
        // reinicia valor de subasta y muestra panel
        auctionValue = 0;
        if (auctionValueText != null) auctionValueText.setText("Valor actual: $" + auctionValue);
        subastaPanel.setCullHint(Node.CullHint.Inherit);
    }

    public void ocultarSubasta() {
        subastaPanel.setCullHint(Node.CullHint.Always);
    }

    /** ---------- DETECCIÓN DE CLICS ---------- **/
    private boolean isOverText(BitmapText text, Vector2f click) {
        if (text == null) return false;
        float x = text.getLocalTranslation().x;
        float y = text.getLocalTranslation().y - text.getLineHeight();
        float w = text.getLineWidth();
        float h = text.getLineHeight();
        // comprobación directa
        if (click.x >= x && click.x <= x + w && click.y >= y && click.y <= y + h) return true;
        // chequeo alternativo por si el sistema tiene Y invertida
        float altY = camHeight - click.y;
        return (altY >= y && altY <= y + h && click.x >= x && click.x <= x + w);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;

        if ("ToggleTarjeta".equals(name)) {
            if (tarjetaVisible) ocultarTarjetaPropiedad();
            else mostrarTarjetaPropiedad();
            return;
        }

        if ("Click".equals(name)) {
            Vector2f click = inputManager.getCursorPosition();

            // botones tarjeta
            if (tarjetaVisible && isOverText(comprarBtn, click)) {
                ocultarTarjetaPropiedad();
                // TODO: notificar controlador para lógica real (controller.comprarPropiedad(...))
                System.out.println("HUD: COMPRAR clicado (visual).");
                return;
            } else if (tarjetaVisible && isOverText(subastarBtn, click)) {
                ocultarTarjetaPropiedad();
                mostrarSubasta();
                System.out.println("HUD: SUBASTAR clicado -> se muestra panel de subasta.");
                return;
            }

            // botones subasta: +20, +50, +100, RETIRARSE
            if (subastaPanel.getCullHint() == Node.CullHint.Inherit) {
                if (isOverText(plus20Btn, click)) {
                    auctionValue += 20;
                    auctionValueText.setText("Valor actual: $" + auctionValue);
                    System.out.println("Subasta: +20 -> " + auctionValue);

                } else if (isOverText(plus50Btn, click)) {
                    auctionValue += 50;
                    auctionValueText.setText("Valor actual: $" + auctionValue);
                    System.out.println("Subasta: +50 -> " + auctionValue);

                } else if (isOverText(plus100Btn, click)) {
                    auctionValue += 100;
                    auctionValueText.setText("Valor actual: $" + auctionValue);
                    System.out.println("Subasta: +100 -> " + auctionValue);

                } else if (isOverText(retirarseBtn, click)) {
                    System.out.println("Subasta: jugador se retira (visual).");
                    ocultarSubasta();

                }
            }
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) { }

    /** ---------- ANIMACIÓN DE ENTRADA ---------- **/
    @Override
    public void update(float tpf) {
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

            e.casilla.setLocalTranslation(newX, e.y, 0);
            centerText(e.personajeText, newX, e.y, 128f, 0);
            centerText(e.nameText, newX, e.y, 128f, -25);
            centerText(e.dineroText, newX, e.y, 128f, -50);
            centerText(e.estadoText, newX, e.y, 128f, -80);
        }

        if (allDone) animStarted = false;
    }

    /** Utilidades **/
    private void centerText(BitmapText text, float x, float y, float size, float offsetY) {
        float width = text.getLineWidth();
        float textX = x + (size - width) / 2f;
        float textY = y + (size / 2f) + offsetY + 20f;
        text.setLocalTranslation(textX, textY, 10f);
    }

    private float easeOutCubic(float t) {
        return (float) (1 - Math.pow(1 - t, 3));
    }

    @Override
    protected void onEnable() {
        // registrar listener cuando el estado se activa
        inputManager.addListener(this, "Click", "ToggleTarjeta");
        tarjetaVisible = (tarjetaPanel.getCullHint() == Node.CullHint.Inherit);
    }

    @Override
    protected void onDisable() {
        inputManager.removeListener(this);
    }

    @Override
    protected void cleanup(Application app) {
        if (inputManager != null) {
            inputManager.deleteMapping("Click");
            inputManager.deleteMapping("ToggleTarjeta");
            inputManager.removeListener(this);
        }
    }
}












