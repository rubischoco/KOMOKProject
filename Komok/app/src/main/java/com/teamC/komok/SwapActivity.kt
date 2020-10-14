package com.teamC.komok

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException

class SwapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap)

        // get image upload from home activity
        val imageUpload: String? = intent.getStringExtra("imageUpload")
        findViewById<ImageView>(R.id.image_detect).setImageURI(Uri.parse(imageUpload))

        findViewById<Button>(R.id.button_swap).setOnClickListener {
            try {
                val image = InputImage.fromFilePath(this, Uri.parse(imageUpload))
                detectFaces(image)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun detectFaces(image: InputImage) {
        val options = FaceDetectorOptions.Builder()
            .setClassificationMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()

        val detector = FaceDetection.getClient(options)

        val test: TextView = findViewById(R.id.text_test)

        val result = detector.process(image)
            .addOnSuccessListener { faces ->
                var i = 0

                for (face in faces) {
                    val bounds = face.boundingBox
                    i += 1
                }

                when (i) {
                    1 -> test.text = "< $i FACE DETECTED >"
                    else -> test.text = "< $i FACES DETECTED >"
                }
            }
            .addOnFailureListener { e ->
                test.text =  "< NO FACE DETECTED >"
                // Task failed with an exception
                // ...
            }
    }
}