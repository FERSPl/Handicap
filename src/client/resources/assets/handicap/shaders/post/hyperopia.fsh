#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
	vec2 texel = 1.0 / vec2(textureSize(InSampler, 0));
	float amount = (1.0 - smoothstep(0.0, 0.5, texCoord.y)) * 3.0;
	vec3 sum = vec3(0.0);
	float total = 0.0;
	for (int x = -2; x <= 2; x++) {
		for (int y = -2; y <= 2; y++) {
			float w = 1.0 / (1.0 + float(x * x + y * y));
			sum += texture(InSampler, texCoord + vec2(float(x), float(y)) * texel * amount).rgb * w;
			total += w;
		}
	}
	fragColor = vec4(sum / total, 1.0);
}
