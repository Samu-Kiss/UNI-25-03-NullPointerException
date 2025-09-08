package FIS;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.OBJLoader;

public class Main extends SimpleApplication {
    private Spatial board;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Ensure OBJ loader is registered (often automatic via jme3-plugins)
        assetManager.registerLoader(OBJLoader.class, "obj");

        // Load model from classpath resources (current structure: src/main/resources/assets/3DModels/...)
        board = assetManager.loadModel("assets/3DModels/board/Tablero.obj");

        rootNode.attachChild(board);

        // Optional: adjust scale or position if model is too large/small
        // board.scale(0.5f);
        board.center();

        // Lighting
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1f, -2f, -1f).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.35f));
        rootNode.addLight(ambient);

        // Camera
        cam.setLocation(new Vector3f(0, 4f, 10f));
        cam.lookAt(board.getWorldTranslation(), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(15f);
    }
}
