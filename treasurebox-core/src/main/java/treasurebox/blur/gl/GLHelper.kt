package treasurebox.blur.gl

import android.content.res.Resources
import android.graphics.Bitmap
import android.hardware.Camera
import android.opengl.GLES20
import android.opengl.GLU
import android.opengl.GLUtils
import android.util.Log
import androidx.annotation.RawRes
import java.io.InputStream
import java.nio.Buffer
import java.nio.IntBuffer

/**
 * @author chenjimou
 * @description openGL工具类
 * @date 2024/7/2
 */
internal object GLHelper {
    private val TAG = GLHelper::class.java.simpleName
    const val NO_TEXTURE: Int = -1

    fun loadAsset(res: Resources, path: String?): String {
        val stringBuilder = StringBuilder()
        var `is`: InputStream? = null
        try {
            `is` = res.assets.open(path!!)
            val buffer = ByteArray(1024)
            var count: Int
            while (-1 != (`is`.read(buffer).also { count = it })) {
                stringBuilder.append(String(buffer, 0, count))
            }
            val result = stringBuilder.toString().replace("\\r\\n".toRegex(), "\n")
            return result
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
        } finally {
            try {
                `is`?.close()
            } catch (e: Exception) {
                Log.e(TAG, Log.getStackTraceString(e))
            }
        }
        return ""
    }

    fun loadRaw(res: Resources, @RawRes resId: Int): String {
        val stringBuilder = StringBuilder()
        var `is`: InputStream? = null
        try {
            `is` = res.openRawResource(resId)
            val buffer = ByteArray(1024)
            var count: Int
            while (-1 != (`is`.read(buffer).also { count = it })) {
                stringBuilder.append(String(buffer, 0, count))
            }
            val result = stringBuilder.toString().replace("\\r\\n".toRegex(), "\n")
            return result
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
        } finally {
            try {
                `is`?.close()
            } catch (e: Exception) {
                Log.e(TAG, Log.getStackTraceString(e))
            }
        }
        return ""
    }

    private fun loadShader(type: Int, codeStr: String): Int {
        //1. 根据类型（顶点着色器、片元着色器）创建着色器，拿到着色器句柄
        val shader = GLES20.glCreateShader(type)
        Log.d(TAG, "glCreateShader : type = $type shaderId = $shader")

        if (shader > 0) {
            //2. 设置着色器代码 ，shader句柄和code进行绑定
            GLES20.glShaderSource(shader, codeStr)

            //3. 编译着色器，
            GLES20.glCompileShader(shader)

            //4. 查询编译状态
            val status = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
            Log.d(TAG, "glCompileShader : status[0] = ${status[0]}")
            //如果失败，释放资源
            if (status[0] == 0) {
                Log.e(TAG, "glCompileShader : error = ${GLES20.glGetShaderInfoLog(shader)}")
                GLES20.glDeleteShader(shader)
                return 0
            }
        }
        return shader
    }


    fun loadProgram(verCode: String, fragmentCode: String): Int {
        //1. 创建Shader程序，获取到program句柄
        val programId = GLES20.glCreateProgram()
        if (programId == 0) {
            val errorCode = GLES20.glGetError()
            Log.e(TAG, "glCreateProgram : errorCode = $errorCode")
            return 0
        }

        //2. 根据着色器语言类型和代码，attach着色器
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, verCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode)
        GLES20.glAttachShader(programId, vertexShader)
        GLES20.glAttachShader(programId, fragmentShader)

        //3. 链接
        GLES20.glLinkProgram(programId)

        //4. 使用
        GLES20.glUseProgram(programId)

        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "glAttachShader : error = ${GLU.gluErrorString(error)}")
        }

        val status = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] <= 0) {
            Log.e(TAG, "glLinkProgram : error -> " + GLES20.glGetProgramInfoLog(programId))
            return 0
        }
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)

        return programId
    }

    @JvmOverloads
    fun loadTexture(img: Bitmap, usedTexId: Int, recycle: Boolean = true): Int {
        val textures = IntArray(1)
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0)
            if (textures[0] == 0) {
                Log.e(TAG, "loadTexture : Could not generate a new OpenGL texture object")
                return 0
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0)
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId)
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, img)
            textures[0] = usedTexId
        }
        if (recycle) {
            img.recycle()
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textures[0]
    }

    fun loadTexture(data: Buffer?, width: Int, height: Int, usedTexId: Int): Int {
        val textures = IntArray(1)
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0)
            if (textures[0] == 0) {
                Log.e(TAG, "loadTexture : Could not generate a new OpenGL texture object")
                return 0
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data
            )
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId)
            GLES20.glTexSubImage2D(
                GLES20.GL_TEXTURE_2D, 0, 0, 0, width,
                height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data
            )
            textures[0] = usedTexId
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textures[0]
    }

    fun loadTextureAsBitmap(data: IntBuffer, size: Camera.Size, usedTexId: Int): Int {
        val bitmap =
            Bitmap.createBitmap(data.array(), size.width, size.height, Bitmap.Config.ARGB_8888)
        return loadTexture(bitmap, usedTexId)
    }
}