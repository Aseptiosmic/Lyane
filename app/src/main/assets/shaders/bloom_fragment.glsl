#version 300 es
precision mediump float;

in vec2 vTexCoord;
uniform sampler2D uTexture;
uniform float uBloomIntensity;

out vec4 fragColor;

void main() {
    vec4 base = texture(uTexture, vTexCoord);
    
    // Gaussian blur sample offsets
    vec2 texel = 1.0 / vec2(textureSize(uTexture, 0));
    vec4 bloom = vec4(0.0);
    
    bloom += texture(uTexture, vTexCoord + vec2(-1.5, -1.5) * texel) * 0.09;
    bloom += texture(uTexture, vTexCoord + vec2( 0.0, -1.5) * texel) * 0.12;
    bloom += texture(uTexture, vTexCoord + vec2( 1.5, -1.5) * texel) * 0.09;
    
    bloom += texture(uTexture, vTexCoord + vec2(-1.5,  0.0) * texel) * 0.12;
    bloom += texture(uTexture, vTexCoord + vec2( 0.0,  0.0) * texel) * 0.16;
    bloom += texture(uTexture, vTexCoord + vec2( 1.5,  0.0) * texel) * 0.12;
    
    bloom += texture(uTexture, vTexCoord + vec2(-1.5,  1.5) * texel) * 0.09;
    bloom += texture(uTexture, vTexCoord + vec2( 0.0,  1.5) * texel) * 0.12;
    bloom += texture(uTexture, vTexCoord + vec2( 1.5,  1.5) * texel) * 0.09;
    
    fragColor = base + bloom * uBloomIntensity;
}
