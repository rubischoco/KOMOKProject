package com.teamC.komok

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.teamC.komok.adapter.CropFaceAdapter
import com.teamC.komok.utils.DrawUtils
import com.teamC.komok.utils.ImageUtils
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
    private lateinit var newFaces: MutableList<Face>
    private lateinit var selectFaces: MutableList<Int>

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

        // drawable untuk bentuk crop
        val box = ContextCompat.getDrawable(this, R.drawable.button_shapebox)
        val circle = ContextCompat.getDrawable(this, R.drawable.button_shapecircle)

        // tampilkan gambar dari intent sebelumnya dan deteksi wajah
        loadImage(intent.getStringExtra("imageUpload"))

        // tombol balik
        button_back.setOnClickListener { finish() }

        // tombol bantuan
        button_help.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            intent.putExtra("helpFragment", 1)
            startActivity(intent)
        }

        // tombol setting pilihan wajah
        button_setting.setOnClickListener {
            if (button_setting.alpha == 1f) {
                chooseFaceDialog()
            } else {
                Toast.makeText(this, "No face data", Toast.LENGTH_SHORT).show()
            }
        }

        // tombol ganti gambar
        button_change.setOnClickListener {
            imageUtils.getImageDialog(this, CAMERA_CODE, GALLERY_CODE)
        }

        // tombol ganti bentuk crop
        button_shape.setOnClickListener {
            if (button_shape.alpha == 1f) {
                shape = !shape
                Glide.with(this)
                    .load(drawUtils.drawShapeFaces(bitmap, newFaces, shape, shapeSize))
                    .into(image_preview)
                button_shape.setCompoundDrawablesWithIntrinsicBounds(
                    if (shape) box else circle,
                    null,
                    null,
                    null
                )
            }
        }

        // slider ukuran bentuk crop
        bar_size.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (bar_size.alpha == 1f) {
                    shapeSize = progress - 20
                    image_preview.setImageBitmap(drawUtils.drawShapeFaces(bitmap, newFaces, shape, shapeSize))
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // tombol untuk simpan gambar
        button_save.setOnClickListener {
            if (button_save.alpha == 1f) {
                saveDialog()
            } else {
                Toast.makeText(this, "No face data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // setelah mendapatkan suatu gambar
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
    // menampilkan gambar dan mendeteksi wajah
    private fun loadImage(data: String?) {
        imageUpload = Uri.parse(data)
        Glide.with(this)
            .load(imageUpload)
            .into(image_preview)
        detectFaces(imageUpload)
    }

    // deteksi wajah dalam gambar
    @SuppressLint("SetTextI18n")
    private fun detectFaces(uri: Uri) {
        // nonaktifkan beberapa tombol
        toggleButton(false)
        // gambar input untuk detektor
        val image = InputImage.fromFilePath(this, uri)
        // gambar input dalam bitmap untuk canvas
        bitmap = imageUtils.getBitmapFromContentUri(contentResolver, uri)
        // kosongkan data wajah dan perbarui teks info
        shapeSize = 0
        savedFaces = mutableListOf()
        selectFaces = mutableListOf()
        text_info.text = "[DETECTING FACE]"

        // deteksi wajah
        detector.process(image)
                // jika detektor aman
            .addOnSuccessListener { faces ->
                // jika ditemukan wajah
                if (faces.isNotEmpty()) {
                    // perbarui data wajah dan teks info
                    savedFaces = faces
                    newFaces = faces
                    selectFaces = MutableList(savedFaces.size) {1}
                    text_info.text = "[${savedFaces.size} FACE] [${selectFaces.sum()} SELECTED]"
                    // tampilkan gambar dengan wajah yang terdeteksi
                    Glide.with(this)
                        .load(drawUtils.drawShapeFaces(bitmap, savedFaces, shape))
                        .into(image_preview)
                    // aktifkan tombol
                    toggleButton(true)
                }
                // jika tidak ditemukan wajah
                else { text_info.text = "[NO FACE DETECTED]" }
            }
                // jika detektor gagal
            .addOnFailureListener { e -> text_info.text = "[ $e ]" }
    }

    // simpan potongan wajah pada gambar
    private fun saveCropFaces(curBitmap: Bitmap, faces: MutableList<Face>) {
        for (face in faces) {
            // dapatkan potongan wajah
            val tempBitmap = drawUtils.cropShapeFaces(curBitmap, face, shape, shapeSize)
            // simpan potongan wajah
            imageUtils.saveImage(this, tempBitmap, "KOMOK-crop")
        }
    }

    // dialog untuk menyimpan gambar
    private fun saveDialog() {
        // setup the alert builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Save Image")
        builder.setMessage("Are you sure you want to save the crop image result?")
        // add the buttons
        builder.setPositiveButton("Yes") { _, _ ->
            saveCropFaces(bitmap, newFaces)
            Toast.makeText(this, "Success saved cropped face!", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No", null)
        // create and show the alert dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // dialog untuk memilih wajah
    @SuppressLint("SetTextI18n")
    private fun chooseFaceDialog() {
        // fungsi jika dialog aktif
        toggleDialog(true)
        // susun dan tampilkan dialog kustom
        val customDialog = BottomSheetDialog(this).apply {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setContentView(R.layout.botdialog_face_select)
            setOnDismissListener {
                toggleDialog(false)
            }
        }
        customDialog.show()
        // ambil referensi view dari dialog
        val textDialog = customDialog.findViewById<TextView>(R.id.text_dialog)
        val buttonClose = customDialog.findViewById<Button>(R.id.button_close)
        val recyclerFace = customDialog.findViewById<RecyclerView>(R.id.recycler_face)
        // setting untuk recyclerview
        val chooseFaceAdapter = CropFaceAdapter(this, bitmap, savedFaces, selectFaces, textDialog)
        recyclerFace?.adapter = chooseFaceAdapter
        recyclerFace?.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        // ganti text dialog
        textDialog?.text = "[${savedFaces.size} FACE] [${selectFaces.sum()} SELECTED]"
        // tombol tutup dialog
        buttonClose?.setOnClickListener {
            customDialog.dismiss()
        }
    }
    // fungsi tergantung kondisi dialog
    @SuppressLint("SetTextI18n")
    private fun toggleDialog(bool: Boolean) {
        if (bool) {
            Glide.with(this)
                .load(drawUtils.drawShapeFaces(bitmap, savedFaces))
                .into(image_preview)
        } else {
            newFaces = drawUtils.getSelectedFaces(savedFaces, selectFaces)
            Glide.with(this)
                .load(drawUtils.drawShapeFaces(bitmap, newFaces, shape, shapeSize))
                .into(image_preview)

            text_info.text = "[${savedFaces.size} FACE] [${selectFaces.sum()} SELECTED]"
        }
    }

    // merubah kondisi beberapa tombol
    private fun toggleButton(bool: Boolean) {
        val n: Float = if (bool) 1f else 0.6f

        button_setting.alpha = n
        button_shape.alpha = n
        bar_size.alpha = n
        bar_size.progress = 20
        bar_size.isEnabled = bool
        button_save.alpha = n
    }
}