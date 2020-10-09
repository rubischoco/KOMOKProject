package com.teamC.komok

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SwapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap)

        val imageUpload: String? = intent.getStringExtra("imageUpload")

        findViewById<ImageView>(R.id.image_detect).setImageURI(Uri.parse(imageUpload))
    }
}