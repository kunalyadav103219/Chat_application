package com.example.chatts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.expp.Message
import com.example.expp.R
import com.example.expp.User
import java.text.SimpleDateFormat
import java.util.Date


class ChatListAdapter(
    private val messageList: MutableList<Message>,
    private val currentUserUid: String,
    private val user: MutableList<User>,
    private val recyclerView: RecyclerView,
   private val senderImage: String?

) :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]

        return if (message.senderId == currentUserUid) {
            R.layout.rc // Sender layout
        } else {
            R.layout.item_message // Receiver layout
        }
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
         val viewStatusImageView: ImageView = itemView.findViewById(R.id.viewStatusImageView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutResId = viewType // Use viewType directly

        val view = LayoutInflater.from(parent.context)
            .inflate(layoutResId, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]
        holder.messageTextView.text = message.text

        fun convertTimestampToTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("h:mm a")  // Customize the time format as needed
            val date = Date(timestamp)
            return sdf.format(date)
        }
        val timestamp = message.timestamp // Replace with your message's timestamp
        val formattedTime = convertTimestampToTime(timestamp)
//        textViewMessageTime.text = formattedTime

        if(message.isViewed)
        {
            holder.viewStatusImageView.setImageResource(R.drawable.baseline_remove_red_eye_24)
        }
        else
        {
            Glide.with(holder.itemView.context)
                .load(senderImage)  // Use the user's profile image URL here
                .into(holder.viewStatusImageView)
        }

    }
    override fun getItemCount(): Int {
        return messageList.size
    }

    fun setMessageList(messages: List<Message>) {
        messageList.clear()
        messageList.addAll(messages)
        notifyDataSetChanged()
        scrollToLastItem()

    }

    private fun scrollToLastItem() {
        recyclerView.post {
            recyclerView.scrollToPosition(itemCount - 1)
        }

    }


}
