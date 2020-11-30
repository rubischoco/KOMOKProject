package com.teamC.komok

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    companion object {
        const val EMAIL_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        button_back.setOnClickListener {
            finish()
        }

        button_email.setOnClickListener {
            sendEmail()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EMAIL_CODE) {
            Toast.makeText(this, "Thank you for your feedback! :D", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail() {
        val to = "KOMOK.app@gmail.com"
        val subject = "Feedback/Suggestion for KOMOK"
        val emailIntent = Intent(Intent.ACTION_VIEW)
        emailIntent.data = Uri.parse("mailto:?to=$to&subject=$subject")
        startActivityForResult(Intent.createChooser(emailIntent, "Send email"), EMAIL_CODE)
    }
}