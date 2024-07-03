#version 300 es

precision mediump float;

const lowp int GAUSSIAN_SAMPLES = 9;

uniform sampler2D inputImageTexture;

in highp vec2 texturePosition;
in highp vec2 blurPositions[GAUSSIAN_SAMPLES];

out vec4 FragColor;

//卷积核
mat3 kernelMatrix = mat3(
    0.0947416f, 0.118318f, 0.0947416f,
    0.118318f, 0.147761f, 0.118318f,
    0.0947416f, 0.118318f, 0.0947416f
);

void main() {
    lowp vec4 originColor = texture(inputImageTexture, texturePosition);

    //卷积处理
    lowp vec3 sum = texture(inputImageTexture, blurPositions[0]).rgb * kernelMatrix[0][0];
    sum += texture(inputImageTexture, blurPositions[1]).rgb * kernelMatrix[0][1];
    sum += texture(inputImageTexture, blurPositions[2]).rgb * kernelMatrix[0][2];
    sum += texture(inputImageTexture, blurPositions[3]).rgb * kernelMatrix[1][0];
    sum += texture(inputImageTexture, blurPositions[4]).rgb * kernelMatrix[1][1];
    sum += texture(inputImageTexture, blurPositions[5]).rgb * kernelMatrix[1][2];
    sum += texture(inputImageTexture, blurPositions[6]).rgb * kernelMatrix[2][0];
    sum += texture(inputImageTexture, blurPositions[7]).rgb * kernelMatrix[2][1];
    sum += texture(inputImageTexture, blurPositions[8]).rgb * kernelMatrix[2][2];

    FragColor = vec4(sum, originColor.a);
}