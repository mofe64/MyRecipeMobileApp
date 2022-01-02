package com.nubari.favdish.view.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.nubari.favdish.R
import com.nubari.favdish.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashBinding: ActivitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)
        // hide title bar programmatically
        //supportActionBar?.hide()
        // We are using a custom style and theme to enforce this


        //make screen full screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // if api version is greater than or equal to 30
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
            )
        }
        //get out animation
        val splashAnimation = AnimationUtils.loadAnimation(
            this,
            R.anim.anim_splash
        )
        splashBinding.tvAppName.animation = splashAnimation

        //setup animation listener
        splashAnimation.setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }, 1000)
                }

                override fun onAnimationRepeat(animation: Animation?) {}

            }
        )

    }
}