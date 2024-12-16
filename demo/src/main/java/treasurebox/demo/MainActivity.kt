package treasurebox.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import treasurebox.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.sleep(1000)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.preInflateBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, PreInflateActivity::class.java))
        }
        binding.blurBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, BlurActivity::class.java))
        }
    }
}