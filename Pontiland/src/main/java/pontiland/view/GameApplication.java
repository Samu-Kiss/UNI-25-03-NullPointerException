package pontiland.view;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

import java.awt.*;

/**
 * Main JMonkeyEngine 3 application. Opens a window at 75% of the primary screen size.
 */
public class GameApplication extends SimpleApplication {

    public static void main(String[] args) {
        GameApplication app = new GameApplication();
        AppSettings settings = new AppSettings(true);

        // Determine 75% screen size (primary monitor)
        try {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) (screen.width * 0.75);
            int height = (int) (screen.height * 0.75);
            settings.setResolution(width, height);
        } catch (HeadlessException e) {
            // Fallback if AWT headless (e.g., CI environment)
            settings.setResolution(1280, 720);
        }

        settings.setTitle("Pontiland");
        settings.setVSync(true);
        settings.setFullscreen(false);
        settings.setGammaCorrection(true);
        app.setShowSettings(false); // Skip default settings dialog
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Basic test geometry so we can see something
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Cyan);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
        cam.setLocation(new Vector3f(3,3,6));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(10f);
    }
}

