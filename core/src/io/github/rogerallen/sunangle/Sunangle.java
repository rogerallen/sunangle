    package io.github.rogerallen.sunangle;


    import com.badlogic.gdx.ApplicationAdapter;
    import com.badlogic.gdx.Gdx;
    import com.badlogic.gdx.Input;
    import com.badlogic.gdx.graphics.GL20;
    import com.badlogic.gdx.graphics.PerspectiveCamera;
    import com.badlogic.gdx.graphics.Texture;
    import com.badlogic.gdx.graphics.VertexAttribute;
    import com.badlogic.gdx.graphics.VertexAttributes;
    import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
    import com.badlogic.gdx.graphics.glutils.ShaderProgram;
    import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
    import com.badlogic.gdx.math.Matrix4;

    public class Sunangle extends ApplicationAdapter {

        // Things about the object to render
        private float vertices[];
        private VertexBufferObject vertexBufferObject;
        private String vertexShader;
        private String fragmentShader;
        private ShaderProgram shaderProgram;
        private Matrix4 idt;
        private int u_worldTrans;
        private VertexAttributes vAs;
        private Texture compassTex;

        // Things for the camera & view
        private PerspectiveCamera cam;
        private CameraInputController camController;
        //    private Camera cam;
        private int u_projViewTrans;

        @Override
        public void create() {

            //identity matrix used for world transform
            idt = new Matrix4().idt();

            //create some points with color
            vertices = new float[]{
                    // (x, y, z),       (r, g, b, a),     (s, t),
                    -10f,   0f, -10f,   0f, 0f, 0f, 1f,   1f, 1f,
                    -10f,   0f,  10f,   0f, 1f, 0f, 1f,   1f, 0f,
                     10f,   0f, -10f,   1f, 0f, 0f, 1f,   0f, 1f,
                     10f,   0f,  10f,   1f, 1f, 0f, 1f,   0f, 0f
            };

            //create vertex attributes which define how data is accessed in VBO
            VertexAttribute vA = new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position");
            VertexAttribute vC = new VertexAttribute(VertexAttributes.Usage.Position, 4, "a_color");
            VertexAttribute vT = new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_texCoord");
            vAs = new VertexAttributes( new VertexAttribute[]{vA, vC, vT} );

            //create VBO and pass in vertex attributes object
            vertexBufferObject = new VertexBufferObject( false, (vertices.length / (3+4+2) ), vAs );

            compassTex = new Texture("compass.png");

            //create and compile shaders
            vertexShader = Gdx.files.internal("vertexShader.glsl").readString();
            fragmentShader = Gdx.files.internal("fragmentShader.glsl").readString();
            shaderProgram = new ShaderProgram( vertexShader, fragmentShader );

            //get shader code variable pointers
            u_projViewTrans = shaderProgram.getUniformLocation("u_projViewTrans");
            u_worldTrans = shaderProgram.getUniformLocation("u_worldTrans");

            //let OGL know where vertex data is
            vertexBufferObject.bind( shaderProgram ); //gdx.gl.bindbuffer && gdx.gl.glBufferData
            vertexBufferObject.setVertices( vertices, 0, vertices.length );

            //set up window into our virtual space
            cam = new PerspectiveCamera( 67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
            cam.position.set( 0f, 20f, -20f );
            cam.lookAt( 0, 0, 0 );
            cam.near = 0.1f;
            cam.far = 1000f;
            cam.update();

            camController = new CameraInputController(cam);
            Gdx.input.setInputProcessor(camController);

        }

        @Override
        public void render () {
            Gdx.gl.glViewport( 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
            Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

            shaderProgram.begin(); //Gdx.gl.glUseProgram( shadProgram );

            shaderProgram.setUniformMatrix( u_projViewTrans, cam.combined );
            shaderProgram.setUniformMatrix( u_worldTrans, idt );
            shaderProgram.setUniformi("s_texture", 0);

            Gdx.gl.glEnable( Gdx.gl.GL_DEPTH_TEST );

            Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
            compassTex.bind();

            //draw the points on the screen
            Gdx.gl.glDrawArrays( Gdx.gl.GL_TRIANGLE_STRIP, 0, vertexBufferObject.getNumVertices() );

            shaderProgram.end();

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
            vertexBufferObject.dispose();
            shaderProgram.dispose();
            compassTex.dispose();
        }

    }
