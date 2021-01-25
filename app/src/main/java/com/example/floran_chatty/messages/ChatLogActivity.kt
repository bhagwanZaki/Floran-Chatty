package com.example.floran_chatty.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.floran_chatty.NewMessageActivity
import com.example.floran_chatty.R
import com.example.floran_chatty.model.ChatMessages
import com.example.floran_chatty.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlin.math.log

class ChatLogActivity : AppCompatActivity() {
    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser:User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_chat_log)
        recyclerView.adapter = adapter
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        if (toUser != null) {
            supportActionBar?.title = toUser!!.username
        }
        val send_button = findViewById<Button>(R.id.send_button_chat_log)
//        setupDumyData()
        listenForMessage()
        send_button.setOnClickListener {
            Log.d(TAG, "ATtempt to send messagge")
            preformSendMessage()
        }


    }

    private fun listenForMessage() {
        val fromID = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromID/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMesssage = snapshot.getValue(ChatMessages::class.java)
                if (chatMesssage != null) {
                    chatMesssage?.text?.let { Log.d(TAG, it) }
                    if (chatMesssage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatToItems(chatMesssage.text))
                    } else {
                        adapter.add(ChatFromItems(chatMesssage.text))
                    }
                }

                findViewById<RecyclerView>(R.id.recycler_view_chat_log).scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    private fun preformSendMessage() {
        // Send message to firebase
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val fromID = FirebaseAuth.getInstance().uid
        val toId = user?.uid

        if (fromID == null) return
        if (toId == null) return
        val chat_box = findViewById<EditText>(R.id.edit_text_chat_log)
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromID/$toId").push()
        val toref = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromID").push()

        val chatMessage = ChatMessages(
            ref.key!!,
            chat_box.text.toString(),
            fromID,
            toId,
            System.currentTimeMillis() / 1000
        )
        //saving in database
        ref.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "save messaged in databse: ${ref.key}")
                findViewById<EditText>(R.id.edit_text_chat_log).text.clear()
                findViewById<RecyclerView>(R.id.recycler_view_chat_log).scrollToPosition(adapter.itemCount - 1)
            }
        toref.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromID/$toId")
        val latestMessagetoRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromID")
        latestMessageRef.setValue(chatMessage)
        latestMessagetoRef.setValue(chatMessage)
    }
}

class ChatFromItems(val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.message_textview_chat_from).text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}
class ChatToItems(val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.message_textview_chat_to).text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}
