#version 300 es

precision lowp float;

in vec4 inputPosition;
in vec2 inputTexturePosition;

out vec2 texturePosition;

void main() {
    gl_Position = inputPosition;
    texturePosition = inputTexturePosition.xy;
}
