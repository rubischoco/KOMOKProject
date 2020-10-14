package com.teamC.komok

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //val btnUpload: Button = findViewById(R.id.button_upload)

        findViewById<Button>(R.id.button_upload).setOnClickListener {
            if (PermissionUtils.requestPermission(this, PERMISSION_CODE,
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                pickImageFromGallery()
            }
        }

        findViewById<Button>(R.id.button_about).setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionUtils.permissionGranted(requestCode, PERMISSION_CODE, grantResults)) {
            pickImageFromGallery()
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val intent = Intent(this, SwapActivity::class.java)
            intent.putExtra("imageUpload", data?.data.toString())
            startActivity(intent)
        }
    }
}