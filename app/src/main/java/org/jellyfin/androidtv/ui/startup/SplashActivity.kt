package org.jellyfin.androidtv.ui.startup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.databinding.ActivitySplashBinding
import org.jellyfin.androidtv.ui.browsing.MainActivity
import pl.droidsonroids.gif.GifDrawable
import timber.log.Timber
import java.io.IOException

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private val splashDuration = 5200L // 5.2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            // Load the GIF from assets
            val gifFromAssets = assets.open("splash.gif")
            val gifDrawable = GifDrawable(gifFromAssets)
            
            // Set up GIF to play once
            gifDrawable.loopCount = 1
            
            binding.gifView.setImageDrawable(gifDrawable)
            
            // Start the GIF
            gifDrawable.start()
            
            // Schedule the transition to MainActivity
            handler.postDelayed({
                if (!isFinishing) {
                    startMainActivity()
                }
            }, splashDuration)
            
        } catch (e: IOException) {
            Timber.e(e, "Error loading splash screen GIF")
            // If we can't load the GIF, just proceed immediately
            startMainActivity()
        }
    }

    private fun startMainActivity() {
        // First try to start MainActivity
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            // Add any necessary flags or extras
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        // Also start StartupActivity to handle authentication if needed
        val startupIntent = Intent(this, StartupActivity::class.java).apply {
            putExtra("fromSplash", true)
        }
        
        // Start both activities
        startActivities(arrayOf(mainIntent, startupIntent))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
