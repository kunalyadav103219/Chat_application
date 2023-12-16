package com.example.expp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class signup : AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var pas: EditText
    private lateinit var cnfpas: EditText
    private lateinit var signbtn: Button
    private lateinit var logih: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var Circularim: CircleImageView
    private lateinit var imgupload: CircleImageView
    private lateinit var imageUri: Uri
    private val storageRef = FirebaseStorage.getInstance().reference
    private lateinit var progressbar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        auth = FirebaseAuth.getInstance()
        name = findViewById(R.id.name)
        email = findViewById(R.id.gmail)
        pas = findViewById(R.id.pass)
        cnfpas = findViewById(R.id.cnfpass)
        signbtn = findViewById(R.id.signup)
        logih = findViewById(R.id.lbtn)
        Circularim = findViewById(R.id.imgg)
        imgupload = findViewById(R.id.upload)
        database = FirebaseDatabase.getInstance().reference
        progressbar=findViewById(R.id.progressBar1)

        imgupload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        logih.setOnClickListener {
            val intent = Intent(this, loginpage::class.java)
            startActivity(intent)
        }



        signbtn.setOnClickListener {

            try {
                if (validation()) {
                    if (imageUri != null) {
                        val em = email.text.toString()
                        val pas = pas.text.toString()
                        progressbar.visibility=View.VISIBLE
                        auth.createUserWithEmailAndPassword(em, pas)
                            .addOnCompleteListener { registrationTask ->
                                if (registrationTask.isSuccessful) {
                                    val user = auth.currentUser

                                    user?.sendEmailVerification()
                                        ?.addOnCompleteListener { verificationTask ->
                                            if (verificationTask.isSuccessful) {
                                                val user = auth.currentUser
                                                val uid = user?.uid ?: ""
                                                val username = name.text.toString()

                                                // Upload image to Firebase Storage
                                                val imageRef =
                                                    storageRef.child("profileImages/$uid.jpg")
                                                imageUri?.let { uri ->
                                                    imageRef.putFile(uri)
                                                        .addOnSuccessListener {
                                                            imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                                                                val newUser = User(uid,
                                                                    username,
                                                                    email.text.toString(),
                                                                    imageUrl.toString(),"",false,
                                                                    pas,"","")

                                                                // Store the user in the "users" collection in Firebase
                                                                database.child("users").child(uid)
                                                                    .setValue(newUser)
                                                                showToast("Verification email sent. Please check your email.")
                                                                progressbar.visibility=View.GONE
                                                                redirectToLoginPage()
                                                            }
                                                        }
                                                        .addOnFailureListener {
                                                            showToast("Failed to upload image.")
                                                            progressbar.visibility=View.GONE
                                                        }


                                                }

                                            } else {
                                                showToast("Failed to send verification email.")
                                                progressbar.visibility=View.GONE
                                            }
                                        }

                                }


                            }

                    }

                }


            }  catch (e:Exception)
            {
                showToast("Upload your image")
            }

        }


    }
    private fun redirectToLoginPage() {
        val intent=Intent(this, loginpage::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(s: String) {
        Toast.makeText(this,s,Toast.LENGTH_LONG).show()

    }

    private fun validation():Boolean {
        if (name.length()==0)
        {
            name.error="Enter your name"
            name.requestFocus()
            return false
        }
        val e=email.text.toString()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            email.error = "Invalid username"
            email.requestFocus()
            return false
        }
        if (pas.length()==0)
        {
            pas.error="Enter password"
            pas.requestFocus()
            return false
        }
        if (pas.length()<6)
        {
            pas.error="Enter 6 digit password"
            pas.requestFocus()
            return false
        }
        if (pas.text.toString()!=cnfpas.text.toString())
        {
            cnfpas.error="password and confirm is not match"
            cnfpas.requestFocus()
            return false
        }

        return true
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            imageUri = data?.data!!
            Glide.with(this).load(imageUri).into(Circularim)
            // Now you have the image URI, you can use it as needed
//            uploadImageAndSaveToDatabase(imageUri)
        }

    }


}
