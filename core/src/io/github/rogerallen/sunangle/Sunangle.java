    package io.github.rogerallen.sunangle;


    import com.badlogic.gdx.ApplicationAdapter;
    import com.badlogic.gdx.Gdx;
    import com.badlogic.gdx.Input;
    import com.badlogic.gdx.graphics.GL20;
    import com.badlogic.gdx.graphics.PerspectiveCamera;
    import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;

    public class Sunangle extends ApplicationAdapter {

        private Drawable compassPlane, clockPlane, compassBackPlane, clockBackPlane;
        private PerspectiveCamera cam;

        @Override
        public void create() {

            compassPlane = new Drawable(
                    "vs_pct.glsl","fs_ct.glsl","compass.png",
                    new float[] {
                            // (x, y, z),     (r, g, b, a),     (s, t),
                            -10f, 0f, -10f,   0f, 0f, 0f, 1f,   1f, 1f,
                            -10f, 0f,  10f,   0f, 1f, 0f, 1f,   1f, 0f,
                             10f, 0f, -10f,   1f, 0f, 0f, 1f,   0f, 1f,
                             10f, 0f,  10f,   1f, 1f, 0f, 1f,   0f, 0f
                    });

            compassBackPlane = new Drawable(
                    "vs_pct.glsl","fs_ct.glsl","compass_back.png",
                    new float[] {
                            // (x, y, z),     (r, g, b, a),     (s, t),
                            -10f, 0f, -10f,   0f, 0f, 0f, 1f,   0f, 1f,
                             10f, 0f, -10f,   1f, 0f, 0f, 1f,   1f, 1f,
                            -10f, 0f,  10f,   0f, 1f, 0f, 1f,   0f, 0f,
                             10f, 0f,  10f,   1f, 1f, 0f, 1f,   1f, 0f
                    });

            clockPlane = new Drawable(
                    "vs_pct.glsl","fs_ct.glsl","clock.png",
                    new float[] {
                            // (x, y, z),     (r, g, b, a),     (s, t),
                            -10f, -10f,  0f,   0f, 0f, 0f, 1f,   0f, 1f,
                             10f, -10f,  0f,   1f, 0f, 0f, 1f,   1f, 1f,
                            -10f,  10f,  0f,   0f, 0f, 1f, 1f,   0f, 0f,
                             10f,  10f,  0f,   1f, 0f, 1f, 1f,   1f, 0f
                    });
            clockBackPlane = new Drawable(
                    "vs_pct.glsl","fs_ct.glsl","clock_back.png",
                    new float[] {
                            // (x, y, z),     (r, g, b, a),     (s, t),
                            -10f, -10f,  0f,   0f, 0f, 0f, 1f,   1f, 1f,
                            -10f,  10f,  0f,   0f, 0f, 1f, 1f,   1f, 0f,
                             10f, -10f,  0f,   1f, 0f, 0f, 1f,   0f, 1f,
                             10f,  10f,  0f,   1f, 0f, 1f, 1f,   0f, 0f
                    });

            //set up window into our virtual space
            cam = new PerspectiveCamera( 67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
            cam.position.set( 0f, 20f, -20f );
            cam.lookAt( 0, 0, 0 );
            cam.near = 0.1f;
            cam.far = 1000f;
            cam.update();

            CameraInputController camController = new CameraInputController(cam);
            Gdx.input.setInputProcessor(camController);

            Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
            Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        }

        @Override
        public void render () {
            Gdx.gl.glViewport( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
            Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
            Gdx.gl.glCullFace(GL20.GL_BACK);
            compassPlane.render(cam.combined);
            clockPlane.render(cam.combined);
            compassBackPlane.render(cam.combined);
            clockBackPlane.render(cam.combined);
            // draw when the depth test fails and ...
            Gdx.gl.glDepthFunc(GL20.GL_GREATER);
            // use the compassPlane alpha=1 in the FB to enable only that region
            // this will require clearcolor=0,0,0
            Gdx.gl.glBlendFunc(GL20.GL_ONE_MINUS_DST_ALPHA, GL20.GL_DST_ALPHA);
            clockPlane.render(cam.combined);
            clockBackPlane.render(cam.combined);
            compassBackPlane.render(cam.combined);
            Gdx.gl.glDisable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ZERO);

            handleKeyboard();
        }

        private void handleKeyboard() {
            if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
            }
        }

        @Override
        public void resize(int width, int height) {
            cam.viewportWidth = width;
            cam.viewportHeight = height;
            cam.update();
        }

        public void dispose() {
            compassPlane.dispose();
            clockPlane.dispose();
        }

    }
