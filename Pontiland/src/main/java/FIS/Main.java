package FIS;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.OBJLoader;
import com.jme3.material.Material;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.texture.Texture;

public class Main extends SimpleApplication {
    private Spatial board;

    public static void main(String[] args) {
        Main app = new Main();
        // Configure window to be half of current screen size
        java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.max(640, screen.width / 2); // enforce a sensible minimum
        int height = Math.max(360, screen.height / 2);
        AppSettings settings = new AppSettings(true);
        settings.setResolution(width, height);
        settings.setTitle("Pontiland");
        settings.setVSync(true);
        settings.setFullscreen(false);
        app.setShowSettings(false); // skip default settings dialog
        app.setSettings(settings);
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

        // Apply improved texture filtering to combat low-res appearance
        applyTextureQualitySettings(rootNode);

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
        // Reduce near clipping plane so geometry is not clipped when camera is very close
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.05f, 2000f);
        flyCam.setMoveSpeed(15f);
    }

    private void applyTextureQualitySettings(Spatial root) {
        root.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Geometry geom) {
                Material mat = geom.getMaterial();
                if (mat == null) return;
                for (MatParam param : mat.getParams()) {
                    if (param instanceof MatParamTexture) {
                        Texture tex = ((MatParamTexture) param).getTextureValue();
                        if (tex != null) {
                            // Increase anisotropic filtering for sharper angled textures
                            tex.setAnisotropicFilter(8); // can raise to 16 if performance allows
                            // Use trilinear minification filtering and bilinear magnification
                            tex.setMinFilter(Texture.MinFilter.Trilinear);
                            tex.setMagFilter(Texture.MagFilter.Bilinear);
                            // Simple debug output (once per texture ref hash)
                            System.out.println("Adjusted texture: " + tex.getName() +
                                    " size=" + tex.getImage().getWidth() + "x" + tex.getImage().getHeight() +
                                    " aniso=" + tex.getAnisotropicFilter());
                        }
                    }
                }
            }
        });
    }
}
