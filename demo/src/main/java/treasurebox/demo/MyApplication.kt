package treasurebox.demo

import android.app.Application
import android.content.Context
import treasurebox.monitor.MonitorHelper

/**
 * @author chenjimou
 * @date 2024/4/18
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MonitorHelper.startMonitorUIThread()
    }
}