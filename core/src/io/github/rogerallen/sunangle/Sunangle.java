    package io.github.rogerallen.sunangle;


    import com.badlogic.gdx.ApplicationAdapter;
    import com.badlogic.gdx.Gdx;
    import com.badlogic.gdx.Input;
    import com.badlogic.gdx.graphics.GL20;
    import com.badlogic.gdx.graphics.PerspectiveCamera;
    import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;

    public class Sunangle extends ApplicationAdapter {

        private Drawable compassPlane, clockPlane;
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

            clockPlane = new Drawable(
                    "vs_pct.glsl","fs_ct.glsl","clock.png",
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

            Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
            Gdx.gl.glEnable( Gdx.gl.GL_DEPTH_TEST );
        }

        @Override
        public void render () {
            Gdx.gl.glViewport( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
            Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            //Gdx.gl.glDepthFunc(GL20.GL_LEQUAL); TODO figure out 2-pass way to render clockPlane
            compassPlane.render(cam.combined);
            clockPlane.render(cam.combined);
            //Gdx.gl.glDepthFunc(GL20.GL_GREATER);
            //clockPlane.render(cam.combined);
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
