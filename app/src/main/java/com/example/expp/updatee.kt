package com.example.expp

import android.app.Activity;
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView


class updatee : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    private lateinit var newPasswordEditText:EditText
    private lateinit var updatePasswordButton:Button
    private lateinit var changeImageButton:CircleImageView
    private lateinit var profileImageView:CircleImageView
    private var imageUri:Uri?=null
    private lateinit var name:EditText
    private lateinit var c:String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_updatee)


        newPasswordEditText=findViewById(R.id.newPasswordEditText)
        updatePasswordButton=findViewById(R.id.updatePasswordButton)
        changeImageButton=findViewById(R.id.changeImageButton)
        profileImageView=findViewById(R.id.imgg)
        name=findViewById(R.id.upname)

        val imgurl= intent.getStringExtra("imgurl")
        val username= intent.getStringExtra("username")
        val password= intent.getStringExtra("password")


        Glide.with(this).load(imgurl).into(profileImageView)
        name.setText(username)
        newPasswordEditText.setText(password)

        updatePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()
            if (newPassword.isNotEmpty() && !(newPassword.length<6)) {
                updateUserPassword(newPassword)
                val user = auth.currentUser
                val userId = user?.uid ?: ""
                val userDatabaseRef = FirebaseDatabase.getInstance().getReference("users/${user?.uid}")
                userDatabaseRef.child("username").setValue(name.text.toString())
                userDatabaseRef.child("password").setValue(newPassword)
                    .addOnSuccessListener {
                        // Username updated successfully
                        showToast("Username updated successfully")
                        onBackPressed()
                    }
                    .addOnFailureListener {
                        // Error occurred while updating username
                        showToast("Failed to update username")
                    }
            }
            else
            {
                Toast.makeText(this,"Name should present in box and password should six digit",Toast.LENGTH_LONG).show()
            }
            imageUri.let {
                if (it != null) {
                    updateUserProfileImage(it)
                }


            }

        }

        changeImageButton.setOnClickListener {
            openImagePicker()
        }
    }

    private fun updateUserPassword(newPassword: String) {
        val user = auth.currentUser
        user?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Password updated successfully")
                } else {
                    showToast("Password update failed")
                }
            }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            imageUri = data?.data!!
            Glide.with(this).load(imageUri).into(profileImageView)

        }
    }

    private fun updateUserProfileImage(imageUri: Uri) {
        val user = auth.currentUser
        val userId = user?.uid ?: ""
        val profileImageRef = storageRef.child("profile_images/$userId.jpg")

        profileImageRef.putFile(imageUri)
            .addOnSuccessListener {
                profileImageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    val newImageUrl = imageUrl.toString() // Convert URL to string

                    val imgurl= intent.getStringExtra("imgurl")
                    if(newImageUrl==null)
                    {
                        if (imgurl != null) {
                            c=imgurl
                        }
                    }
                    else
                    {
                        c=newImageUrl
                    }


                    // Update profile image URL in Firebase Realtime Database
                    val userDatabaseRef = FirebaseDatabase.getInstance().getReference("users/${user?.uid}")
                    userDatabaseRef.child("image").setValue(c)
                        .addOnSuccessListener {
                            showToast("Profile image URL updated in database")

                            // Update the user's display name (optional)
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(user?.displayName) // Maintain current display name
                                .setPhotoUri(imageUrl)
                                .build()

                            user?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        showToast("Profile image updated successfully")

                                    } else {
                                        showToast("Profile image update failed")
                                    }
                                }
                        }
                        .addOnFailureListener {
                            showToast("Failed to update profile image URL in database")
                        }
                }
            }
            .addOnFailureListener {
                showToast("Failed to upload image")
            }
    }


    private fun showToast(message: String) {
        // Show a toast message
    }

    companion object {
        private const val IMAGE_PICK_REQUEST_CODE = 1
    }
}
