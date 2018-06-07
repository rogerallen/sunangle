#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;
uniform sampler2D s_texture;

void main() {
    gl_FragColor = texture2D( s_texture, v_texCoord );
    //gl_FragColor = vec4(v_texCoord.xyxy);//v_color);
}
