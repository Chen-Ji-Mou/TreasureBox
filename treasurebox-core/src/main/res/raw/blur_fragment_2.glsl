#version 300 es

precision lowp float;

const int MAX_SIZE = 20; // 最大模糊半径（10）所包含的对角纹理坐标总数

uniform float blurRadius;
uniform float blurSamplingRate;
uniform sampler2D inputImageTexture;

in vec2 texturePosition;

out mediump vec4 FragColor;

/**
 * 基于blur_fragment_1实现优化后的方案，只计算模糊半径内的对角纹理坐标，极大精简计算量，降低运行耗时
 * 参考：https://cloud.tencent.com/developer/article/1167273
 */
void main() {
    vec4 blurPositions[MAX_SIZE]; // 模糊半径内的对角纹理坐标矩阵，使用vec4类型一次存储一对对角纹理坐标，减少循环计算次数

    // 纹理宽高
    ivec2 totalSize = textureSize(inputImageTexture, 0);

    // 水平/竖直模糊半径单位长度，单位长度越大模糊效果越发散
    float widthPX = blurSamplingRate / float(totalSize.x);
    float heightPX = blurSamplingRate / float(totalSize.y);

    // 单位相对坐标，用于计算对角纹理坐标
    vec2 leftReferPosition = vec2(widthPX, heightPX);
    vec2 rightReferPosition = vec2(widthPX, -heightPX);

    // 计算出当前纹理坐标相邻单位长度的对角纹理坐标
    for (int i = 0; i < int(blurRadius) * 2; i++) {
        if (i < int(blurRadius)) {
            float curRadius = float(i + 1);
            blurPositions[i] = vec4(texturePosition.xy - curRadius * leftReferPosition,
                                    texturePosition.xy + curRadius * leftReferPosition);
        } else {
            float curRadius = float(i - int(blurRadius) + 1);
            blurPositions[i] = vec4(texturePosition.xy - curRadius * rightReferPosition,
                                    texturePosition.xy + curRadius * rightReferPosition);
        }
    }

    vec4 curTextureColor = texture(inputImageTexture, texturePosition);

    // 获取纹理坐标点对应的纹理，将纹理做模糊
    float totalWeight = 1.0 / (4.0 * blurRadius); // 权重总和
    vec3 sum = curTextureColor.rgb / (4.0 * blurRadius);
    for (int i = 0; i < int(blurRadius) * 2; i++) {
        float curRadius;
        if (i < int(blurRadius)) {
            curRadius = float(i + 1);
        } else {
            curRadius = float(i - int(blurRadius) + 1);
        }
        float curWeight = pow(1.0 / (4.0 * blurRadius), curRadius);
        sum += texture(inputImageTexture, blurPositions[i].xy).rgb * curWeight;
        sum += texture(inputImageTexture, blurPositions[i].zw).rgb * curWeight;
        totalWeight += curWeight * 2.0;
    }

    // 由于需要纹理坐标点加权平均后等于1，因此每个纹理坐标点的权重需要除以权重总和
    FragColor = vec4(sum / totalWeight, curTextureColor.a);
}