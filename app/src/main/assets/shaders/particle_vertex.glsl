#version 300 es
layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec4 aColor;
layout(location = 2) in float aSize;

uniform mat4 uMVPMatrix;

out vec4 vColor;

void main() {
    vColor = aColor;
    gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
    gl_PointSize = aSize;
}
