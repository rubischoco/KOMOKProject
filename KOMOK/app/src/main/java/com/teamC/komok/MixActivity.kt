package com.teamC.komok

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.teamC.komok.adapter.MixFaceAdapter1
import com.teamC.komok.adapter.MixFaceAdapter2
import com.teamC.komok.retrofit.GalleryResponse
import com.teamC.komok.retrofit.RetrofitClient
import com.teamC.komok.utils.DrawUtils
import com.teamC.komok.utils.ImageUtils
import kotlinx.android.synthetic.main.activity_mix.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MixActivity : AppCompatActivity() {
    private val imageUtils = ImageUtils()
    private val drawUtils = DrawUtils()
    private val detector: FaceDetector
    private var apiGallery: MutableList<GalleryResponse> = mutableListOf()
    private lateinit var bitmap: Bitmap
    private lateinit var imageUpload: Uri
    private lateinit var savedFaces: MutableList<Face>
    private lateinit var mixList: MutableList<Pair<Face, Bitmap>>

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
        setContentView(R.layout.activity_mix)

        RetrofitClient.api.getGallery().enqueue(object: Callback<List<GalleryResponse>> {
            override fun onResponse(
                call: Call<List<GalleryResponse>>,
                response: Response<List<GalleryResponse>>
            ) {
                apiGallery = response.body() as MutableList<GalleryResponse>
            }

            override fun onFailure(call: Call<List<GalleryResponse>>, t: Throwable) {}
        })

        // tampilkan gambar dari intent sebelumnya dan deteksi wajah
        loadImage(intent.getStringExtra("imageUpload"))

        // tombol balik
        button_back.setOnClickListener { finish() }

        // tombol bantuan
        button_help.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            intent.putExtra("helpFragment", 2)
            startActivity(intent)
        }

        // tombol ganti gambar
        button_change.setOnClickListener {
            imageUtils.getImageDialog(this, CAMERA_CODE, GALLERY_CODE)
        }

        // tombol tambah bagian
        button_mix.setOnClickListener {
            if (button_mix.alpha == 1f) {
                mixFaceDialog1()
            }
        }

        // tombol untuk lihat list bagian tambahan
        button_list.setOnClickListener {
            if (button_list.alpha == 1f) {
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
            }
        }

        // tombol untuk simpan gambar
        button_save.setOnClickListener {
            if (button_save.alpha == 1f && mixList.size > 0) {
                saveDialog()
            }
        }

        // tombol untuk share gambar
        button_share.setOnClickListener {
            if (button_share.alpha == 1f && mixList.size > 0) {
                imageUtils.shareImage(this, drawUtils.drawMixFace(bitmap, mixList))
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

    @SuppressLint("SetTextI18n")
    private fun detectFaces(uri: Uri) {
        // nonaktifkan beberapa tombol
        toggleButton(false)
        // gambar input untuk detektor
        val image = InputImage.fromFilePath(this, uri)
        // gambar input dalam bitmap untuk canvas
        bitmap = imageUtils.getBitmapFromContentUri(contentResolver, uri)
        // kosongkan data wajah dan perbarui teks info
        savedFaces = mutableListOf()
        mixList = mutableListOf()
        text_info.text = "[DETECTING FACE]"

        // deteksi wajah
        detector.process(image)
            // jika detektor aman
            .addOnSuccessListener { faces ->
                // jika ditemukan wajah
                if (faces.isNotEmpty()) {
                    // perbarui data wajah dan teks info
                    savedFaces = faces
                    text_info.text = "[${savedFaces.size} FACE]"
                    // tampilkan gambar dengan wajah yang terdeteksi
                    Glide.with(this)
                        .load(drawUtils.drawShapeFaces(bitmap, savedFaces))
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

    // dialog menyimpan gambar
    private fun saveDialog() {
        // setup the alert builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Save Image")
        builder.setMessage("Are you sure you want to save the mix image result?")
        // add the buttons
        builder.setPositiveButton("Yes") { _, _ ->
            val bmp = drawUtils.drawMixFace(bitmap, mixList)
            imageUtils.saveImage(this, bmp)
            Toast.makeText(this, "Success saved mixed face!", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No", null)
        // create and show the alert dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // dialog untuk memilih wajah
    @SuppressLint("SetTextI18n")
    private fun mixFaceDialog1() {
        // susun dan tampilkan dialog kustom
        val customDialog = BottomSheetDialog(this).apply {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setContentView(R.layout.botdialog_mix_face)
        }
        customDialog.show()
        // ambil referensi view dari dialog
        val buttonClose = customDialog.findViewById<Button>(R.id.button_close)
        val recyclerFace = customDialog.findViewById<RecyclerView>(R.id.recycler_face)
        val mixLayout = customDialog.findViewById<LinearLayout>(R.id.mix_layout)
        val cardSelect = customDialog.findViewById<CardView>(R.id.card_select)
        val spinnerMix = customDialog.findViewById<Spinner>(R.id.spinner_mix)
        //val recyclerMix = customDialog.findViewById<RecyclerView>(R.id.recycler_mix)
        // setting untuk recyclerview (pilih wajah)
        val mixFaceAdapter1 = MixFaceAdapter1(this, bitmap, savedFaces, recyclerFace, mixLayout)
        recyclerFace?.adapter = mixFaceAdapter1
        recyclerFace?.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        // list sementara untuk bagian tambahan
        val items = arrayOf("Face", "Eyes", "Ears", "Nose", "Mouth")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinnerMix?.adapter = adapter
        // tombol tutup dialog
        buttonClose?.setOnClickListener { customDialog.dismiss() }
        // tombol pilih ulang wajah
        cardSelect?.setOnClickListener {
            // balik ke recycler 1
            recyclerFace?.visibility = View.VISIBLE
            mixLayout?.visibility = View.GONE
        }
    }
    fun mixFace1(layoutSelect: LinearLayout, facePos: Int) {
        // set gambar wajah yang terpilih untuk di mix
        Glide.with(this)
            .load(drawUtils.cropShapeFaces(bitmap, savedFaces[facePos]))
            .into(layoutSelect.findViewById(R.id.image_select))
        // adapter untuk recyclerview (pilih mix yang ingin ditambahkan)
        val recyclerMix = layoutSelect.findViewById<RecyclerView>(R.id.recycler_mix)
        val mixFaceAdapter2 = MixFaceAdapter2(this, facePos, apiGallery)
        recyclerMix?.adapter = mixFaceAdapter2
        recyclerMix?.layoutManager = GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false)
        // tampilkan layout
        layoutSelect.visibility = View.VISIBLE
    }
    fun mixFace2(facePos: Int, bitmapMix: Bitmap) {
        mixList.add(Pair(savedFaces[facePos], bitmapMix))
        val bmp = drawUtils.drawMixFace(bitmap, mixList)
        Glide.with(this)
            .load(bmp)
            .into(image_preview)
    }

    // merubah kondisi beberapa tombol
    private fun toggleButton(bool: Boolean) {
        val n: Float = if (bool) 1f else 0.6f

        button_mix.alpha = n
        button_list.alpha = n
        button_save.alpha = n
        button_share.alpha = n
    }
}