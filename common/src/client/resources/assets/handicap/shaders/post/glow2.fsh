#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
	vec3 color = texture(InSampler, texCoord).rgb * (1.0 + 2.0 * 0.65);
	float gray = dot(color, vec3(0.299, 0.587, 0.114));
	color = mix(color, vec3(gray), 0.65 * 0.6);
	float glare = clamp(1.0 - distance(texCoord, vec2(0.5)) * 1.4, 0.0, 1.0);
	color = mix(color, vec3(1.0), clamp(0.65 * (0.55 + glare), 0.0, 1.0));
	fragColor = vec4(color, 1.0);
}
