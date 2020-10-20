package com.teamC.komok

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.android.synthetic.main.activity_crop.*

class CropActivity : AppCompatActivity() {
    private val imageUtils = ImageUtils()
    private val drawUtils = DrawUtils()
    private val detector: FaceDetector
    private lateinit var imageUpload: Uri
    private lateinit var savedFaces: MutableList<Face>

    companion object {
        const val IMAGE_PICK_CODE = 100
    }

    init {
        val detectOptions: FaceDetectorOptions = FaceDetectorOptions.Builder()
            .setClassificationMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setMinFaceSize(0.1f)
            .build()
        detector = FaceDetection.getClient(detectOptions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        imageUpload = Uri.parse(intent.getStringExtra("imageUpload"))

        image_preview.setImageURI(imageUpload)
        detectFaces(imageUpload)

        button_change.setOnClickListener {
            imageUtils.pickImageFromGallery(this, IMAGE_PICK_CODE)
        }

        button_setting.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
        }

        button_save.setOnClickListener {
            if (savedFaces.isNotEmpty()) {
                saveCropFaces(imageUpload, savedFaces)
                Toast.makeText(this, "Success saved cropped face!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No face data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun detectFaces(uri: Uri) {
        val image = InputImage.fromFilePath(this, uri)
        val bitmap = drawUtils.getBitmapFromUri(this, uri)

        savedFaces = mutableListOf()
        text_info.text = "[DETECTING FACE]"

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    savedFaces = faces
                    text_info.text = "[${savedFaces.size} FACE DETECTED]"
                    image_preview.setImageBitmap(drawUtils.drawRectFaces(bitmap, savedFaces))
                } else { text_info.text = "[NO FACE DETECTED]" }
            }
            .addOnFailureListener { e -> text_info.text = "[ $e ]" }
    }

    private fun saveCropFaces(uri: Uri, faces: MutableList<Face>) {
        val curBitmap = drawUtils.getBitmapFromUri(this, uri)

        for (face in faces) {
            val tempBitmap = drawUtils.cropFaces(curBitmap, face)

            imageUtils.saveImage(this, tempBitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_CODE) {
                imageUpload = Uri.parse(data?.data.toString())
                image_preview.setImageURI(imageUpload)
                detectFaces(imageUpload)
            }
        }
    }
}