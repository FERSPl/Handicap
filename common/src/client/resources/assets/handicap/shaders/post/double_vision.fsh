#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
	vec2 offset = vec2(0.018, 0.008);
	vec3 base = texture(InSampler, texCoord).rgb;
	vec3 ghost = texture(InSampler, clamp(texCoord + offset, 0.0, 1.0)).rgb;
	fragColor = vec4(mix(base, ghost, 0.45), 1.0);
}
