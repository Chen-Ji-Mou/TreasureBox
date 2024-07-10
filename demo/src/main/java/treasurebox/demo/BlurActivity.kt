package treasurebox.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import treasurebox.demo.databinding.ActivityBlurBinding

class BlurActivity : AppCompatActivity() {
    private val binding by lazy { ActivityBlurBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.root.setOnTouchListener { _, event ->
            Log.d(TAG, "x = ${event.x}, y = ${event.y}")
            binding.blurOverlay.x = event.x - (binding.blurOverlay.width / 2)
            binding.blurOverlay.y = event.y - (binding.blurOverlay.height / 2)
            binding.blurOverlay.post {
                binding.blurOverlay.refreshBlurOverlay()
            }
            return@setOnTouchListener true
        }
    }

    companion object {
        private val TAG = BlurActivity::class.java.simpleName
    }
}