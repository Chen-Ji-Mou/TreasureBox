#version 300 es

precision mediump float;

const int MAX_SIZE = 1024;

uniform float blurRadius;
uniform float screenWidth;
uniform float screenHeight;

in vec4 inputPosition;
in vec2 inputTexturePosition;

out vec2 texturePosition;
out vec2 blurPositions[MAX_SIZE]; // 坐标矩阵

void main() {
    gl_Position = inputPosition;
    texturePosition = inputTexturePosition.xy;

    // 水平/竖直单个像素
    float widthPX = 1.0 / screenWidth;
    float heightPX = 1.0 / screenHeight;

    // 模糊半径换算成水平/竖直像素
    float blurRadiusWidthPX = widthPX * blurRadius;
    float blurRadiusHeightPX = heightPX * blurRadius;

    vec2 referPosition = vec2(-blurRadiusWidthPX, blurRadiusHeightPX);
    
    // 计算出当前顶点相邻像素的顶点坐标
    float matrixSize = pow(3.0 + ((blurRadius - 1.0) * 2.0), 2.0);
    float matrixWidth = sqrt(float(matrixSize));
    int index = 0;
    for (int i = 0; i < int(matrixWidth); i++) {
        for (int j = 0; j < int(matrixWidth); j++) {
            float x = referPosition.x + (float(j) * widthPX);
            float y = referPosition.y - (float(i) * heightPX);
            blurPositions[index] = texturePosition.xy + vec2(x, y);
            index++;
        }
    }
}
