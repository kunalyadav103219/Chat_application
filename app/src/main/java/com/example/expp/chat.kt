package com.example.expp

import android.content.ContentValues.TAG
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatts.ChatListAdapter


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
//import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
//import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import de.hdodenhof.circleimageview.CircleImageView


class chat : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var messageListAdapter: ChatListAdapter
    private var isTyping = false
    private lateinit var messageEditText: EditText
    private lateinit var img: CircleImageView
    private lateinit var backpressedbtn:ImageButton
    //    private lateinit var voicecall: ZegoSendCallInvitationButton
//    private lateinit var videocall: ZegoSendCallInvitationButton
    private lateinit var selectedImageUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)



        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        val userRf = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser?.uid}")

        userRf.child("status").setValue("online")
        val receiverId = intent.getStringExtra("userId")
        val currentuid=auth.currentUser?.uid

//        val receiverMessageRef = database.child("chats").child(receiverId).child(currentuid).push()
//        receiverMessageRef.setValue(isViewed = false)
//
//
//            val receiverMessageRef = receiverId?.let { database.child("chats").child(it).child(senderId).push() }
//            receiverMessageRef.child("viewed").setValue(true)
            val senderToReceiverRef =
                FirebaseDatabase.getInstance().getReference("chats/$currentuid/$receiverId")

// Listen for changes in the receiver's chat with the sender
        if (senderToReceiverRef != null) {
            senderToReceiverRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (messageSnapshot in snapshot.children) {
                        // Update isViewed to true for all messages
                        messageSnapshot.child("viewed").ref.setValue(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
            else
        {
             Toast.makeText(this,"Sender Receieve Ref is null",Toast.LENGTH_LONG).show()
        }

//        val userRef = FirebaseDatabase.getInstance().getReference("chats/${auth.currentUser?.uid}")
//        userRef.child("isViewed").setValue(true)
        img = findViewById(R.id.chat_tollbarimage)
        val stsus = findViewById<TextView>(R.id.status)

//        voicecall = findViewById(R.id.new_voice_call)
//        videocall = findViewById(R.id.new_video_call)

        val backpressedbtn = findViewById<ImageButton>(R.id.backp)
        val selectedImageView = findViewById<ImageView>(R.id.selectedImageView)


      backpressedbtn.setOnClickListener {
          onBackPressed()
      }
//        attachImageButton.setOnClickListener {
//            val imagePickerIntent = Intent(Intent.ACTION_PICK)
//            imagePickerIntent.type = "image/*"
//            startActivityForResult(imagePickerIntent, IMAGE_PICKER_REQUEST)
//        }


        val usd = intent.getStringExtra("userId")
        val user = FirebaseDatabase.getInstance().getReference("users/$usd")
        user.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java)
                val typing = snapshot.child("typing").getValue(Boolean::class.java) ?: false

                // Use the status and typing values in your UI
                val statusTextView = findViewById<TextView>(R.id.status)
                if (typing) {
                    statusTextView.setText("typing...")

                } else if (status == "online") {
                    statusTextView.setText("online")
                } else {
                    statusTextView.setText("offline")
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })




        messageEditText = findViewById(R.id.messageEditText)
        val userId = intent.getStringExtra("userId")
        val username = intent.getStringExtra("username")
        val senderImage = intent.getStringExtra("imggg")
        Glide.with(this).load(senderImage).into(img)


        val toolbar: TextView = findViewById(R.id.toolbarTitleTextView)
        toolbar.setText(username)


        val messageList = mutableListOf<Message>()
        val us = mutableListOf<User>()




        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        messageListAdapter = userId?.let { ChatListAdapter(messageList, it, us, recyclerView,senderImage) }!!
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageListAdapter


        if (userId != null) {
            loadChatMessages(userId)
        }
        if (messageListAdapter.itemCount > 0) {
            // Scroll to the last message
         recyclerView.scrollToPosition(messageListAdapter.itemCount - 1)
        }
        val sendButton: Button = findViewById(R.id.sendButton)


        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString()
            val username = intent.getStringExtra("sendername")
            val senderImage = intent.getStringExtra("senderimage")
            if (messageText.isNotEmpty()) {



                if (userId != null && username != null && senderImage != null) {

                    sendMessage(userId, messageText, username, senderImage,)

                }


                messageEditText.text.clear()
            }
            else
            {
                Toast.makeText(this,"Please Enter your text",Toast.LENGTH_LONG).show()
            }
        }


        if (usd != null) {
//            setVoiceCall(usd.toString().trim())
//            setVideoCall(usd.toString().trim())
        }

    }







    private fun sendNotificationToUser(recipientId: String, messageText: String) {
        // Retrieve the recipient's FCM token (you should have this stored in your database)
        val recipientToken = "RECIPIENT_FCM_TOKEN" // Replace with the recipient's FCM token

        // Build the notification payload
        val payload = mapOf(
            "to" to recipientToken,
            "data" to mapOf(
                "title" to "New Message",
                "body" to messageText
            )
        )

        // Send the FCM notification
        FirebaseFunctions.getInstance().getHttpsCallable("sendNotification")
            .call(payload)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Notification sent successfully")
                } else {
                    Log.e(TAG, "Failed to send notification", task.exception)
                }
            }

    }

//    private fun setVoiceCall(targetUserID: String){
//                voicecall.setIsVideoCall(false)
//                voicecall.resourceID = "zego_uikit_call"
//                voicecall.setInvitees(
//                    listOf(
//                        ZegoUIKitUser(
//                            targetUserID,
//                            targetUserID
//                        )
//                    ))
//            }
//
//            private fun setVideoCall(targetUserID: String){
//                videocall.setIsVideoCall(true)
//                videocall.resourceID = "zego_uikit_call"
//                 videocall.setInvitees(
//                    listOf(
//                        ZegoUIKitUser(
//                            targetUserID,
//                            targetUserID
//                        )
//                    ))
//            }


    private fun markMessageAsSeen(ref: DatabaseReference) {
       ref.child("seen").setValue(true)
    }

    private fun loadChatMessages(receiverId: String) {
        database.child("chats")
            .child(auth.currentUser?.uid ?: "")
            .child(receiverId)
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messageList = mutableListOf<Message>()
                    for (messageSnapshot in snapshot.children) {
                        val chatMessage = messageSnapshot.getValue(Message::class.java)
                        chatMessage?.let { messageList.add(it) }


                        }
                    messageListAdapter.setMessageList(messageList)
                    // Call notifyDataSetChanged() here
                    messageListAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "failed to load text", Toast.LENGTH_LONG)
                        .show()
                }
            })
        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // If the text input is not empty, consider the user as typing
                isTyping = s?.isNotEmpty() == true

                // Update typing status
                updateTypingStatus(isTyping)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

    private fun sendMessage(
        receiverId: String,
        messageText: String,
        senderName: String,
        SenderImage: String,
        //  sendingimageuri: String
    ) {
        val senderId = auth.currentUser?.uid ?: ""
        val timestamp = System.currentTimeMillis()

       // val message = Message(senderId, receiverId, messageText, timestamp, senderName, SenderImage)
        val message = Message(messageText,  timestamp, receiverId, senderId, senderName,  SenderImage)

        val senderMessageRef = database.child("chats").child(senderId).child(receiverId).push()
        senderMessageRef.setValue(message.copy(isViewed = true)) // Sender's message is automatically viewed

        val receiverMessageRef = database.child("chats").child(receiverId).child(senderId).push()
        receiverMessageRef.setValue(message.copy(isViewed = false))


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
                            val sender =
                                messageSnapshot.child("senderId").getValue(String::class.java)
                            val reciever =
                                messageSnapshot.child("receiverId").getValue(String::class.java)
                            val text = messageSnapshot.child("text").getValue(String::class.java)
                            val timestamp =
                                messageSnapshot.child("timestamp").getValue(Long::class.java)
                            val userList = mutableListOf<User>()
                            if (reciever != null && sender != null && text != null && timestamp != null) {

                                val latestMessage = LatestMessage(reciever, sender, text, timestamp)

                            }

                        }

                    }
                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            }
        Toast.makeText(this,"$receiverId",Toast.LENGTH_LONG).show()
        Toast.makeText(this,"$auth.currentUser?.uid",Toast.LENGTH_LONG).show()




//        database.child("chats")
//            .child(senderId)
//            .child(receiverId)
//            .push()
//            .setValue(message)
//
//        database.child("chats")
//            .child(receiverId)
//            .child(senderId)
//            .push()
//            .setValue(message)
    }

    private fun updateTypingStatus(isTyping: Boolean) {
        if (isTyping) {
            val currentUserRef = FirebaseDatabase.getInstance().getReference("users/${auth.currentUser?.uid}")
            currentUserRef.child("typing").setValue(true)
        } else {
            val currentUserRef =
                FirebaseDatabase.getInstance().getReference("users/${auth.currentUser?.uid}")
            currentUserRef.child("typing").setValue(false)
        }
    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == RESULT_OK) {
//             selectedImageUri = data?.data!!
//          val  selectedImageView = findViewById<ImageView>(R.id.selectedImageView)
//            selectedImageView.visibility = View.VISIBLE
//            selectedImageView.setImageURI(selectedImageUri)
//        }
//    }
//
//    companion object {
//        private const val IMAGE_PICKER_REQUEST = 1
//    }
}






