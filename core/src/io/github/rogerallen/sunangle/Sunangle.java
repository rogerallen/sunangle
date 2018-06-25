package io.github.rogerallen.sunangle;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;

public class Sunangle extends ApplicationAdapter {

    private Drawable compassFrontPlane, clockFrontPlane, compassBackPlane, clockBackPlane;
    private Matrix4 clockProjMatrix;
    private PerspectiveCamera cam;
    private long startTime;

    private Sunobserver obs;
    private float latitude, longitude, day;

    private Skin skin;
    private Stage stage;
    private Label latLabel, lonLabel, dayLabel;

    @Override
    public void create() {

        Gdx.app.setLogLevel(Application.LOG_INFO); // LOG_NONE, LOG_DEBUG, LOG_ERROR, LOG_INFO
        Gdx.app.log("Sunangle", "libGDX Version = " + com.badlogic.gdx.Version.VERSION);

        // initialize the observer
        day = 0f;
        latitude = 45f; // (45 + (25.0 / 60.0));
        longitude = -122f; // (-122 - (41.0 / 60.0));
        Gdx.app.log("Sunangle", "lat = " + latitude + " lon = " + longitude);
        Date now = new Date();
        obs = new Sunobserver(latitude, longitude, now);

        setClockMatrix();

        startTime = TimeUtils.millis();

        createGnomonGeometry();

        createStage();

        //set up window into our virtual space
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 2f, -2f);
        cam.lookAt(0, 0, 0);
        cam.near = 0.1f;
        cam.far = 1000f;
        cam.update();

        CameraInputController camController = new CameraInputController(cam);
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(multiplexer);
        //Gdx.input.setInputProcessor(camController);

        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    private void createGnomonGeometry() {
        compassFrontPlane = new Drawable(
                "vs_pct.glsl", "fs_ct.glsl", "compass.png",
                new float[]{
                        // (x, y, z),     (r, g, b, a),     (s, t),
                        -1f, 0f, -1f, 0f, 0f, 0f, 1f, 1f, 1f,
                        -1f, 0f, 1f, 0f, 1f, 0f, 1f, 1f, 0f,
                        1f, 0f, -1f, 1f, 0f, 0f, 1f, 0f, 1f,
                        1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f
                });

        compassBackPlane = new Drawable(
                "vs_pct.glsl", "fs_ct.glsl", "compass_back.png",
                new float[]{
                        // (x, y, z),     (r, g, b, a),     (s, t),
                        -1f, 0f, -1f, 0f, 0f, 0f, 1f, 0f, 1f,
                        1f, 0f, -1f, 1f, 0f, 0f, 1f, 1f, 1f,
                        -1f, 0f, 1f, 0f, 1f, 0f, 1f, 0f, 0f,
                        1f, 0f, 1f, 1f, 1f, 0f, 1f, 1f, 0f
                });

        clockFrontPlane = new Drawable(
                "vs_pct.glsl", "fs_ct.glsl", "clock.png",
                new float[]{
                        // (x, y, z),     (r, g, b, a),     (s, t),
                        -1f, -1f, 0f, 0f, 0f, 0f, 1f, 0f, 1f,
                        1f, -1f, 0f, 1f, 0f, 0f, 1f, 1f, 1f,
                        -1f, 1f, 0f, 0f, 0f, 1f, 1f, 0f, 0f,
                        1f, 1f, 0f, 1f, 0f, 1f, 1f, 1f, 0f
                });
        clockBackPlane = new Drawable(
                "vs_pct.glsl", "fs_ct.glsl", "clock_back.png",
                new float[]{
                        // (x, y, z),     (r, g, b, a),     (s, t),
                        -1f, -1f, 0f, 0f, 0f, 0f, 1f, 1f, 1f,
                        -1f, 1f, 0f, 0f, 0f, 1f, 1f, 1f, 0f,
                        1f, -1f, 0f, 1f, 0f, 0f, 1f, 0f, 1f,
                        1f, 1f, 0f, 1f, 0f, 1f, 1f, 0f, 0f
                });
    }

    private void createStage() {
        stage = new Stage();
        // FIXME -- these sliders & font are too small on my tablet
        skin = new Skin(Gdx.files.internal("gdx-skins-orange/uiskin.json"));

        final Slider latSlider = new Slider(-90f,90f,1f,true, skin);
        latSlider.setPosition(50f, 50f);
        latSlider.setValue(latitude);
        stage.addActor(latSlider);
        final Slider lonSlider = new Slider(-180f,180f,1f,false, skin);
        lonSlider.setPosition(100f, 50f);
        latSlider.setValue(longitude);
        stage.addActor(lonSlider);
        final Slider daySlider = new Slider(0f,365f,1f,false, skin);
        daySlider.setPosition(lonSlider.getX() + lonSlider.getWidth() + 50f, 50f);
        daySlider.setValue(day);
        stage.addActor(daySlider);


        latLabel = new Label("Latitude = "+(latitude), skin);
        latLabel.setPosition(latSlider.getX(), latSlider.getY() + latSlider.getHeight() + 20f);
        stage.addActor(latLabel);

        lonLabel = new Label("Longitude = "+(longitude), skin);
        lonLabel.setPosition(lonSlider.getX(), lonSlider.getY() + lonSlider.getHeight() + 20f);
        stage.addActor(lonLabel);

        dayLabel = new Label("Day = "+(day), skin);
        dayLabel.setPosition(daySlider.getX(), daySlider.getY() + daySlider.getHeight() + 20f);
        stage.addActor(dayLabel);


        latSlider.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("lat = " + latSlider.getValue());
                latitude = latSlider.getValue();
                latLabel.setText("Latitude = "+(latitude));
                obs.setObserverLatitude(latitude);
                setClockMatrix();
            }
        });
        lonSlider.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("lon = " + lonSlider.getValue());
                longitude = lonSlider.getValue();
                lonLabel.setText("Longitude = "+(longitude));
                obs.setObserverLongitude(longitude);
                setClockMatrix();
            }
        });
        daySlider.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("day = " + daySlider.getValue());
                day = daySlider.getValue();
                dayLabel.setText("Day = "+(day));
                Date cur_date = obs.getObserverDate();
                Calendar time = Calendar.getInstance();
                time.add(Calendar.DAY_OF_YEAR,(int)day);
                obs.setTime(time.getTime());
                setClockMatrix();
            }
        });

    }

    private void setClockMatrix() {
        // find out where the sun is at various times in the day.
        //Vector3 nowVec = obs.getSunUnitXYZ();
        Date then = obs.getObserverDate();
        then.setHours(12); // noon = (0,1,0)
        then.setMinutes(0);
        then.setSeconds(0);
        obs.setTime(then);
        Vector3 noonVec = obs.getSunUnitXYZ();
        then.setHours(18); // eve = (1,0,0)
        obs.setTime(then);
        Vector3 eveVec = obs.getSunUnitXYZ();
        then.setHours(6); // morn = (-1,0,0)
        obs.setTime(then);
        Vector3 mornVec = obs.getSunUnitXYZ();
        Vector3 upVec = noonVec.cpy();
        upVec.crs(mornVec); // up = (0, 0, 1)

        // startVecs * worldProjMatrix = projVecs
        // worldProjMatrix = projVecs * (startVecs ^ -1)

        Matrix4 startVectorMatrix = new Matrix4();
        startVectorMatrix.val[Matrix4.M00] = 0.0f; // noon = (0,1,0)
        startVectorMatrix.val[Matrix4.M01] = 1.0f;
        startVectorMatrix.val[Matrix4.M02] = 0.0f;
        startVectorMatrix.val[Matrix4.M03] = 1.0f;
        startVectorMatrix.val[Matrix4.M10] = -1.0f; // morn = (-1,0,0)
        startVectorMatrix.val[Matrix4.M11] = 0.0f;
        startVectorMatrix.val[Matrix4.M12] = 0.0f;
        startVectorMatrix.val[Matrix4.M13] = 1.0f;
        startVectorMatrix.val[Matrix4.M20] = 1.0f; // eve = (1,0,0)
        startVectorMatrix.val[Matrix4.M21] = 0.0f;
        startVectorMatrix.val[Matrix4.M22] = 0.0f;
        startVectorMatrix.val[Matrix4.M23] = 1.0f;
        startVectorMatrix.val[Matrix4.M30] = 0.0f; // up = (0, 0, 1)
        startVectorMatrix.val[Matrix4.M31] = 0.0f;
        startVectorMatrix.val[Matrix4.M32] = 1.0f;
        startVectorMatrix.val[Matrix4.M33] = 1.0f;
        Matrix4 invStartVectorMatrix = startVectorMatrix.cpy();
        invStartVectorMatrix.inv();

        Matrix4 projectedVectorMatrix = new Matrix4();
        projectedVectorMatrix.val[Matrix4.M00] = noonVec.x;
        projectedVectorMatrix.val[Matrix4.M01] = noonVec.y;
        projectedVectorMatrix.val[Matrix4.M02] = noonVec.z;
        projectedVectorMatrix.val[Matrix4.M03] = 1.0f;
        projectedVectorMatrix.val[Matrix4.M10] = mornVec.x;
        projectedVectorMatrix.val[Matrix4.M11] = mornVec.y;
        projectedVectorMatrix.val[Matrix4.M12] = mornVec.z;
        projectedVectorMatrix.val[Matrix4.M13] = 1.0f;
        projectedVectorMatrix.val[Matrix4.M20] = eveVec.x;
        projectedVectorMatrix.val[Matrix4.M21] = eveVec.y;
        projectedVectorMatrix.val[Matrix4.M22] = eveVec.z;
        projectedVectorMatrix.val[Matrix4.M23] = 1.0f;
        projectedVectorMatrix.val[Matrix4.M30] = upVec.x;
        projectedVectorMatrix.val[Matrix4.M31] = upVec.y;
        projectedVectorMatrix.val[Matrix4.M32] = upVec.z;
        projectedVectorMatrix.val[Matrix4.M33] = 1.0f;

        // This gave me the clue to solve this:
        // https://math.stackexchange.com/questions/312696/how-to-find-a-transformation-matrix-having-several-original-points-and-their-res
        // But -- why do we need to transpose the Matrix?  Found this by accident.
        clockProjMatrix = invStartVectorMatrix.cpy();
        clockProjMatrix.mul(projectedVectorMatrix);
        clockProjMatrix.tra(); // ???

            /*
            // now test it out...compare with noonVec, mornVec, eveVec, upVec.
            Vector3 t0 = (new Vector3(0f,0f,0f)).mul(clockProjMatrix);  // origin
            Vector3 t1 = (new Vector3(0f,1f,0f)).mul(clockProjMatrix);  // noon
            float d01 = t0.dst(t1);
            float cosTheta1 = t0.dot(t1);
            Vector3 t2 = (new Vector3(-1f,0f,0f)).mul(clockProjMatrix); // morn
            float d02 = t0.dst(t2);
            Vector3 t3 = (new Vector3(1f,0f,0f)).mul(clockProjMatrix);  // eve
            float d03 = t0.dst(t3);
            Vector3 t4 = (new Vector3(0f,0f,1f)).mul(clockProjMatrix);  // up
            float d04 = t0.dst(t4);
            */
    }

    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        drawGnomon();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        handleKeyboard();
        updateWorld();
    }

    private void drawGnomon() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        compassFrontPlane.render(cam.combined);
        clockFrontPlane.render(cam.combined);
        compassBackPlane.render(cam.combined);
        clockBackPlane.render(cam.combined);
        // draw when the depth test fails and ...
        Gdx.gl.glDepthFunc(GL20.GL_GREATER);
        // use the compassPlane alpha=1 in the FB to enable only that region
        // this will require clearcolor=0,0,0
        Gdx.gl.glBlendFunc(GL20.GL_ONE_MINUS_DST_ALPHA, GL20.GL_DST_ALPHA);
        clockFrontPlane.render(cam.combined);
        clockBackPlane.render(cam.combined);
        compassBackPlane.render(cam.combined);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ZERO);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);

    }

    private void handleKeyboard() {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private void updateWorld() {
        clockFrontPlane.worldTrans.set(clockProjMatrix.cpy());
        clockBackPlane.worldTrans.set(clockProjMatrix.cpy());
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();

        stage.getViewport().update(width, height, true);

    }

    public void dispose() {
        compassFrontPlane.dispose();
        clockFrontPlane.dispose();
        compassBackPlane.dispose();
        clockBackPlane.dispose();

        stage.dispose();
        skin.dispose();
    }

}
