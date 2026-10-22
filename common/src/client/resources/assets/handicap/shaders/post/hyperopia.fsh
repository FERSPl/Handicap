#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
	vec2 texel = 1.0 / vec2(textureSize(InSampler, 0));
	float strength = 1.0 - smoothstep(0.30, 0.36, texCoord.y);
	float spacing = strength * 5.0;
	vec3 sum = vec3(0.0);
	float total = 0.0;
	for (int x = -3; x <= 3; x++) {
		for (int y = -3; y <= 3; y++) {
			float w = 1.0 / (1.0 + float(x * x + y * y));
			sum += texture(InSampler, texCoord + vec2(float(x), float(y)) * texel * spacing).rgb * w;
			total += w;
		}
	}
	fragColor = vec4(sum / total, 1.0);
}
