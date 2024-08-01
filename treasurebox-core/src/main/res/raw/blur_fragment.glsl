#version 300 es

precision mediump float;

const int MAX_SIZE = 1024;
const float PI = 3.14159265358979323846;
const float E = 2.71828182845904523536;
const float VARIANCE = 2.0;

uniform sampler2D inputImageTexture;

in highp vec2 texturePosition;
in highp vec2 blurPositions[MAX_SIZE];

out vec4 FragColor;

/**
 * 计算坐标模糊权重 https://www.ruanyifeng.com/blog/2012/11/gaussian_blur.html
 */
float calculateWeight(vec2 position) {
    return (1.0 / (2.0 * PI * pow(VARIANCE, 2.0))) * pow(E, -((pow(position.x, 2.0) + pow(position.y, 2.0)) / (2.0 * pow(VARIANCE, 2.0))));
}

void main() {
    float matrixSize = pow(3.0 + ((blurRadius - 1.0) * 2.0), 2.0);
    float positionWeights[matrixSize]; // 坐标模糊权重矩阵

    // 计算每个坐标点的模糊权重，并累加获取权重总和
    float totalWeight = 0.0;
    for (int i = 0; i < matrixSize; i++) {
        float curPositionWeight = calculateWeight(blurPositions[i]);
        positionWeights[i] = curPositionWeight;
        totalWeight += curPositionWeight;
    }

    // 由于需要坐标点加权平均后等于1，因此每个坐标点的权重需要除以权重总和
    for (int i = 0; i < matrixSize; i++) {
        positionWeights[i] = positionWeights[i] / totalWeight;
    }

    lowp vec4 originTexture = texture(inputImageTexture, texturePosition);

    // 获取坐标点对应的纹理，将纹理做模糊
    lowp vec3 sum = vec3(0);
    for (int i = 0; i < matrixSize; i++) {
        sum += texture(inputImageTexture, blurPositions[i]).rgb * positionWeights[i];
    }

    FragColor = vec4(sum, originTexture.a);
}