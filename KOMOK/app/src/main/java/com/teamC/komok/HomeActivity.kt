package com.teamC.komok

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    private val permissionUtils = PermissionUtils()
    private val imageUtils = ImageUtils()
    private lateinit var handler: Handler
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        const val PERMISSION_CODE1 = 100
        const val PERMISSION_CODE2 = 101
        const val CAMERA_CODE1 = 102
        const val CAMERA_CODE2 = 103
        const val GALLERY_CODE1 = 104
        const val GALLERY_CODE2 = 105
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null){
            firebaseAuth.currentUser?.reload()
            button_swap.setBackgroundResource(R.drawable.button_border)
            button_user.setBackgroundResource(R.drawable.button_border)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        handler = Handler()
        firebaseAuth = FirebaseAuth.getInstance()

        button_user.setOnClickListener {
            if (firebaseAuth.currentUser != null) {
                profileDialog()
            } else {
                loginDialog()
            }
        }

        button_swap.setOnClickListener {
            if (firebaseAuth.currentUser != null) {
                if (permissionUtils.requestPermission(this, PERMISSION_CODE1,
                        Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    imageUtils.getImageDialog(this, CAMERA_CODE1, GALLERY_CODE1)
                }
            } else {
                Toast.makeText(this, "You need to login first", Toast.LENGTH_SHORT).show()

                loginDialog()
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

    private fun loginDialog(email: String="") {
        val customDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_login)
            setCancelable(true)
        }
        customDialog.show()

        val inputEmail = customDialog.findViewById<EditText>(R.id.input_email)
        val inputPassword = customDialog.findViewById<EditText>(R.id.input_password)
        val loadingLogin = customDialog.findViewById<ProgressBar>(R.id.loading_login)
        val textForgot = customDialog.findViewById<TextView>(R.id.text_forgot)
        val buttonLogin = customDialog.findViewById<Button>(R.id.button_login)
        val buttonRegister = customDialog.findViewById<Button>(R.id.button_register)

        loadingLogin.visibility = View.GONE
        inputEmail.setText(email)
        inputHighlightRemove(inputEmail)

        textForgot.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
        }

        buttonLogin.setOnClickListener {
            val dataEmail = inputEmail.text.toString()
            val dataPassword = inputPassword.text.toString()
            var check = true

            if (!Patterns.EMAIL_ADDRESS.matcher(dataEmail).matches()) {
                inputEmail.error = "email not valid"
                inputEmail.requestFocus()
                check = false
            }
            if (dataPassword.length < 6) {
                inputPassword.error = "password must be at last 6 characters"
                if (check) {
                    inputPassword.requestFocus()
                    check = false
                }
            }

            if (check) {
                loadingLogin.visibility = View.VISIBLE
                // login user
                firebaseAuth.signInWithEmailAndPassword(dataEmail, dataPassword)
                    .addOnCompleteListener(this) {
                        handler.postDelayed({}, 2000)

                        loadingLogin.visibility = View.GONE

                        if (it.isSuccessful){
                            customDialog.dismiss()

                            button_swap.setBackgroundResource(R.drawable.button_border)
                            button_user.setBackgroundResource(R.drawable.button_border)
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        buttonRegister.setOnClickListener {
            customDialog.dismiss()

            registerDialog()
        }
    }

    private fun registerDialog() {
        val customDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_register)
            setCancelable(true)
        }
        customDialog.show()

        val inputEmail = customDialog.findViewById<EditText>(R.id.input_email)
        val inputPassword1 = customDialog.findViewById<EditText>(R.id.input_password1)
        val inputPassword2 = customDialog.findViewById<EditText>(R.id.input_password2)
        val loadingRegister = customDialog.findViewById<ProgressBar>(R.id.loading_register)
        val buttonRegister = customDialog.findViewById<Button>(R.id.button_register)
        val buttonLogin = customDialog.findViewById<Button>(R.id.button_login)

        loadingRegister.visibility = View.GONE
        inputHighlightRemove(inputEmail)

        buttonRegister.setOnClickListener {
            val dataEmail = inputEmail.text.toString()
            val dataPassword1 = inputPassword1.text.toString()
            val dataPassword2 = inputPassword2.text.toString()
            var check = true

            if (!Patterns.EMAIL_ADDRESS.matcher(dataEmail).matches()) {
                inputEmail.error = "email not valid"
                inputEmail.requestFocus()
                check = false
            }
            if (dataPassword1.length < 6) {
                inputPassword1.error = "password must be at last 6 characters"
                if (check) {
                    inputPassword1.requestFocus()
                    check = false
                }
            }
            else if (dataPassword1 != dataPassword2) {
                inputPassword2.error = "confirm password does not match"
                if (check) {
                    inputPassword2.requestFocus()
                    check = false
                }
            }

            if (check) {
                loadingRegister.visibility = View.VISIBLE
                // mendaftarkan user
                firebaseAuth.createUserWithEmailAndPassword(dataEmail, dataPassword1)
                    .addOnCompleteListener(this) { register ->
                        handler.postDelayed({}, 2000)

                        if (register.isSuccessful){
                            firebaseAuth.signInWithEmailAndPassword(dataEmail, dataPassword1)
                                .addOnCompleteListener(this) {
                                    loadingRegister.visibility = View.GONE

                                    if (it.isSuccessful) {
                                        customDialog.dismiss()

                                        firebaseAuth.currentUser?.sendEmailVerification()
                                        firebaseAuth.signOut()

                                        loginDialog(dataEmail)
                                        Toast.makeText(this, "Register successful!\nPlease login and verify your email :)", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                        else {
                            loadingRegister.visibility = View.GONE
                            Toast.makeText(this, register.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        buttonLogin.setOnClickListener {
            customDialog.dismiss()

            loginDialog()
        }
    }

    private fun profileDialog() {
        val customDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_profile)
            setCancelable(true)
        }
        customDialog.show()

        val textEmail = customDialog.findViewById<TextView>(R.id.text_email)
        val textVerify = customDialog.findViewById<TextView>(R.id.text_verify)
        val buttonVerify = customDialog.findViewById<Button>(R.id.button_verify)
        val buttonChange = customDialog.findViewById<Button>(R.id.button_change)
        val buttonLogout = customDialog.findViewById<Button>(R.id.button_logout)
        val user = firebaseAuth.currentUser
        val userVerify = user?.isEmailVerified

        textEmail.setText("Email : ${firebaseAuth.currentUser?.email}")
        textVerify.setText("Verified : $userVerify")

        if (userVerify!!) {
            buttonVerify.visibility = View.GONE
        } else {
            buttonVerify.setOnClickListener {
                user.sendEmailVerification().addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(this, "Email verification resend successfully", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        buttonChange.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
        }

        buttonLogout.setOnClickListener {
            customDialog.dismiss()

            firebaseAuth.signOut()
            button_swap.setBackgroundResource(R.drawable.dialog_background_gray)
            button_user.setBackgroundResource(R.drawable.dialog_background_gray)
            Toast.makeText(this, "Logout successful!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inputHighlightRemove(input: EditText) {
        input.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val text = input.text.toString()
                input.setText("")
                input.setText(text)
            }
        }
    }
}