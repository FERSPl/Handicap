#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
	vec3 c = texture(InSampler, texCoord).rgb;
	float g = dot(c, vec3(0.299, 0.587, 0.114));
	c = mix(c, vec3(g), 0.6);
	c = floor(c * 5.0 + 0.5) / 5.0;
	fragColor = vec4(c, 1.0);
}
