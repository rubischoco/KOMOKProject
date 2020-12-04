package com.teamC.komok

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Glide.with(this)
            .asGif()
            .load(R.drawable.komok_splashscreen)
            .listener(object : RequestListener<GifDrawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<GifDrawable?>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
            override fun onResourceReady(
                resource: GifDrawable?,
                model: Any?,
                target: Target<GifDrawable?>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                resource?.setLoopCount(1)
                resource?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable) {
                        //do whatever after specified number of loops complete
                        splashFinish()
                    }
                })
                return false
            }
        }).into(image_splash)
    }

    private fun splashFinish() {
        handler = Handler()
        handler.postDelayed({

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }, 500)
    }
}