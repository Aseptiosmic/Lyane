#version 300 es
precision mediump float;

in vec4 vColor;
out vec4 fragColor;

void main() {
    // Soft radial circular particle with glowing core
    vec2 coord = gl_PointCoord - vec2(0.5);
    float dist = length(coord);
    if (dist > 0.5) {
        discard;
    }
    
    float intensity = exp(-dist * 6.0);
    vec3 col = vColor.rgb * (1.0 + intensity * 1.5);
    float alpha = vColor.a * (1.0 - smoothstep(0.3, 0.5, dist));
    
    fragColor = vec4(col, alpha);
}
