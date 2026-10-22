#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
	vec4 color = texture(InSampler, texCoord);
	vec2 size = vec2(textureSize(InSampler, 0));
	vec2 p = (texCoord - vec2(0.5)) * vec2(size.x / size.y, 1.0);
	float d = length(p);
	float f = smoothstep(0.10, 0.28, d);
	fragColor = vec4(color.rgb * (1.0 - f), 1.0);
}
