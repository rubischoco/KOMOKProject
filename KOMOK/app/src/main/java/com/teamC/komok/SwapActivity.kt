package com.teamC.komok

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.android.synthetic.main.activity_swap.*

class SwapActivity : AppCompatActivity() {
    private val imageUtils = ImageUtils()
    private val drawUtils = DrawUtils()
    private val detector: FaceDetector
    private var randPos = 0
    private var watermark = true
    private lateinit var imageUpload: Uri
    private lateinit var bitmap: Bitmap
    private lateinit var savedBitmap: Bitmap
    private lateinit var watermarkBitmap: Bitmap
    private lateinit var savedFaces: MutableList<Face>
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        const val CAMERA_CODE = 100
        const val GALLERY_CODE = 101
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

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null){
            firebaseAuth.currentUser?.reload()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser

        val watermarkOn = ContextCompat.getDrawable(this, R.drawable.button_watermark_on)
        val watermarkOff = ContextCompat.getDrawable(this, R.drawable.button_watermark_off)
        watermarkBitmap = ContextCompat.getDrawable(this, R.drawable.komok_splash)?.toBitmap()!!

        imageUpload = Uri.parse(intent.getStringExtra("imageUpload"))

        image_preview.setImageURI(imageUpload)
        detectFaces(imageUpload)

        button_back.setOnClickListener { finish() }

        button_watermark.setOnClickListener {
            if (user != null) {
                if (user.isEmailVerified) {
                    if (button_watermark.alpha == 1f) {
                        watermark = !watermark
                        image_preview.setImageBitmap(bitmapWithWatermark(watermark, savedBitmap, watermarkBitmap))
                        button_watermark.setCompoundDrawablesWithIntrinsicBounds(
                            if (watermark) watermarkOn else watermarkOff,
                            null,
                            null,
                            null
                        )
                    }
                } else {
                    Toast.makeText(this, "Verify your email first", Toast.LENGTH_SHORT).show()
                }
            }
        }

        button_help.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
        }

        button_change.setOnClickListener {
            imageUtils.getImageDialog(this, CAMERA_CODE, GALLERY_CODE)
        }

        button_duplicate.setOnClickListener {
            if (button_duplicate.alpha == 1f) {
                if (randPos >= savedFaces.size-1) {randPos=0} else {randPos+=1}
                savedBitmap = drawUtils.swapFaces(bitmap, savedFaces, randPos, true)
                image_preview.setImageBitmap(bitmapWithWatermark(watermark, savedBitmap, watermarkBitmap))
                toggleButton(true, 2)
            } else {
                Toast.makeText(this, "Not enough face data", Toast.LENGTH_SHORT).show()
            }
        }

        button_swap.setOnClickListener {
            if (button_swap.alpha == 1f) {
                if (randPos+1 >= savedFaces.size-1) {randPos=0} else {randPos+=1}
                savedBitmap = drawUtils.swapFaces(bitmap, savedFaces, randPos)
                image_preview.setImageBitmap(bitmapWithWatermark(watermark, savedBitmap, watermarkBitmap))
                toggleButton(true, 2)
            } else {
                Toast.makeText(this, "Not enough face data", Toast.LENGTH_SHORT).show()
            }
        }

        button_save.setOnClickListener {
            if (button_save.alpha == 1f) {
                saveDialog()
            } else {
                Toast.makeText(this, "Swap face first!", Toast.LENGTH_SHORT).show()
            }
        }

        button_share.setOnClickListener {
            if (button_share.alpha == 1f) {
                imageUtils.shareImage(this, bitmapWithWatermark(watermark, savedBitmap, watermarkBitmap))
            } else {
                Toast.makeText(this, "Swap face first!", Toast.LENGTH_SHORT).show()
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
                    text_info.text = "[SCANNING FACE]"
                    //image_preview.setImageBitmap(drawUtils.drawRectFaces(bitmap, faces, Color.RED))
                    detectFacesContour(bitmap, faces) { newFaces ->
                        if (newFaces.isNotEmpty()) {
                            savedFaces = drawUtils.checkFacePoint(bitmap, newFaces)

                            if (savedFaces.isNotEmpty()) {
                                text_info.text = "[${savedFaces.size} FACE SCANNED]"
                                savedBitmap = drawUtils.drawContourFaces(bitmap, savedFaces)
                                image_preview.setImageBitmap(bitmapWithWatermark(watermark, savedBitmap, watermarkBitmap))

                                if (button_swap.alpha < 1 && savedFaces.size > 1) {toggleButton(true, 1)}
                            } else { text_info.text = "[FACE NOT PASS]" }
                        } else { text_info.text = "[FACE NOT PASS]" }
                    }
                } else { text_info.text = "[NO FACE DETECTED]" }
            }
            .addOnFailureListener { e -> text_info.text = "[ $e ]" }
    }

    private fun detectFacesContour(curBitmap: Bitmap, faces: MutableList<Face>, callback: (MutableList<Face>) -> Unit) {
        val newFaces = mutableListOf<Face>()

        for (face in faces) {
            val tempBitmap = drawUtils.fillFaces(curBitmap, faces, face)
            val images = InputImage.fromBitmap(tempBitmap, 0)
            //imageUtils.saveImage(this, tempBitmap)
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

    private fun bitmapWithWatermark(watermark: Boolean, bitmap: Bitmap, watermarkBitmap: Bitmap): Bitmap {
        return if (watermark) { drawUtils.drawWatermark(bitmap, watermarkBitmap) }
        else { bitmap }
    }

    private fun saveDialog() {
        // setup the alert builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Save Image")
        builder.setMessage("Are you sure you want to save the swap image result?")
        // add the buttons
        builder.setPositiveButton("Yes") { _, _ ->
            imageUtils.saveImage(this, bitmapWithWatermark(watermark, savedBitmap, watermarkBitmap))
            Toast.makeText(this, "Success saved swapped face!", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No", null)
        // create and show the alert dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun toggleButton(bool: Boolean, buttonGroup: Int=0) {
        val n: Float = if (bool) 1f else 0.6f

        if (buttonGroup == 0 || buttonGroup == 1)  {
            button_watermark.alpha = n
            button_duplicate.alpha = n
            button_swap.alpha = n
        }
        if (buttonGroup == 0 || buttonGroup == 2) {
            button_save.alpha = n
            button_share.alpha = n
        }
    }
}