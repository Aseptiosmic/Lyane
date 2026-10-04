#version 300 es
layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aColor;
layout(location = 3) in float aGlow;

uniform mat4 uMVPMatrix;
uniform float uTime;
uniform int uStyle;

out vec2 vTexCoord;
out vec4 vColor;
out float vGlow;
out vec3 vWorldPos;

void main() {
    vTexCoord = aTexCoord;
    vColor = aColor;
    vGlow = aGlow;
    
    vec3 pos = aPosition;
    // Wave style subtle vertex displacement
    if (uStyle == 7) { // WAVE
        pos.x += sin(pos.y * 8.0 + uTime * 4.0) * 0.015;
    }
    
    vWorldPos = pos;
    gl_Position = uMVPMatrix * vec4(pos, 1.0);
}
