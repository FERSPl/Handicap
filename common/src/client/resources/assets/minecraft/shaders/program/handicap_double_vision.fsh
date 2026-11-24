#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
	vec2 offset = vec2(0.018, 0.008);
	vec3 base = texture(DiffuseSampler, texCoord).rgb;
	vec3 ghost = texture(DiffuseSampler, clamp(texCoord + offset, 0.0, 1.0)).rgb;
	fragColor = vec4(mix(base, ghost, 0.45), 1.0);
}
