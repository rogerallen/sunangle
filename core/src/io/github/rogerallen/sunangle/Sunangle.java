package io.github.rogerallen.sunangle;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Date;

public class Sunangle extends ApplicationAdapter {

    private Drawable compassFrontPlane, clockFrontPlane, compassBackPlane, clockBackPlane;
    private Matrix4 clockProjMatrix;
    private PerspectiveCamera cam;
    private long startTime;

    private Sunobserver obs;

    @Override
    public void create() {

        Gdx.app.setLogLevel(Application.LOG_INFO); // LOG_NONE, LOG_DEBUG, LOG_ERROR, LOG_INFO
        Gdx.app.log("Sunangle", "libGDX Version = " + com.badlogic.gdx.Version.VERSION);
        setClockMatrix();

        startTime = TimeUtils.millis();

        compassFrontPlane = new Drawable(
                "vs_pct.glsl", "fs_ct.glsl", "compass.png",
                new float[]{
                        // (x, y, z),     (r, g, b, a),     (s, t),
                        -10f, 0f, -10f, 0f, 0f, 0f, 1f, 1f, 1f,
                        -10f, 0f, 10f, 0f, 1f, 0f, 1f, 1f, 0f,
                        10f, 0f, -10f, 1f, 0f, 0f, 1f, 0f, 1f,
                        10f, 0f, 10f, 1f, 1f, 0f, 1f, 0f, 0f
                });

        compassBackPlane = new Drawable(
                "vs_pct.glsl", "fs_ct.glsl", "compass_back.png",
                new float[]{
                        // (x, y, z),     (r, g, b, a),     (s, t),
                        -10f, 0f, -10f, 0f, 0f, 0f, 1f, 0f, 1f,
                        10f, 0f, -10f, 1f, 0f, 0f, 1f, 1f, 1f,
                        -10f, 0f, 10f, 0f, 1f, 0f, 1f, 0f, 0f,
                        10f, 0f, 10f, 1f, 1f, 0f, 1f, 1f, 0f
                });

        clockFrontPlane = new Drawable(
                "vs_pct.glsl", "fs_ct.glsl", "clock.png",
                new float[]{
                        // (x, y, z),     (r, g, b, a),     (s, t),
                        -10f, -10f, 0f, 0f, 0f, 0f, 1f, 0f, 1f,
                        10f, -10f, 0f, 1f, 0f, 0f, 1f, 1f, 1f,
                        -10f, 10f, 0f, 0f, 0f, 1f, 1f, 0f, 0f,
                        10f, 10f, 0f, 1f, 0f, 1f, 1f, 1f, 0f
                });
        clockBackPlane = new Drawable(
                "vs_pct.glsl", "fs_ct.glsl", "clock_back.png",
                new float[]{
                        // (x, y, z),     (r, g, b, a),     (s, t),
                        -10f, -10f, 0f, 0f, 0f, 0f, 1f, 1f, 1f,
                        -10f, 10f, 0f, 0f, 0f, 1f, 1f, 1f, 0f,
                        10f, -10f, 0f, 1f, 0f, 0f, 1f, 0f, 1f,
                        10f, 10f, 0f, 1f, 0f, 1f, 1f, 0f, 0f
                });

        //set up window into our virtual space
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 20f, -20f);
        cam.lookAt(0, 0, 0);
        cam.near = 0.1f;
        cam.far = 1000f;
        cam.update();

        CameraInputController camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);

        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    }

    private void setClockMatrix() {
        // initialize the observer
        double lat = (45 + (25.0 / 60.0));
        double lon = (-122 - (41.0 / 60.0));
        Gdx.app.log("Sunangle", "lat = " + lat + " lon = " + lon);
        Date now = new Date();
        obs = new Sunobserver(lat, lon, now);
        // find out where the sun is at various times in the day.
        //Vector3 nowVec = obs.getSunUnitXYZ();
        Date then = new Date();
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
            Vector3 t0 = (new Vector3(0f,1f,0f)).mul(clockProjMatrix);
            Vector3 t2 = (new Vector3(-1f,0f,0f)).mul(clockProjMatrix);
            Vector3 t3 = (new Vector3(1f,0f,0f)).mul(clockProjMatrix);
            Vector3 t4 = (new Vector3(0f,0f,1f)).mul(clockProjMatrix);
            */

    }

    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

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

        handleKeyboard();
        updateWorld();
    }

    private void handleKeyboard() {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private void updateWorld() {
        // Just something to give it a bit of animation
            /*
            Vector3 ax = new Vector3(1f,0f, 0f);
            float curTime = (float)TimeUtils.timeSinceMillis(startTime);
            float deg = 0.30f* MathUtils.sin(curTime/1000f);
            clockFrontPlane.worldTrans.rotate(ax,-deg);
            clockBackPlane.worldTrans.rotate(ax,-deg);
            */
        clockFrontPlane.worldTrans.set(clockProjMatrix.cpy());
        clockBackPlane.worldTrans.set(clockProjMatrix.cpy());
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();
    }

    public void dispose() {
        compassFrontPlane.dispose();
        clockFrontPlane.dispose();
        compassBackPlane.dispose();
        clockBackPlane.dispose();
    }

}
