package treasurebox.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Thread.sleep(5000L)
    }

    override fun onStart() {
        super.onStart()
        Thread.sleep(3000L)
    }
}