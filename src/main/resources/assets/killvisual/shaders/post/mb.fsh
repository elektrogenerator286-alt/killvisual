#version 330

uniform sampler2D MainSampler;
uniform sampler2D HistorySampler;

layout(std140) uniform MbConfig {
    float Strength;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 cur = texture(MainSampler, texCoord);
    vec4 old = texture(HistorySampler, texCoord);
    vec3 col = old.a > 0.5 ? mix(cur.rgb, old.rgb, Strength) : cur.rgb;
    fragColor = vec4(col, 1.0);
}
