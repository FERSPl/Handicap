#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
	vec4 color = texture(InSampler, vec2(texCoord.x, 1.0 - texCoord.y));
	fragColor = vec4(color.rgb, 1.0);
}
