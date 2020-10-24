package com.teamC.komok

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private val permissionUtils = PermissionUtils()
    private val imageUtils = ImageUtils()

    companion object {
        const val PERMISSION_CODE = 100
        const val IMAGE_PICK_CODE1 = 101
        const val IMAGE_PICK_CODE2 = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        button_swap.setOnClickListener {
            if (permissionUtils.requestPermission(this, PERMISSION_CODE,
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                imageUtils.pickImageFromGallery(this, IMAGE_PICK_CODE1)
            }
        }

        button_crop.setOnClickListener {
            if (permissionUtils.requestPermission(this, PERMISSION_CODE,
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                imageUtils.pickImageFromGallery(this, IMAGE_PICK_CODE2)
            }
        }

        button_about.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionUtils.permissionGranted(requestCode, PERMISSION_CODE, grantResults)) {
            imageUtils.pickImageFromGallery(this, IMAGE_PICK_CODE1)
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_CODE1) {
                val intent = Intent(this, SwapActivity::class.java)
                intent.putExtra("imageUpload", data?.data.toString())
                startActivity(intent)
            }
            else if (requestCode == IMAGE_PICK_CODE2) {
                val intent = Intent(this, CropActivity::class.java)
                intent.putExtra("imageUpload", data?.data.toString())
                startActivity(intent)
            }
        }
    }
}