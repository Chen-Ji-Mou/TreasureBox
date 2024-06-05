package treasurebox.uitls

import android.content.Context
import android.net.ConnectivityManager

/**
 * @author chenjimou
 * @description 网络相关工具类
 * @date 2024/06/03
 */
object NetworkUtil {
    /**
     * 通过ping公网ip（www.baidu.com）检测当前网络是否可以上网
     */
    fun syncNetworkAvailable(): Boolean {
        var pingSuccess = false
        val runtime = Runtime.getRuntime()
        try {
            // 会阻塞线程 ping baidu 3次
            val exec = runtime.exec("ping -c 3 -w 10 www.baidu.com")
            val result = exec.waitFor()
            // wifi不可用或未连接，返回2；WiFi需要认证，返回1；WiFi可用，返回0；
            pingSuccess = result == 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return pingSuccess
    }

    /**
     * 检测当前Wifi是否连接网络
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.isConnected ?: false
    }
}