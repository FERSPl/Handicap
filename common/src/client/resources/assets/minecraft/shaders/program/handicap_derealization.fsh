#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
	vec2 size = vec2(textureSize(DiffuseSampler, 0));
	vec2 block = vec2(size.y / size.x, 1.0) / 64.0;
	vec2 cell = (floor(texCoord / block) + 0.5) * block;
	vec3 color = texture(DiffuseSampler, cell).rgb;
	fragColor = vec4(color, 1.0);
}
