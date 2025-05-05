package com.example.cropchecker

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.cropchecker.databinding.ActivitySplashScreenBinding

class CustomSplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.videoview.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false
            binding.videoview.scaleX = 1.0f
            binding.videoview.scaleY = 1.0f

        }


        val videoUri = Uri.parse("android.resource://$packageName/${R.raw.splash_screem}")
        binding.videoview.setVideoURI(videoUri)

        binding.videoview.setOnCompletionListener {
            startActivity(Intent(this, SignIn::class.java))
            finish()
        }

        binding.videoview.start()
    }
}
