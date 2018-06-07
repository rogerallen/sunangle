attribute vec3 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord;

uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;

varying vec4 v_color;
varying vec2 v_texCoord;

void main() {
    v_color     = a_color;
    v_texCoord  = a_texCoord;
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
}
