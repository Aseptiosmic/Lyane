#version 300 es
precision mediump float;

in vec2 vTexCoord;
in vec4 vColor;
in float vGlow;
in vec3 vWorldPos;

uniform float uTime;
uniform int uStyle;
uniform float uBloomIntensity;

out vec4 fragColor;

void main() {
    vec2 uv = vTexCoord;
    vec4 baseColor = vColor;
    
    // Default border roundness & glow
    float edgeDistX = min(uv.x, 1.0 - uv.x);
    float edgeDistY = min(uv.y, 1.0 - uv.y);
    float borderGlow = smoothstep(0.0, 0.12, edgeDistX) * smoothstep(0.0, 0.05, edgeDistY);
    
    vec3 col = baseColor.rgb;
    float alpha = baseColor.a;
    
    // Style presets:
    // 0: Classic, 1: Neon, 2: Glass, 3: Crystal, 4: Gem, 5: Particle, 6: Energy, 7: Wave, 8: Minimal, 9: Cosmic
    if (uStyle == 1) { // NEON
        // Glowing core with bright center streak
        float centerDist = abs(uv.x - 0.5) * 2.0;
        float core = 1.0 - smoothstep(0.0, 0.4, centerDist);
        col = mix(col, vec3(1.0, 1.0, 1.0), core * 0.7);
        col += baseColor.rgb * (1.0 - centerDist) * vGlow * uBloomIntensity;
    } else if (uStyle == 2) { // GLASS
        // Refraction edge highlight & gradient transparency
        float fresnel = pow(1.0 - edgeDistX * 2.0, 2.0);
        col = mix(col * 0.8, vec3(0.9, 0.95, 1.0), fresnel * 0.8);
        alpha *= (0.4 + 0.6 * fresnel);
    } else if (uStyle == 3) { // CRYSTAL
        // Faceted diamond pattern
        float facet = sin(uv.x * 20.0 + uv.y * 30.0);
        col += vec3(facet * 0.25) * baseColor.rgb;
    } else if (uStyle == 4) { // GEM
        // Specular highlight sheen
        float sheen = smoothstep(0.45, 0.55, sin(uv.x * 6.0 - uv.y * 12.0 + uTime * 2.0));
        col += vec3(sheen * 0.4);
    } else if (uStyle == 6) { // ENERGY
        // Pulsing electric plasma noise
        float pulse = sin(uv.y * 40.0 - uTime * 15.0) * cos(uv.x * 30.0 + uTime * 10.0);
        col += baseColor.rgb * pulse * 0.5;
        col = clamp(col, 0.0, 2.0);
    } else if (uStyle == 8) { // MINIMAL
        // Crisp flat geometry with delicate 1px border
        if (edgeDistX < 0.04 || edgeDistY < 0.02) {
            col = vec3(1.0);
        }
    } else if (uStyle == 9) { // COSMIC
        // Star speckles inside note
        float stars = step(0.96, fract(sin(dot(uv * 50.0, vec2(12.9898, 78.233))) * 43758.5453));
        col += vec3(stars * 0.8);
    }
    
    // Bottom edge contact illumination
    float hitEdge = 1.0 - smoothstep(0.0, 0.08, uv.y);
    col += baseColor.rgb * hitEdge * 0.8;
    
    fragColor = vec4(col, alpha);
}
