package FIS;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.LightProbeFactory;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Make resources under src/main/resources/assets available via classpath
        assetManager.registerLocator("assets", ClasspathLocator.class);

    // Reduce near clipping so geometry doesn't disappear when the camera gets very close
    cam.setFrustumNear(0.05f);
    // Set a normal vertical FOV (in degrees) to avoid the ultra-wide look
    float aspect = (float) cam.getWidth() / (float) cam.getHeight();
    cam.setFrustumPerspective(45f, aspect, cam.getFrustumNear(), cam.getFrustumFar());

        setUpSkyAndEnvironment();
        setUpLight();

        Spatial object = assetManager.loadModel("3DModels/Board.glb");
        object.setLocalTranslation(-1f, -1f, -1f);
        rootNode.attachChild(object);
    }

    private void setUpSkyAndEnvironment() {
        // Load HDRI as sky (equirectangular map)
        Texture hdr = assetManager.loadTexture("HDRI/citrus_orchard_road_puresky_4k.hdr");
        Spatial sky = SkyFactory.createSky(assetManager, hdr, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);

        // Generate a light probe for PBR image-based lighting.
        // We attach an EnvironmentCamera and a tiny AppState that waits until the camera is initialized,
        // then creates the probe (avoids NPE from calling too early).
        EnvironmentCamera envCam = new EnvironmentCamera(256, Vector3f.ZERO);
        stateManager.attach(envCam);
        stateManager.attach(new ProbeInitState(envCam));
    }

    /** Delays probe creation until the EnvironmentCamera is initialized by the application. */
    private static class ProbeInitState extends BaseAppState {
        private final EnvironmentCamera envCam;

        private ProbeInitState(EnvironmentCamera envCam) {
            this.envCam = envCam;
        }

        @Override
        protected void initialize(Application app) {
            // no-op
        }

        @Override
        public void update(float tpf) {
            if (envCam.getApplication() == null) {
                return; // wait one or two frames until the camera is bound to the app
            }
            SimpleApplication sapp = (SimpleApplication) getApplication();
            LightProbe probe = LightProbeFactory.makeProbe(envCam, sapp.getRootNode());
            probe.getArea().setRadius(100f);
            sapp.getRootNode().addLight(probe);
            getStateManager().detach(this);
        }

        @Override
        protected void cleanup(Application app) { /* no-op */ }

        @Override
        protected void onEnable() { /* no-op */ }

        @Override
        protected void onDisable() { /* no-op */ }
    }

    private void setUpLight() {
        // Basic ambient + directional so the model is visible
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }
}