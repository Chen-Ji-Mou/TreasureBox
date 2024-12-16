#version 300 es

precision lowp float;

const int MAX_SIZE = 441; // 最大模糊半径（10）所包含的纹理坐标总数
const float PI = 3.14159265358979323846;
const float E = 2.71828182845904523536;

uniform float blurRadius;
uniform float blurSamplingRate;
uniform sampler2D inputImageTexture;

in vec2 texturePosition;

out vec4 FragColor;

/**
 * 计算坐标模糊权重，公式：https://www.ruanyifeng.com/blog/2012/11/gaussian_blur.html
 * 公式中σ即为模糊半径
 */
float calculateWeight(vec2 position) {
    return (1.0 / (2.0 * PI * pow(blurRadius, 2.0))) * pow(E, -((pow(position.x, 2.0) + pow(position.y, 2.0)) / (2.0 * pow(blurRadius, 2.0))));
}

void main() {
    vec2 blurPositions[MAX_SIZE]; // 模糊半径内的纹理坐标矩阵
    float positionWeights[MAX_SIZE]; // 纹理坐标模糊权重矩阵

    // 纹理宽高
    ivec2 totalSize = textureSize(inputImageTexture, 0);

    // 水平/竖直模糊半径单位长度，单位长度越大模糊效果越发散
    float widthPX = blurSamplingRate / float(totalSize.x);
    float heightPX = blurSamplingRate / float(totalSize.y);

    // 模糊半径换算单位
    float blurRadiusWidthPX = widthPX * blurRadius;
    float blurRadiusHeightPX = heightPX * blurRadius;

    float matrixSize = pow(3.0 + ((blurRadius - 1.0) * 2.0), 2.0); // 当前模糊半径对应的矩阵大小
    float matrixWidth = sqrt(float(matrixSize)); // 矩阵宽度
    float totalWeight = 0.0; // 权重总和

    // 计算出当前纹理坐标相邻单位长度的纹理坐标
    vec2 referPosition = vec2(-blurRadiusWidthPX, blurRadiusHeightPX);
    int index = 0;
    for (int i = 0; i < int(matrixWidth); i++) {
        for (int j = 0; j < int(matrixWidth); j++) {
            float x = referPosition.x + (float(j) * widthPX);
            float y = referPosition.y - (float(i) * heightPX);
            blurPositions[index] = texturePosition.xy + vec2(x, y);
            // 计算每个纹理坐标点的模糊权重，并累加获取权重总和
            float curPositionWeight = calculateWeight(blurPositions[index]);
            positionWeights[index] = curPositionWeight;
            totalWeight += curPositionWeight;
            index++;
        }
    }

    vec4 curTextureColor = texture(inputImageTexture, texturePosition);

    // 获取纹理坐标点对应的纹理，将纹理做模糊
    // 由于需要纹理坐标点加权平均后等于1，因此每个纹理坐标点的权重需要除以权重总和
    vec3 sum = vec3(0);
    for (int i = 0; i < int(matrixSize); i++) {
        sum += texture(inputImageTexture, blurPositions[i]).rgb * (positionWeights[i] / totalWeight);
    }

    FragColor = vec4(sum, curTextureColor.a);
}