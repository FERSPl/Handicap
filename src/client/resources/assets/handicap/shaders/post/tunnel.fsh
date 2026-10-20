#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
	vec4 color = texture(InSampler, texCoord);
	float d = distance(texCoord, vec2(0.5));
	float f = smoothstep(0.2, 0.55, d);
	fragColor = vec4(color.rgb * (1.0 - f), 1.0);
}
