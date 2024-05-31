package treasurebox.demo

import android.app.Application
import android.content.Context
//import treasurebox.monitor.MonitorHelper

/**
 * @author chenjimou
 * @description TODO
 * @date 2024/4/18
 */
class MyApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
//        MonitorHelper.monitoringUIThread()
    }
}