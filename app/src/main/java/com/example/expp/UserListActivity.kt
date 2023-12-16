package com.example.expp

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class UserListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var userListAdapter: UserListAdapter
    private lateinit var photoo: String
    private lateinit var pass: String
    private lateinit var username: String
    private var isTyping = false
    private var uidd:String=""





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        //val database = FirebaseDatabase.getInstance()


        auth = FirebaseAuth.getInstance()
        var userRef = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser?.uid}")
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // FCM token retrieved successfully
                    val token = task.result
                    Log.d(ContentValues.TAG, "Firebase registration token: $token")
                    //Toast.makeText(this, "$token", Toast.LENGTH_LONG).show()


                    // Now you can send this token to your server for targeting this device
                    sendTokenToServer(token)
                } else {
                    Log.e(ContentValues.TAG, "Failed to get Firebase registration token")
                    // Error occurred while retrieving the FCM token
                    val exception = task.exception
                    if (exception != null) {
                        // Handle the error (e.g., log it, display an error message, etc.)
                        Log.e(ContentValues.TAG, "FCM token retrieval failed: ${exception.message}")
                    }
                }
            }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        userRef.child("status").setValue("online")
//        if (key!=null) {
//            Toast.makeText(this, key, Toast.LENGTH_LONG).show()
//        }
//        else
//        {
//            Toast.makeText(this,"Not retrived", Toast.LENGTH_LONG).show()
//        }






//    private fun keyy() {
//
//        val latestMessagesReference = FirebaseDatabase.getInstance().getReference("latestMessages")
//
//        latestMessagesReference.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val users = mutableListOf<User>()
//                for (userSnapshot in snapshot.children) {
//                    val key = userSnapshot.key.toString() // This is the user's UID
//                    val text = userSnapshot.child("text").getValue(String::class.java)
//                    val timestamp = userSnapshot.child("timestamp").getValue(Long::class.java)
//                }
//                userListAdapter.setUserList(users)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle error
//            }
//        })


        val userList = mutableListOf<User>()
        val latestMessages = mutableMapOf<String, LatestMessage>()


        userListAdapter = UserListAdapter(this, userList, latestMessages) { user ->
            startChatActivity(user)

        }
        val recyclerView: RecyclerView = findViewById(R.id.userRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.adapter = userListAdapter


        loadUserProfiles()

    }
    fun sendTokenToServer(token: String) {
        val serverUrl = "https://example.com/api/store-token"

        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("token", token) // Send the FCM token as a parameter to your server
            // Add any other parameters or headers as needed
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            // Add any headers or additional configuration to the request as needed
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Token successfully sent to your server
                    // You can handle the server's response if needed
                    val responseBody = response.body?.string()
                    // Log or process the response data as necessary
                } else {
                    // Request was not successful
                    // Handle the server error
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Request failed (e.g., no network connectivity)
                // Handle the failure and provide appropriate feedback to the user
            }
        })
    }


    override fun onResume() {
        super.onResume()
        val userRef = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser?.uid}")
        userRef.child("status").setValue("online")
        // Update other necessary data in your user node
    }

    override fun onPause() {
        super.onPause()
        val userRef = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser?.uid}")
        userRef.child("status").setValue("offline")
        // Update other necessary data in your user node
    }

    override fun onDestroy() {
        super.onDestroy()
        val userRef = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser?.uid}")
        userRef.child("status").setValue("offline")
    }


    private fun loadUserProfiles() {
        val userRef = FirebaseDatabase.getInstance().getReference("users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    uidd = userSnapshot.child("uid").getValue(String::class.java).toString()
                    val username = userSnapshot.child("username").getValue(String::class.java)
                    val email = userSnapshot.child("email").getValue(String::class.java)
                    val photo = userSnapshot.child("image").getValue(String::class.java)
                    val statuss = userSnapshot.child("status").getValue(String::class.java)
                    val typing = userSnapshot.child("typing").getValue(Boolean::class.java) ?: false
                    val latest = userSnapshot.child("latestMessage").getValue(String::class.java)



                    if (uidd != auth.currentUser?.uid && uidd != null && username != null && email != null && photo != null && statuss!=null ) {
                        val user = User(uidd, username, email, photo, statuss,typing,"","","")
                        users.add(user)
                        auth.currentUser?.uid?.let { loadRecentChatText(it, uidd) }
                    }
                }
                userListAdapter.setUserList(users)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        val currentUserRef =
            FirebaseDatabase.getInstance().getReference("users/${auth.currentUser?.uid}")
        currentUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                username = snapshot.child("username").getValue(String::class.java).toString()
                photoo = snapshot.child("image").getValue(String::class.java).toString()
                pass = snapshot.child("password").getValue(String::class.java).toString()


                // Load and set user's image in the toolbar
                val profileImage = findViewById<ImageView>(R.id.tollbarimage)
                if (username != null && photoo != null && pass != null) {
                    Glide.with(applicationContext)
                        .load(photoo)
                        .into(profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

    }

    private fun loadRecentChatText(senderId:String, receiverId:String) {

        val chatReference = senderId?.let {
            FirebaseDatabase.getInstance().getReference("chats")
                .child(it) // Go to the sender's node
                .child(receiverId)
                .limitToLast(1)
        }

        if (chatReference != null) {
            chatReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(chatSnapshot: DataSnapshot) {
                    for (messageSnapshot in chatSnapshot.children) {
                        val sender = messageSnapshot.child("senderId").getValue(String::class.java)
                        val reciever = messageSnapshot.child("receiverId").getValue(String::class.java)
                        val text = messageSnapshot.child("text").getValue(String::class.java)
                        val timestamp = messageSnapshot.child("timestamp").getValue(Long::class.java)
                        val userList = mutableListOf<User>()
                        if ( reciever!=null && sender!=null && text!=null && timestamp!=null) {
                            insertLatestMessage(  reciever,sender, text, timestamp)
                          }
                        }

                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun retrieveLatestMessages(userId: String) {
        val latestMessagesReference = FirebaseDatabase.getInstance().getReference("latestMessages")

        latestMessagesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latestMessages = mutableMapOf<String, LatestMessage>()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.child("ID").getValue(String::class.java)
                    val senderr = userSnapshot.child("sender").getValue(String::class.java)
                    val text = userSnapshot.child("text").getValue(String::class.java)
                    val timestamp = userSnapshot.child("timestamp").getValue(Long::class.java)
//                    Toast.makeText(applicationContext,"$text",Toast.LENGTH_LONG).show()
                    if (userId != null && text != null && timestamp != null&& senderr!=null) {
                        val latestMessage = LatestMessage(userId,senderr,text, timestamp)



                        latestMessages[userId] = latestMessage
                    }
                }

                // Update the adapter with the latest messages
                userListAdapter.updateLatestMessages(latestMessages)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun insertLatestMessage(receiverId:String,sender:String, text: String?, timestamp: Long?) {
        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("latestMessages").child(receiverId)

        val latestMessageData = hashMapOf(
            "ID" to receiverId,
            "sender" to sender,
            "text" to text,
            "timestamp" to timestamp
        )
        retrieveLatestMessages(receiverId)
        latestMessageRef.setValue(latestMessageData)
            .addOnSuccessListener {
//                println("Latest message inserted successfully for user $userId")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,"$e",Toast.LENGTH_LONG).show()
            }

    }


    private fun startChatActivity(user: User) {
        val intent = Intent(this, chat::class.java)
        intent.putExtra("userId", user.uid)
        intent.putExtra("username", user.username)
        intent.putExtra("imggg",user.image)
        intent.putExtra("senderimage",photoo)
        intent.putExtra("sendername",username)

        startActivity(intent)



    }

    fun showOptionsMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_update_image -> {
                    // Handle "Update Image" menu item click
                    // Open an image picker or other UI for updating the image
                    val intent = Intent(applicationContext, updatee::class.java)
                    intent.putExtra("imgurl", photoo)
                    intent.putExtra("username", username)
                    intent.putExtra("password", pass)
                    startActivity(intent)
                    true
                }

                R.id.action_logout -> {
                    val authManager = AuthManager(this)
                    authManager.setLoggedIn(false)
                    val userRef = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser?.uid}")
                    userRef.child("status").setValue("offline")

                    // Handle "Logout" menu item click
                    auth.signOut()
                    startActivity(Intent(this, loginpage::class.java))
                    finish()

                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

}
