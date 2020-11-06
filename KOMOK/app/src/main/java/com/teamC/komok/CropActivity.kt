package com.teamC.komok

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private var shape: Boolean = true
    private var shapeSize: Int = 0
    private lateinit var bitmap: Bitmap
    private lateinit var imageUpload: Uri
    private lateinit var savedFaces: MutableList<Face>

    companion object {
        const val CAMERA_CODE = 100
        const val GALLERY_CODE = 101
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

        val box = ContextCompat.getDrawable(this, R.drawable.button_shapebox)
        val circle = ContextCompat.getDrawable(this, R.drawable.button_shapecircle)

        imageUpload = Uri.parse(intent.getStringExtra("imageUpload"))

        image_preview.setImageURI(imageUpload)
        detectFaces(imageUpload)

        button_back.setOnClickListener { finish() }

        button_help.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
        }

        button_change.setOnClickListener {
            imageUtils.getImageDialog(this, CAMERA_CODE, GALLERY_CODE)
        }

        button_shape.setOnClickListener {
            if (button_shape.alpha == 1f) {
                shape = !shape
                image_preview.setImageBitmap(
                    drawUtils.drawShapeFaces(
                        bitmap,
                        savedFaces,
                        shape,
                        shapeSize
                    )
                )
                button_shape.setCompoundDrawablesWithIntrinsicBounds(
                    if (shape) box else circle,
                    null,
                    null,
                    null
                )
            }
        }

        bar_size.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (bar_size.alpha == 1f) {
                    shapeSize = progress - 20
                    image_preview.setImageBitmap(
                        drawUtils.drawShapeFaces(
                            bitmap,
                            savedFaces,
                            shape,
                            shapeSize
                        )
                    )
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        button_save.setOnClickListener {
            if (button_save.alpha == 1f) {
                saveDialog()
            } else {
                Toast.makeText(this, "No face data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_CODE -> loadImage(imageUtils.cameraUri.toString())
                GALLERY_CODE -> loadImage(data?.dataString)
            }
        } else {
            when (requestCode) {
                CAMERA_CODE -> imageUtils.cancelCamera(this)
            }
        }
    }
    private fun loadImage(data: String?) {
        imageUpload = Uri.parse(data)
        image_preview.setImageURI(imageUpload)
        detectFaces(imageUpload)
    }

    @SuppressLint("SetTextI18n")
    private fun detectFaces(uri: Uri) {
        toggleButton(false)
        val image = InputImage.fromFilePath(this, uri)
        bitmap = imageUtils.getBitmapFromContentUri(contentResolver, uri)
        savedFaces = mutableListOf()
        text_info.text = "[DETECTING FACE]"

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    savedFaces = faces
                    text_info.text = "[${savedFaces.size} FACE DETECTED]"
                    image_preview.setImageBitmap(
                        drawUtils.drawShapeFaces(
                            bitmap,
                            savedFaces,
                            shape
                        )
                    )
                    toggleButton(true)
                } else { text_info.text = "[NO FACE DETECTED]" }
            }
            .addOnFailureListener { e -> text_info.text = "[ $e ]" }
    }

    private fun saveCropFaces(curBitmap: Bitmap, faces: MutableList<Face>) {
        for (face in faces) {
            val tempBitmap = drawUtils.cropShapeFaces(curBitmap, face, shape, shapeSize)

            imageUtils.saveImage(this, tempBitmap, "KOMOK-crop")
        }
    }

    private fun saveDialog() {
        // setup the alert builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Save Image")
        builder.setMessage("Are you sure you want to save the crop image result?")
        // add the buttons
        builder.setPositiveButton("Yes") { _, _ ->
            saveCropFaces(bitmap, savedFaces)
            Toast.makeText(this, "Success saved cropped face!", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No", null)
        // create and show the alert dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun toggleButton(bool: Boolean) {
        val n: Float = if (bool) 1f else 0.6f

        button_shape.alpha = n
        bar_size.alpha = n
        bar_size.progress = 20
        bar_size.isEnabled = bool
        button_save.alpha = n
    }
}