package com.teamC.komok

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.android.synthetic.main.activity_swap.*

class SwapActivity : AppCompatActivity() {
    private var rand = 0
    private val imageUtils = ImageUtils()
    private val drawUtils = DrawUtils()
    private val detector: FaceDetector
    private lateinit var imageUpload: Uri
    private lateinit var savedBitmap: Bitmap
    private lateinit var savedFaces: MutableList<Face>

    companion object {
        const val IMAGE_PICK_CODE = 100
    }

    init {
        val detectOptions = FaceDetectorOptions.Builder()
            .setClassificationMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            //.setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(1f)
            //.enableTracking()
            .build()
        detector = FaceDetection.getClient(detectOptions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap)

        imageUpload = Uri.parse(intent.getStringExtra("imageUpload"))

        image_preview.setImageURI(imageUpload)
        detectFaces(imageUpload)

        button_change.setOnClickListener {
            imageUtils.pickImageFromGallery(this, IMAGE_PICK_CODE)
        }

        button_duplicate.setOnClickListener {
            if (savedFaces.isNotEmpty()) {
                if (rand >= savedFaces.size-1) rand=0 else rand+=1
                savedBitmap = drawUtils.swapFaces(drawUtils.getBitmapFromUri(this, imageUpload), savedFaces, rand, true)
                image_preview.setImageBitmap(savedBitmap)
                enabledButton(2, true)
            } else {
                Toast.makeText(this, "No face data", Toast.LENGTH_SHORT).show()
            }
        }

        button_swap.setOnClickListener {
            if (savedFaces.isNotEmpty()) {
                if (rand+1 >= savedFaces.size-1) rand=0 else rand+=1
                savedBitmap = drawUtils.swapFaces(drawUtils.getBitmapFromUri(this, imageUpload), savedFaces, rand)
                image_preview.setImageBitmap(savedBitmap)
                enabledButton(2, true)
            } else {
                Toast.makeText(this, "No face data", Toast.LENGTH_SHORT).show()
            }
        }

        button_save.setOnClickListener {
            if (button_save.alpha == 1f) {
                imageUtils.saveImage(this, savedBitmap)
                Toast.makeText(this, "Success saved swapped face!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Swap face first!", Toast.LENGTH_SHORT).show()
            }
        }

        button_share.setOnClickListener {
            if (button_share.alpha == 1f) {
                imageUtils.shareImage(this, savedBitmap)
            } else {
                Toast.makeText(this, "Swap face first!", Toast.LENGTH_SHORT).show()
            }
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

    @SuppressLint("SetTextI18n")
    private fun detectFaces(uri: Uri) {
        val image = InputImage.fromFilePath(this, uri)
        val bitmap = drawUtils.getBitmapFromUri(this, uri)

        enabledButton(1, false)
        enabledButton(2, false)
        savedFaces = mutableListOf()
        text_info.text = "[DETECTING FACE]"

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    text_info.text = "[SCANNING FACE]"
                    //image_preview.setImageBitmap(drawUtils.drawRectFaces(bitmap, faces, Color.RED))
                    detectFacesContour(bitmap, faces) { newFaces ->
                        if (newFaces.isNotEmpty()) {
                            savedFaces = drawUtils.checkFacePoint(bitmap, newFaces)
                            if (savedFaces.isNotEmpty()) {
                                text_info.text = "[${savedFaces.size} FACE SCANNED]"

                                image_preview.setImageBitmap(drawUtils.drawContourFaces(bitmap, savedFaces))
                                if (button_swap.alpha < 1 && savedFaces.size > 1) {enabledButton(1, true)}
                            } else { text_info.text = "[FACE NOT PASS]" }
                        } else { text_info.text = "[FACE NOT PASS]" }
                    }
                } else { text_info.text = "[NO FACE DETECTED]" }
            }
            .addOnFailureListener { e -> text_info.text = "[ $e ]" }
    }

    private fun detectFacesContour(bitmap: Bitmap, faces: MutableList<Face>, callback: (MutableList<Face>) -> Unit) {
        val newFaces = mutableListOf<Face>()

        for (face in faces) {
            val tempBitmap = drawUtils.fillFaces(bitmap, faces, face)
            //imageUtils.saveImage(this, tempBitmap)
            val images = InputImage.fromBitmap(tempBitmap, 0)

            detector.process(images)
                .addOnSuccessListener { nFaces ->
                    if (nFaces.isNotEmpty()) {
                        for (nFace in nFaces) {
                            if (nFace.allContours.isNotEmpty()
                                && nFace.allContours[0].points.isNotEmpty()) {
                                newFaces.add(nFace)
                                break
                            }
                        }
                    }
                }
                .addOnCompleteListener { callback.invoke(newFaces) }
        }
    }

    private fun enabledButton(buttonGroup: Int, bool: Boolean) {
        if (buttonGroup == 1) {
            if (bool) {
                button_duplicate.alpha = 1f
                button_swap.alpha = 1f
            } else {
                button_duplicate.alpha = 0.6f
                button_swap.alpha = 0.6f
            }
        } else if (buttonGroup == 2) {
            if (bool) {
                button_save.alpha = 1f
                button_share.alpha = 1f
            } else {
                button_save.alpha = 0.6f
                button_share.alpha = 0.6f
            }
        }
    }
}