package treasurebox.uitls

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * @author chenjimou
 * @description 日期相关工具类
 * @date 2024/06/03
 */

object DateUtil {
    /**
     * 获取当前系统时间，按照 --:-- 的格式进行format 返回
     */
    fun getSystemTimeStr(): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        var str = format.format(Date(System.currentTimeMillis()))
        if (str.isNullOrEmpty()) {
            str = "--:--"
        }
        return str
    }

    /**
     * 获取当前日期的星期字符串
     */
    fun getWeekStr(): String {
        val weeks = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        val calendar = Calendar.getInstance()
        calendar.time = Date();
        var weekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekIndex < 0) {
            weekIndex = 0;
        }
        return weeks[weekIndex]
    }

    /**
     * 获取当前日期，按照 --/-- 格式
     */
    fun getDateStr(): String {
        val format = SimpleDateFormat("MM/dd", Locale.getDefault())
        var str = format.format(Date(System.currentTimeMillis()))
        if (str.isNullOrEmpty()) {
            str = "--/--"
        }
        return str
    }

    /**
     * 获取当前日期，按照 --年--月--日 格式
     */
    fun getFullDateStr(): String {
        val format = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        var str = format.format(Date(System.currentTimeMillis()))
        if (str.isNullOrEmpty()) {
            str = ""
        }
        return str
    }

    /**
     * 判断当前时间是白天还是黑夜
     *
     * @return true 白天 false 晚上
     */
    fun isDayOrNight(context: Context): Boolean {
        if (get24HourMode(context)) {
            //24小时制
            val currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (currHour in 6..17) {
                return true
            }
        } else {
            //12小时制
            val currHour = Calendar.getInstance().get(Calendar.HOUR)
            if (Calendar.getInstance().get(Calendar.AM_PM) == 0) {
                //上午
                if (currHour in 6..12) {
                    return true
                }
            } else {
                //下午
                if (currHour in 0..5) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 判断系统时间设置是否为24小时制
     *
     * @return true 24小时制 false 12小时制
     */
    fun get24HourMode(context: Context): Boolean {
        return android.text.format.DateFormat.is24HourFormat(context)
    }
}