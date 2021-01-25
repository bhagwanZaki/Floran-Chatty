package com.example.floran_chatty.registerlogin


import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.floran_chatty.R
import com.example.floran_chatty.messages.LatestMessagesActivity
import com.example.floran_chatty.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val register_button = findViewById<Button>(R.id.register_button_register)
        val already_account = findViewById<TextView>(R.id.already_account_textview)
        val photo_button = findViewById<ImageView>(R.id.profile_pic)


        // onclick selections
        register_button.setOnClickListener{
            userRegistration()
        }

        already_account.setOnClickListener {
            Log.d("RegisterActivity","Already have an account")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        photo_button.setOnClickListener{
            Log.d("RegisterActivity","try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("RegisterActivity","Photo was selected")
            selectedPhotoUri = data.data
//            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            findViewById<ImageView>(R.id.profile_pic).setImageURI(selectedPhotoUri)
            findViewById<CircleImageView>(R.id.select_photo_view_register).setImageURI(selectedPhotoUri)
            findViewById<ImageView>(R.id.profile_pic).alpha = 0f
        }
    }

    // User Registration function
    private fun userRegistration(){
        val email = findViewById<EditText>(R.id.email_edittext_register).text.toString()
        val password = findViewById<EditText>(R.id.password_edittext_register).text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,"Please Enter email and password",Toast.LENGTH_SHORT).show()
        }
        Log.d("RegisterActivity","Email is : "+email)
        Log.d("RegisterActivity","Password is : "+password)

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) return@addOnCompleteListener
                    Log.d("RegisterActivity","Successfully created with uid: ${it.result?.user?.uid}")
                    uploadImagetofirebase()

                }
                .addOnFailureListener{
                    Log.d("RegisterActivity","Failed to create user: ${it.message}")
                    Toast.makeText(this,"${it.message}",Toast.LENGTH_SHORT).show()
                }
    }

    private fun uploadImagetofirebase(){
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Register","image in bucket ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
//                    it.toString()
                    Log.d("RegisterActivity","location : $it")
                    saveUsertodatabase(it.toString())


                }
            }
            .addOnFailureListener{
                Log.d("Register","Failed to upload image in bucket")
            }
    }

    private fun saveUsertodatabase(profileImageUrl : String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, findViewById<EditText>(R.id.usernmae_edittext_register).text.toString(),profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Register","saved databese")
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener{
                Log.d("Register","Failed to save user in database")
            }


    }
}

