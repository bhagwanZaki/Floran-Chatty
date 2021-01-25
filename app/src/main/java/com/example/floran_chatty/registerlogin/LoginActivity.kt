package com.example.floran_chatty.registerlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.floran_chatty.R
import com.example.floran_chatty.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val create_new_account = findViewById<TextView>(R.id.create_an_account)
        val login_button = findViewById<Button>(R.id.login_button_login)

        login_button.setOnClickListener{
            val email = findViewById<EditText>(R.id.email_edittexr_login).text.toString()
            val password = findViewById<EditText>(R.id.password_edittext_login).text.toString()

            Log.d("LoginActivity","Email is : $email")
            Log.d("LoginActivity","Password is : $password")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{
                    if(!it.isSuccessful) return@addOnCompleteListener
                    Log.d("LoginActivity","Successfully login with uid: ${it.result?.user?.uid}")
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Log.d("LoginActivity","Failed to create user: ${it.message}")
                    Toast.makeText(this,"${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        create_new_account.setOnClickListener {
            Log.d("LoginActivity","Register page open")
            finish()
        }
    }
}