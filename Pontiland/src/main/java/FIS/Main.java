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
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.ToneMapFilter;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class Main extends SimpleApplication {

    // Tone mapping filter reference so it can be tweaked later if desired
    private ToneMapFilter toneMap;
    private float whitePointScalar = 1f; // runtime adjustable
    private LightProbe sceneProbe; // keep reference to scale intensity if needed

    public static void main(String[] args) {
        Main app = new Main();
        // Enable gamma correction so HDR values are mapped properly for display
        AppSettings settings = new AppSettings(true);
        settings.setGammaCorrection(true);
        settings.setTitle("Pontiland");
        app.setSettings(settings);
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
        setUpPost(); // tone mapping / exposure control

        Spatial object = assetManager.loadModel("3DModels/Calle 45.glb");
        object.setLocalTranslation(-1f, -1f, -1f);
        rootNode.attachChild(object);

        initExposureControls();
    }

    private void initExposureControls() {
        inputManager.addMapping("ExposureUp", new KeyTrigger(KeyInput.KEY_ADD), new KeyTrigger(KeyInput.KEY_EQUALS));
        inputManager.addMapping("ExposureDown", new KeyTrigger(KeyInput.KEY_SUBTRACT), new KeyTrigger(KeyInput.KEY_MINUS));
        inputManager.addListener(exposureListener, "ExposureUp", "ExposureDown");
    }

    private final ActionListener exposureListener = (name, isPressed, tpf) -> {
        if (isPressed) return; // act on key release to avoid rapid repeats
        if ("ExposureUp".equals(name)) {
            whitePointScalar = Math.min(whitePointScalar + 0.1f, 4f);
            updateToneMap();
        } else if ("ExposureDown".equals(name)) {
            whitePointScalar = Math.max(whitePointScalar - 0.1f, 0.1f);
            updateToneMap();
        }
        System.out.println("WhitePoint=" + whitePointScalar);
    };

    private void updateToneMap() {
        if (toneMap != null) {
            toneMap.setWhitePoint(new Vector3f(whitePointScalar, whitePointScalar, whitePointScalar));
        }
    }

    private void setUpSkyAndEnvironment() {
        // Load HDRI as sky (equirectangular map)
        Texture hdr = assetManager.loadTexture("HDRI/citrus_orchard_road_puresky_4k.hdr");
        Spatial sky = SkyFactory.createSky(assetManager, hdr, SkyFactory.EnvMapType.EquirectMap);
        rootNode.attachChild(sky);

        // Generate a light probe for PBR image-based lighting.
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
            // Scale probe intensity to reduce indirect over-brightness
            probe.setColor(ColorRGBA.White.mult(0.6f));
            ((Main) sapp).sceneProbe = probe;
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
        // Lower intensities to avoid wash-out when combined with HDR sky light probe
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.18f)); // lowered again
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White.mult(0.65f)); // lowered
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }

    private void setUpPost() {
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        toneMap = new ToneMapFilter(new Vector3f(whitePointScalar, whitePointScalar, whitePointScalar));
        fpp.addFilter(toneMap);
        viewPort.addProcessor(fpp);
    }
}