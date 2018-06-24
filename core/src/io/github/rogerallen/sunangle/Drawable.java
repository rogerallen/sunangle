package io.github.rogerallen.sunangle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.math.Matrix4;

class Drawable {
    // Things about the object to render
    public Matrix4 worldTrans;
    private VertexBufferObject vertexBufferObject;
    private ShaderProgram shaderProgram;
    private int u_projViewTrans;
    private int u_worldTrans;
    private int u_texture;
    private Texture texture;

    Drawable(String vs_shader_path, String fs_shader_path, String texture_path, float[] vertices) {
        worldTrans = new Matrix4().idt();

        //create vertex attributes which define how data is accessed in VBO
        VertexAttribute vA = new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position");
        VertexAttribute vC = new VertexAttribute(VertexAttributes.Usage.Position, 4, "a_color");
        VertexAttribute vT = new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_texCoord");
        VertexAttributes vAs = new VertexAttributes(vA, vC, vT);

        vertexBufferObject = new VertexBufferObject(false,
                vertices.length / (3 + 4 + 2),
                vAs);
        vertexBufferObject.setVertices(vertices, 0, vertices.length);

        texture = new Texture(texture_path);

        String vertexShader = Gdx.files.internal(vs_shader_path).readString();
        String fragmentShader = Gdx.files.internal(fs_shader_path).readString();
        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);

        u_projViewTrans = shaderProgram.getUniformLocation("u_projViewTrans");
        u_worldTrans = shaderProgram.getUniformLocation("u_worldTrans");
        u_texture = shaderProgram.getUniformLocation("s_texture");

    }

    public void render(Matrix4 projViewMatrix) {

        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
        Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
        texture.bind();

        shaderProgram.begin();
        // FIXME -- glUniformMatrix4fv fails with error 1282 on Mac OS X
        shaderProgram.setUniformMatrix(u_projViewTrans, projViewMatrix);
        int foo = Gdx.gl.glGetError();
        shaderProgram.setUniformMatrix(u_worldTrans, worldTrans);
        shaderProgram.setUniformi(u_texture, 0);

        vertexBufferObject.bind(shaderProgram);
        Gdx.gl.glDrawArrays(Gdx.gl.GL_TRIANGLE_STRIP, 0, vertexBufferObject.getNumVertices());
        vertexBufferObject.unbind(shaderProgram);
        shaderProgram.end();

        Gdx.gl20.glDisable(GL20.GL_TEXTURE_2D);

    }

    public void dispose() {
        vertexBufferObject.dispose();
        shaderProgram.dispose();
        texture.dispose();
    }
}
