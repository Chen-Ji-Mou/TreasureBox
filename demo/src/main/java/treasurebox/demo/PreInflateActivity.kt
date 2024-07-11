package treasurebox.demo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import treasurebox.demo.databinding.ActivityPreinflateBinding

class PreInflateActivity : AppCompatActivity() {
    private val binding by lazy { ActivityPreinflateBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}