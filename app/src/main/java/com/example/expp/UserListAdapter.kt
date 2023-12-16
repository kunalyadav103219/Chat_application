package com.example.expp

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class UserListAdapter(
    private var context:Context,
    private var userList: List<User>,
    private var latestMessages: Map<String, LatestMessage>,
    private val itemClickListener: ((User) -> Unit)?
) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val userimage: CircleImageView = itemView.findViewById(R.id.profile_image)
        val currentMessage: TextView = itemView.findViewById(R.id.currentMessage)
        val lastmessagetime:TextView=itemView.findViewById(R.id.lastmessage_Time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_profile, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        val userId = user.uid
        val latestMessage = latestMessages[userId]

        holder.usernameTextView.text = user.username
        Glide.with(holder.itemView.context)
            .load(user.image)  // Use the user's profile image URL here
            .into(holder.userimage)

        holder.statusTextView.text = when {
            user.typing -> "Typing..."
            user.status == "online" -> "Online"
            else -> "Offline"

        }




        if (latestMessage!=null) {
            fun convertTimestampToTime(timestamp: Long): String {
                val sdf = SimpleDateFormat("h:mm a")  // Customize the time format as needed
                val date = Date(timestamp)
                return sdf.format(date)
            }
            val timestamp = latestMessage.timestamp // Replace with your message's timestamp
            val formattedTime = convertTimestampToTime(timestamp)
            holder.lastmessagetime.text=formattedTime

            val auth:FirebaseAuth= FirebaseAuth.getInstance()


            if (auth.currentUser?.uid ==latestMessage.sender) {
                holder.currentMessage.text = "You: ${latestMessage.text}"
            }
            else
            {

                    holder.currentMessage.text = "${latestMessage.text}"
                }



        } else {
            holder.currentMessage.text = "No messages"
        }
        holder.itemView.setOnClickListener {
            itemClickListener?.let { it1 -> it1(user) }
        }
    }


    override fun getItemCount(): Int {
        return userList.size
    }

    fun setUserList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }

    fun setUsers(users: List<User>) {
        userList = users
        notifyDataSetChanged()
    }

    fun updateLatestMessages(latestMessages: Map<String, LatestMessage>) {
        this.latestMessages = latestMessages
        notifyDataSetChanged()
    }
//    fun updateLatestMessages(latestMessages: Map<String, Message>) {
//        userList.forEach { user ->
//            val latestMessage = latestMessages[user.uid]
//            if (latestMessage != null)
//            {
//                user.latestMessage = latestMessage
//            }
//        }
//        notifyDataSetChanged()


}



