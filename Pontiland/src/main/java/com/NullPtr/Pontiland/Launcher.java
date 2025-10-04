package com.NullPtr.Pontiland;

import com.NullPtr.Pontiland.controllers.LanzamientoDadosController;
import com.NullPtr.Pontiland.services.DiceService;
import com.NullPtr.Pontiland.view.Scene;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.system.AppSettings;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Aplicación principal que inicia el motor jMonkeyEngine y delega la construcción
 * de la escena al {@link Scene} y la interacción al {@link LanzamientoDadosController}.
 *
 * Esta clase extiende {@link SimpleApplication} y, por tanto, es la única clase
 * con un método {@code main} que arranca la aplicación. Configura los ajustes de
 * la ventana, adjunta el estado de físicas y gestiona el ciclo de actualización.
 */
public class Launcher extends SimpleApplication {
    private final BulletAppState bulletAppState = new BulletAppState();
    private final DiceService diceService = new DiceService();
    private final AtomicReferenceArray<Byte> resultados = new AtomicReferenceArray<>(2);
    private LanzamientoDadosController lanzamientoDadosController;
    private Scene scene;

    /**
     * Método de entrada principal que configura y arranca la aplicación.
     *
     * @param args Argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        Launcher app = new Launcher();
        AppSettings settings = new AppSettings(true);
        try {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) (screen.width * 0.75);
            int height = (int) (screen.height * 0.75);
            settings.setResolution(width, height);
        } catch (HeadlessException e) {
            settings.setResolution(1280, 720);
        }
        settings.setTitle("Pontiland");
        settings.setVSync(true);
        settings.setFullscreen(false);
        settings.setGammaCorrection(true);
        app.setShowSettings(false);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setAccuracy(1f / 120f);
        bulletAppState.getPhysicsSpace().setMaxSubSteps(2);
        lanzamientoDadosController = new LanzamientoDadosController(diceService, resultados);
        lanzamientoDadosController.registerInputs(getInputManager());
        scene = new Scene(this, bulletAppState, lanzamientoDadosController);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (scene != null) {
            scene.update(tpf);
        }
        if (lanzamientoDadosController != null) {
            lanzamientoDadosController.update();
        }
    }
}