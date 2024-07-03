#version 300 es

const int GAUSSIAN_SAMPLES = 9;

uniform float blurRadius;
uniform float screenWidth;
uniform float screenHeight;

in vec4 inputPosition;
in vec2 inputTexturePosition;

out vec2 texturePosition;
out vec2 blurPositions[GAUSSIAN_SAMPLES];

void main() {
    gl_Position = inputPosition;
    texturePosition = inputTexturePosition.xy;

    //横向和纵向的步长
    vec2 widthStep = vec2(blurRadius / screenWidth, 0.0);
    vec2 heightStep = vec2(0.0, blurRadius / screenHeight);

    //计算出当前片段相邻像素的纹理坐标
    blurPositions[0] = texturePosition.xy - heightStep - widthStep;// 左上
    blurPositions[1] = texturePosition.xy - heightStep;// 上
    blurPositions[2] = texturePosition.xy - heightStep + widthStep;// 右上
    blurPositions[3] = texturePosition.xy - widthStep;// 左中
    blurPositions[4] = texturePosition.xy;// 中
    blurPositions[5] = texturePosition.xy + widthStep;// 右中
    blurPositions[6] = texturePosition.xy + heightStep - widthStep;// 左下
    blurPositions[7] = texturePosition.xy + heightStep;// 下
    blurPositions[8] = texturePosition.xy + heightStep + widthStep;// 右下
}
