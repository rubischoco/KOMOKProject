package com.teamC.komok

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private val permissionUtils = PermissionUtils()
    private val imageUtils = ImageUtils()

    companion object {
        const val PERMISSION_CODE1 = 100
        const val PERMISSION_CODE2 = 101
        const val CAMERA_CODE1 = 102
        const val CAMERA_CODE2 = 103
        const val GALLERY_CODE1 = 104
        const val GALLERY_CODE2 = 105
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        button_swap.setOnClickListener {
            if (permissionUtils.requestPermission(this, PERMISSION_CODE1,
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                imageUtils.getImageDialog(this, CAMERA_CODE1, GALLERY_CODE1)
            }
        }

        button_crop.setOnClickListener {
            if (permissionUtils.requestPermission(this, PERMISSION_CODE2,
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                imageUtils.getImageDialog(this, CAMERA_CODE2, GALLERY_CODE2)
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
        if (permissionUtils.permissionGranted(requestCode, PERMISSION_CODE1, grantResults)) {
            imageUtils.getImageDialog(this, CAMERA_CODE1, GALLERY_CODE1)
        }
        else if (permissionUtils.permissionGranted(requestCode, PERMISSION_CODE2, grantResults)) {
            imageUtils.getImageDialog(this, CAMERA_CODE2, GALLERY_CODE2)
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("HomeActivity", "Res $resultCode Req $requestCode")
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_CODE1 -> myStartActivity(SwapActivity::class.java, imageUtils.cameraUri.toString())
                CAMERA_CODE2 -> myStartActivity(CropActivity::class.java, imageUtils.cameraUri.toString())
                GALLERY_CODE1 -> myStartActivity(SwapActivity::class.java, data?.dataString)
                GALLERY_CODE2 -> myStartActivity(CropActivity::class.java, data?.dataString)
            }
        } else {
            when (requestCode) {
                CAMERA_CODE1, CAMERA_CODE2 -> imageUtils.cancelCamera(this)
            }
        }
    }
    private fun <T> myStartActivity(activity: Class<T>, data: String?) {
        val intent = Intent(this, activity)
        intent.putExtra("imageUpload", data)
        startActivity(intent)
    }
}