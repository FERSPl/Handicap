#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
	vec2 size = vec2(textureSize(InSampler, 0));
	vec2 texel = 1.0 / size;
	vec2 p = (texCoord - vec2(0.5)) * vec2(size.x / size.y, 1.0);
	float edge = smoothstep(0.12, 0.48, length(p));
	float spacing = edge * 14.0;
	vec3 sum = vec3(0.0);
	float total = 0.0;
	for (int x = -2; x <= 2; x++) {
		for (int y = -2; y <= 2; y++) {
			float w = 1.0 / (1.0 + float(x * x + y * y));
			sum += texture(InSampler, texCoord + vec2(float(x), float(y)) * texel * spacing).rgb * w;
			total += w;
		}
	}
	vec3 color = sum / total;
	fragColor = vec4(mix(color, vec3(1.0), edge * 0.65), 1.0);
}
