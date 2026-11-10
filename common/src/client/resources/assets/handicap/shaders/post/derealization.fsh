#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
	vec2 size = vec2(textureSize(InSampler, 0));
	vec2 block = vec2(size.y / size.x, 1.0) / 64.0;
	vec2 cell = (floor(texCoord / block) + 0.5) * block;
	vec3 color = texture(InSampler, cell).rgb;
	fragColor = vec4(color, 1.0);
}
