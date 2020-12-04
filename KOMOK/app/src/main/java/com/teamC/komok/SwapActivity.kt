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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.teamC.komok.adapter.*
import com.teamC.komok.utils.DrawUtils
import com.teamC.komok.utils.ImageUtils
import kotlinx.android.synthetic.main.activity_swap.*

class SwapActivity : AppCompatActivity() {
    private val imageUtils = ImageUtils()
    private val drawUtils = DrawUtils()
    private val detector: FaceDetector
//    private var randPos = 0
    private var swapMode = Pair(0, 0)
    private var watermark = true
    private lateinit var imageUpload: Uri
    private lateinit var bitmap: Bitmap
    private lateinit var savedBitmap: Bitmap
    private lateinit var watermarkBitmap: Bitmap
    private lateinit var savedFaces: MutableList<Face>
    private lateinit var newFaces: MutableList<Face>
    private lateinit var selectFaces: MutableList<Int>
//    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        const val CAMERA_CODE = 100
        const val GALLERY_CODE = 101
    }

    init {
        val detectOptions = FaceDetectorOptions.Builder()
            .setClassificationMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setMinFaceSize(1f)
            .build()
        detector = FaceDetection.getClient(detectOptions)
    }

//    override fun onStart() {
//        super.onStart()
//        if (firebaseAuth.currentUser != null){
//            firebaseAuth.currentUser?.reload()
//        }
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap)

//        firebaseAuth = FirebaseAuth.getInstance()
//        val user = firebaseAuth.currentUser

        val watermarkOn = ContextCompat.getDrawable(this, R.drawable.button_watermark_on)
        val watermarkOff = ContextCompat.getDrawable(this, R.drawable.button_watermark_off)
        watermarkBitmap = ContextCompat.getDrawable(this, R.drawable.komok_splash)?.toBitmap()!!

        imageUpload = Uri.parse(intent.getStringExtra("imageUpload"))
        Glide.with(this)
            .load(imageUpload)
            .into(image_preview)
        detectFaces(imageUpload)

        button_back.setOnClickListener { finish() }

        button_watermark.setOnClickListener {
            if (button_watermark.alpha == 1f) {
                watermark = !watermark
                Glide.with(this)
                    .load(bitmapWithWatermark(watermark, savedBitmap, watermarkBitmap))
                    .into(image_preview)
                button_watermark.setCompoundDrawablesWithIntrinsicBounds(
                    if (watermark) watermarkOn else watermarkOff,
                    null,
                    null,
                    null
                )
            }
//            if (user != null) {
//                if (user.isEmailVerified) {
//                    // WATERMARK CODE
//                } else {
//                    Toast.makeText(this, "Verify your email first", Toast.LENGTH_SHORT).show()
//                }
//            }
        }

        button_help.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            intent.putExtra("helpFragment", 0)
            startActivity(intent)
        }

        button_setting.setOnClickListener {
            if (button_setting.alpha == 1f) {
                chooseFaceDialog()
            }
        }

        button_change.setOnClickListener {
            imageUtils.getImageDialog(this, CAMERA_CODE, GALLERY_CODE)
        }

        button_duplicate.setOnClickListener {
            if (button_duplicate.alpha == 1f) {
                duplicateFaceDialog()
//                if (randPos >= newFaces.size-1) {randPos=0} else {randPos+=1}
//                savedBitmap = drawUtils.swapFaces(bitmap, newFaces, randPos, true)
//                Glide.with(this)
//                    .load(bitmapWithWatermark(watermark, savedBitmap, watermarkBitmap))
//                    .into(image_preview)
//                toggleButton(true, 2)
            }
        }

        button_swap.setOnClickListener {
            if (button_swap.alpha == 1f) {
                swapFaceDialog()
//                if (randPos+1 >= newFaces.size-1) {randPos=0} else {randPos+=1}
//                savedBitmap = drawUtils.swapFaces(bitmap, newFaces, randPos)
//                Glide.with(this)
//                    .load(bitmapWithWatermark(watermark, savedBitmap, watermarkBitmap))
//                    .into(image_preview)
//                toggleButton(true, 2)
            }
        }

        button_save.setOnClickListener {
            if (button_save.alpha == 1f) {
                saveDialog()
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
        Glide.with(this)
            .load(imageUpload)
            .into(image_preview)
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
                    //image_preview.setImageBitmap(drawUtils.drawShapeFaces(bitmap, faces))
                    detectFacesContour(bitmap, faces) { detFaces ->
                        if (detFaces.isNotEmpty()) {
                            savedFaces = drawUtils.checkFacePoint(bitmap, detFaces)
                            newFaces = savedFaces
                            selectFaces = MutableList(savedFaces.size) {1}

                            if (savedFaces.isNotEmpty()) {
                                text_info.text = "[${savedFaces.size} FACE]  [${selectFaces.sum()} SELECTED]"
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


    // dialog untuk duplicate face
    private fun duplicateFaceDialog() {
        // susun dan tampilkan dialog kustom
        val customDialog = BottomSheetDialog(this).apply {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setContentView(R.layout.botdialog_duplicate_face)
        }
        customDialog.show()
        // ambil referensi view dari dialog
        val buttonClose = customDialog.findViewById<Button>(R.id.button_close)
        val recyclerDuplicate = customDialog.findViewById<RecyclerView>(R.id.recycler_duplicate)
        val cardDuplicate = customDialog.findViewById<CardView>(R.id.card_duplicate)
        val selectLayout = customDialog.findViewById<LinearLayout>(R.id.select_layout)
        //val recyclerSelect = customDialog.findViewById<RecyclerView>(R.id.recycler_select)
        //val cardSelect = customDialog.findViewById<CardView>(R.id.card_select)
        //val settingLayout = customDialog.findViewById<LinearLayout>(R.id.setting_layout)
        //val recyclerSetting = customDialog.findViewById<RecyclerView>(R.id.recycler_setting)
        // adapter untuk recyclerview (pilih wajah untuk duplicate)
        val duplicateFaceAdapter1 = DuplicateFaceAdapter1(this, bitmap, newFaces, recyclerDuplicate, selectLayout)
        recyclerDuplicate?.adapter = duplicateFaceAdapter1
        recyclerDuplicate?.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        // jika sudah pernah duplicate
        if (swapMode.first == 1) {
            duplicateFaceAdapter1.nextRecycler(swapMode.second)
        }
        // tombol tutup dialog
        buttonClose?.setOnClickListener { customDialog.dismiss() }
        // tombol pilih ulang wajah untuk duplicate
        cardDuplicate?.setOnClickListener {
            // reset mode dan gambar ke awal
            toggleDialog(true)
            // balik ke recycler 1
            recyclerDuplicate?.visibility = View.VISIBLE
            selectLayout?.visibility = View.GONE
        }
    }
    // fungsi untuk mode duplicate 1
    fun duplicateFace1(layoutSelect: LinearLayout, facePos: Int, fromAdapter: Boolean=false) {
        // jika fungsi dipanggil dari adapter
        if (fromAdapter) {
            // set mode swap ke duplicate
            swapMode = Pair(1, facePos)
            // aktifkan tombol 2
            toggleButton(true, 2)
            // tampilkan gambar duplicate
            savedBitmap = drawUtils.swapFaces(bitmap, newFaces, facePos, true)
            Glide.with(this)
                .load(bitmapWithWatermark(watermark, savedBitmap, watermarkBitmap))
                .into(image_preview)
        }
        // set gambar wajah yang terpilih untuk di duplicate
        val bmp = drawUtils.cropShapeFaces(bitmap, newFaces[facePos])
        Glide.with(this)
            .load(bmp)
            .into(layoutSelect.findViewById(R.id.image_duplicate))
        // adapter untuk recyclerview (pilih wajah untuk di setting)
        val recyclerSelect = layoutSelect.findViewById<RecyclerView>(R.id.recycler_select)
        val duplicateFaceAdapter2 = DuplicateFaceAdapter2(this, bitmap, newFaces, swapMode.second)
        recyclerSelect?.adapter = duplicateFaceAdapter2
        recyclerSelect?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // tampilkan layout
        layoutSelect.visibility = View.VISIBLE
    }



    // dialog untuk swap face
    private fun swapFaceDialog() {
        // susun dan tampilkan dialog kustom
        val customDialog = BottomSheetDialog(this).apply {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setContentView(R.layout.botdialog_swap_face)
        }
        customDialog.show()
        // ambil referensi view dari dialog
        val buttonClose = customDialog.findViewById<Button>(R.id.button_close)
        val recyclerSwap = customDialog.findViewById<RecyclerView>(R.id.recycler_swap)
        val cardSwap = customDialog.findViewById<CardView>(R.id.card_swap)
        val selectLayout = customDialog.findViewById<LinearLayout>(R.id.select_layout)
        //val recyclerSelect = customDialog.findViewById<RecyclerView>(R.id.recycler_select)
        //val cardSelect = customDialog.findViewById<CardView>(R.id.card_select)
        //val settingLayout = customDialog.findViewById<LinearLayout>(R.id.setting_layout)
        //val recyclerSetting = customDialog.findViewById<RecyclerView>(R.id.recycler_setting)
        // adapter untuk recyclerview (pilih wajah untuk duplicate)
        val swapFaceAdapter1 = SwapFaceAdapter1(this, newFaces, recyclerSwap, selectLayout)
        recyclerSwap?.adapter = swapFaceAdapter1
        recyclerSwap?.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        // jika sudah pernah duplicate
        if (swapMode.first == 2) {
            swapFaceAdapter1.nextRecycler(swapMode.second)
        }
        // tombol tutup dialog
        buttonClose?.setOnClickListener { customDialog.dismiss() }
        // tombol pilih ulang wajah untuk swap
        cardSwap?.setOnClickListener {
            // reset mode dan gambar ke awal
            toggleDialog(true)
            // balik ke recycler 1
            recyclerSwap?.visibility = View.VISIBLE
            selectLayout?.visibility = View.GONE
        }
    }
    // fungsi untuk mode swap 1
    fun swapFace1(layoutSelect: LinearLayout, facePos: Int, fromAdapter: Boolean=false) {
        // jika fungsi dipanggil dari adapter
        if (fromAdapter) {
            // set mode swap ke duplicate
            swapMode = Pair(2, facePos)
            // aktifkan tombol 2
            toggleButton(true, 2)
            // tampilkan gambar duplicate
            savedBitmap = drawUtils.swapFaces(bitmap, newFaces, facePos)
            Glide.with(this)
                .load(bitmapWithWatermark(watermark, savedBitmap, watermarkBitmap))
                .into(image_preview)
        }
        // set angka sesuai nomor swap
        layoutSelect.findViewById<TextView>(R.id.text_swap).text = (facePos+1).toString()
        // adapter untuk recyclerview (pilih wajah untuk di setting)
        val recyclerSelect = layoutSelect.findViewById<RecyclerView>(R.id.recycler_select)
        val swapFaceAdapter2 = SwapFaceAdapter2(this, bitmap, newFaces, swapMode.second)
        recyclerSelect?.adapter = swapFaceAdapter2
        recyclerSelect?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // tampilkan layout
        layoutSelect.visibility = View.VISIBLE
    }


    // dialog untuk memilih wajah awal
    @SuppressLint("SetTextI18n")
    private fun chooseFaceDialog() {
        // fungsi jika dialog aktif
        toggleDialog(true)
        // susun dan tampilkan dialog kustom
        val customDialog = BottomSheetDialog(this).apply {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setContentView(R.layout.botdialog_face_select)
            setOnDismissListener { toggleDialog(false) }
        }
        customDialog.show()
        // ambil referensi view dari dialog
        val textDialog = customDialog.findViewById<TextView>(R.id.text_dialog)
        val buttonClose = customDialog.findViewById<Button>(R.id.button_close)
        val recyclerFace = customDialog.findViewById<RecyclerView>(R.id.recycler_face)
        // adapter untuk recyclerview (pilih wajah yang akan diolah)
        val chooseFaceAdapter = ChooseFaceAdapter(this, bitmap, savedFaces, selectFaces, textDialog, 2)
        recyclerFace?.adapter = chooseFaceAdapter
        recyclerFace?.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        // ganti text dialog
        textDialog?.text = "[${savedFaces.size} FACE] [${selectFaces.sum()} SELECTED]"
        // tombol tutup dialog
        buttonClose?.setOnClickListener { customDialog.dismiss() }
    }
    // fungsi tergantung kondisi dialog
    @SuppressLint("SetTextI18n")
    private fun toggleDialog(bool: Boolean) {
        // jika terdapat dialog pilih wajah awal
        if (bool) {
            // reset mode swap
            swapMode = Pair(0, 0)
            // nonaktifkan tombol 2
            toggleButton(false, 2)
            // tampilkan gambar awal
            Glide.with(this)
                .load(drawUtils.drawContourFaces(bitmap, savedFaces))
                .into(image_preview)
        }
        // jika dialog pilih wajah awal ditutup
        else {
            // dapatkan pilihan wajah baru
            newFaces = drawUtils.getSelectedFaces(savedFaces, selectFaces)
            // tampilkan gambar pilihan wajah terbaru
            savedBitmap = drawUtils.drawContourFaces(bitmap, newFaces)
            Glide.with(this)
                .load(savedBitmap)
                .into(image_preview)
            // ganti text dialog
            text_info.text = "[${savedFaces.size} FACE] [${selectFaces.sum()} SELECTED]"
        }
    }

    // merubah kondisi beberapa kelompok tombol
    private fun toggleButton(bool: Boolean, buttonGroup: Int=0) {
        val n: Float = if (bool) 1f else 0.6f

        if (buttonGroup == 0 || buttonGroup == 1)  {
            button_watermark.alpha = n
            button_setting.alpha = n
            button_duplicate.alpha = n
            button_swap.alpha = n
        }
        if (buttonGroup == 0 || buttonGroup == 2) {
            button_save.alpha = n
            button_share.alpha = n
        }
    }
}