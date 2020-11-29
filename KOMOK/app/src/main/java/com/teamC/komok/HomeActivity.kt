package com.teamC.komok

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.teamC.komok.utils.ImageUtils
import com.teamC.komok.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {
    private val permissionUtils = PermissionUtils()
    private val imageUtils = ImageUtils()
    private lateinit var handler: Handler
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        const val PERMISSION_CODE1 = 100
        const val PERMISSION_CODE2 = 101
        const val PERMISSION_CODE3 = 102
        const val CAMERA_CODE1 = 103
        const val CAMERA_CODE2 = 104
        const val CAMERA_CODE3 = 105
        const val GALLERY_CODE1 = 106
        const val GALLERY_CODE2 = 107
        const val GALLERY_CODE3 = 108
        const val DELAY: Long = 1500
    }

    override fun onStart() {
        super.onStart()
        // user sudah login
        if (firebaseAuth.currentUser != null){
            firebaseAuth.currentUser?.reload()
            toggleButton(true)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // untuk jeda
        handler = Handler()
        // untuk login akun
        firebaseAuth = FirebaseAuth.getInstance()

        // tombol akun
        button_user.setOnClickListener {
            // user sudah login
            if (firebaseAuth.currentUser != null) {
                profileDialog()
            }
            // user belum login
            else {
                loginDialog()
            }
        }

        // tombol swap face
        button_swap.setOnClickListener {
            // cek izin akses
            if (permissionUtils.requestPermission(
                    this,
                    PERMISSION_CODE1,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )) {
                imageUtils.getImageDialog(this, CAMERA_CODE1, GALLERY_CODE1)
            }
        }

        // tombol crop face
        button_crop.setOnClickListener {
            // cek izin akses
            if (permissionUtils.requestPermission(
                    this,
                    PERMISSION_CODE2,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )) {
                imageUtils.getImageDialog(this, CAMERA_CODE2, GALLERY_CODE2)
            }
        }

        // tombol mix face
        button_mix.setOnClickListener {
            // user sudah login
            if (firebaseAuth.currentUser != null) {
                // cek izin akses
                if (permissionUtils.requestPermission(
                        this,
                        PERMISSION_CODE3,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    imageUtils.getImageDialog(this, CAMERA_CODE3, GALLERY_CODE3)
                }
            }
            // user belum login
            else {
                Toast.makeText(this, "You need to login first", Toast.LENGTH_SHORT).show()

                loginDialog()
            }
        }

        // tombol about
        button_about.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }

    // setelah izin akses pertama kali
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // cek izin akses (swap)
        if (permissionUtils.permissionGranted(requestCode, PERMISSION_CODE1, grantResults)) {
            imageUtils.getImageDialog(this, CAMERA_CODE1, GALLERY_CODE1)
        }
        // cek izin akses (crop)
        else if (permissionUtils.permissionGranted(requestCode, PERMISSION_CODE2, grantResults)) {
            imageUtils.getImageDialog(this, CAMERA_CODE2, GALLERY_CODE2)
        }
        // cek izin akses (mix)
        else if (permissionUtils.permissionGranted(requestCode, PERMISSION_CODE3, grantResults)) {
            imageUtils.getImageDialog(this, CAMERA_CODE3, GALLERY_CODE3)
        }
    }

    // setelah mendapatkan suatu gambar
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_CODE1 -> myStartActivity(SwapActivity::class.java, imageUtils.cameraUri.toString())
                CAMERA_CODE2 -> myStartActivity(CropActivity::class.java, imageUtils.cameraUri.toString())
                CAMERA_CODE3 -> myStartActivity(MixActivity::class.java, imageUtils.cameraUri.toString())

                GALLERY_CODE1 -> myStartActivity(SwapActivity::class.java, data?.dataString)
                GALLERY_CODE2 -> myStartActivity(CropActivity::class.java, data?.dataString)
                GALLERY_CODE3 -> myStartActivity(MixActivity::class.java, data?.dataString)
            }
        } else {
            when (requestCode) {
                CAMERA_CODE1, CAMERA_CODE2, CAMERA_CODE3 -> imageUtils.cancelCamera(this)
            }
        }
    }
    private fun <T> myStartActivity(activity: Class<T>, data: String?) {
        val intent = Intent(this, activity)
        intent.putExtra("imageUpload", data)
        startActivity(intent)
    }

    private fun loginDialog(email: String = "") {
        val customDialog = Dialog(this).apply {
            setTitle(null)
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
            customDialog.dismiss()

            resetDialog()
        }

        buttonLogin.setOnClickListener {
            val dataEmail = inputEmail.text.toString()
            val dataPassword = inputPassword.text.toString()
            var check = true

            // email tidak sesuai
            if (!Patterns.EMAIL_ADDRESS.matcher(dataEmail).matches()) {
                inputEmail.error = "email not valid"
                inputEmail.requestFocus()
                check = false
            }
            // password kurang dari 6 karakter
            if (dataPassword.length < 6) {
                inputPassword.setError("password must be at least 6 characters", null)
                if (check) {
                    inputPassword.requestFocus()
                    check = false
                }
            }

            // email dan password sesuai
            if (check) {
                loadingLogin.visibility = View.VISIBLE
                // login user
                firebaseAuth.signInWithEmailAndPassword(dataEmail, dataPassword)
                    .addOnCompleteListener(this) { login ->
                        handler.postDelayed({}, DELAY)
                        loadingLogin.visibility = View.GONE

                        // login user berhasil
                        if (login.isSuccessful){
                            customDialog.dismiss()

                            toggleButton(true)
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        }
                        // login user gagal
                        else {
                            Toast.makeText(this, login.exception?.message, Toast.LENGTH_SHORT).show()
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
            setTitle(null)
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

            // email tidak sesuai
            if (!Patterns.EMAIL_ADDRESS.matcher(dataEmail).matches()) {
                inputEmail.error = "email not valid"
                inputEmail.requestFocus()
                check = false
            }
            // password kurang dari 6 karakter
            if (dataPassword1.length < 6) {
                inputPassword1.setError("password must be at least 6 characters", null)
                if (check) {
                    inputPassword1.requestFocus()
                    check = false
                }
            }
            // konfirmasi password tidak sesuai
            else if (dataPassword1 != dataPassword2) {
                inputPassword2.setError("confirm password does not match", null)
                if (check) {
                    inputPassword2.requestFocus()
                    check = false
                }
            }

            // email dan password sesuai
            if (check) {
                loadingRegister.visibility = View.VISIBLE
                // mendaftarkan user
                firebaseAuth.createUserWithEmailAndPassword(dataEmail, dataPassword1)
                    .addOnCompleteListener(this) { register ->
                        handler.postDelayed({}, DELAY)
                        loadingRegister.visibility = View.GONE

                        // mendaftarkan user berhasil
                        if (register.isSuccessful){
                            loadingRegister.visibility = View.VISIBLE
                            // login user (mengirimkan email verifikasi)
                            firebaseAuth.signInWithEmailAndPassword(dataEmail, dataPassword1)
                                .addOnCompleteListener(this) { login ->
                                    handler.postDelayed({}, DELAY)
                                    loadingRegister.visibility = View.GONE

                                    // login user berhasil
                                    if (login.isSuccessful) {
                                        customDialog.dismiss()

                                        firebaseAuth.currentUser?.sendEmailVerification()
                                        firebaseAuth.signOut()

                                        loginDialog(dataEmail)
                                        Toast.makeText(
                                            this,
                                            "Register successful!\nPlease verify your email first then login",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    // login user gagal
                                    else {
                                        loginDialog(dataEmail)
                                        Toast.makeText(
                                            this,
                                            "Register successful!\nPlease login and resend email verification",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        }
                        // mendaftarkan user gagal
                        else {
                            Toast.makeText(
                                this,
                                register.exception?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        buttonLogin.setOnClickListener {
            customDialog.dismiss()

            loginDialog()
        }
    }

    private fun resetDialog() {
        val customDialog = Dialog(this).apply {
            setTitle((null))
            setContentView(R.layout.dialog_reset_password)
            setCancelable(true)
        }
        customDialog.show()

        val inputEmail = customDialog.findViewById<EditText>(R.id.input_email)
        val loadingReset = customDialog.findViewById<ProgressBar>(R.id.loading_reset)
        val buttonReset = customDialog.findViewById<Button>(R.id.button_reset)
        val buttonLogin = customDialog.findViewById<Button>(R.id.button_login)

        loadingReset.visibility = View.GONE

        buttonReset.setOnClickListener {
            val dataEmail = inputEmail.text.toString()

            // email tidak sesuai
            if (!Patterns.EMAIL_ADDRESS.matcher(dataEmail).matches()) {
                inputEmail.error = "email not valid"
                inputEmail.requestFocus()
            }
            // email sesuai
            else {
                loadingReset.visibility = View.VISIBLE

                // mengirim email reset password
                firebaseAuth.sendPasswordResetEmail(dataEmail)
                    .addOnCompleteListener { reset ->
                        handler.postDelayed({}, DELAY)
                        loadingReset.visibility = View.GONE

                        // mengirim email reset berhasil
                        if (reset.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Email reset password send successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        // mengirim email reset gagal
                        else {
                            Toast.makeText(
                                this,
                                "${reset.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        buttonLogin.setOnClickListener {
            customDialog.dismiss()

            loginDialog()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun profileDialog() {
        val customDialog = Dialog(this).apply {
            setTitle(null)
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

        textEmail.text = "Email : ${firebaseAuth.currentUser?.email}"
        textVerify.text = "Verified : $userVerify"

        // user sudah verify
        if (userVerify!!) {
            buttonVerify.visibility = View.GONE
        }
        // user belum verify
        else {
            buttonVerify.setOnClickListener {
                // mengirim email verifikasi
                user.sendEmailVerification().addOnCompleteListener { verify->
                    // mengirim email verifikasi berhasil
                    if (verify.isSuccessful){
                        Toast.makeText(
                            this,
                            "Email verification resend successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // mengirim email verifikasi gagal
                    else{
                        Toast.makeText(
                            this,
                            "${verify.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        buttonChange.setOnClickListener {
            customDialog.dismiss()

            changePassDialog()
        }

        buttonLogout.setOnClickListener {
            logoutDialog(customDialog)
        }
    }

    private fun changePassDialog() {
        val customDialog = Dialog(this).apply {
            setTitle(null)
            setContentView(R.layout.dialog_change_password)
            setCancelable(true)
        }
        customDialog.show()

        val inputOldPass = customDialog.findViewById<EditText>(R.id.input_password0)
        val inputNewPass1 = customDialog.findViewById<EditText>(R.id.input_password1)
        val inputNewPass2 = customDialog.findViewById<EditText>(R.id.input_password2)
        val loadingChange = customDialog.findViewById<ProgressBar>(R.id.loading_change)
        val buttonChange = customDialog.findViewById<Button>(R.id.button_change)
        val buttonProfile = customDialog.findViewById<Button>(R.id.button_profile)
        val user = firebaseAuth.currentUser

        loadingChange.visibility = View.GONE

        buttonChange.setOnClickListener {
            val dataOldPass = inputOldPass.text.toString()
            val dataNewPass1 = inputNewPass1.text.toString()
            val dataNewPass2 = inputNewPass2.text.toString()
            var check = true

            user?.let { user ->
                // cek password lama user
                val userCredential = EmailAuthProvider.getCredential(user.email!!, dataOldPass)
                user.reauthenticate(userCredential).addOnCompleteListener { authenticate ->
                    // password lama user sesuai
                    if (authenticate.isSuccessful){
                        // password baru kurang dari 6 karakter
                        if (dataNewPass1.length < 6) {
                            inputNewPass1.setError("new password must be at least 6 characters", null)
                            inputNewPass1.requestFocus()
                            check = false
                        }
                        // konfirmasi password baru tidak sesuai
                        else if (dataNewPass1 != dataNewPass2) {
                            inputNewPass2.setError("confirm new password does not match", null)
                            inputNewPass2.requestFocus()
                            check = false
                        }

                        // password baru sesuai
                        if (check) {
                            loadingChange.visibility = View.VISIBLE
                            // update password user
                            user.updatePassword(dataNewPass1).addOnCompleteListener { update ->
                                handler.postDelayed({}, DELAY)
                                loadingChange.visibility = View.GONE

                                // update password berhasil
                                if (update.isSuccessful) {
                                    customDialog.dismiss()

                                    profileDialog()
                                    Toast.makeText(
                                        this,
                                        "Password changed successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                // update password gagal
                                else {
                                    Toast.makeText(
                                        this,
                                        "${update.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                    // password lama tidak sesuai
                    else if (authenticate.exception is FirebaseAuthInvalidCredentialsException){
                        inputOldPass.error = "old password does not match"
                        inputOldPass.requestFocus()
                    }
                    // error
                    else{
                        Toast.makeText(
                            this,
                            "${authenticate.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        buttonProfile.setOnClickListener {
            customDialog.dismiss()

            profileDialog()
        }
    }

    private fun logoutDialog(customDialog: Dialog) {
        // setup the alert builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Logout Account")
        builder.setMessage("Are you sure you want to logout?")
        // add the buttons
        builder.setPositiveButton("Yes") { _, _ ->
            customDialog.dismiss()

            firebaseAuth.signOut()
            toggleButton(false)
            Toast.makeText(this, "Logout successful!", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No", null)
        // create and show the alert dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun toggleButton(bool: Boolean) {
        val mode = if (bool) R.drawable.button_border else R.drawable.dialog_background_gray

        button_mix.setBackgroundResource(mode)
        button_user.setBackgroundResource(mode)
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