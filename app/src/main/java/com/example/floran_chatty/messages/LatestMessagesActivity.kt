package com.example.floran_chatty.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.floran_chatty.NewMessageActivity
import com.example.floran_chatty.NewMessageActivity.Companion.USER_KEY
import com.example.floran_chatty.R
import com.example.floran_chatty.model.ChatMessages
import com.example.floran_chatty.model.User
import com.example.floran_chatty.registerlogin.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.util.*
import kotlin.collections.HashMap

class LatestMessagesActivity : AppCompatActivity() {
    companion object{
        var currentUser: User? = null
    }
    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        val latest_message_recyclerView = findViewById<RecyclerView>(R.id.recycler_view_latest_message)
        Is_user_logged_in()
        fetchCurrentUser()
//        setDumyRow()
        listenForLatestMsg()
        latest_message_recyclerView.adapter = adapter
        latest_message_recyclerView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->

            val intent = Intent(this, ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(USER_KEY,row.chatPartenerUser)
            startActivity(intent)
        }
    }

    val latestMessagesMap = HashMap<String, ChatMessages>()
    private fun refreshRecycelerView(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }
    private fun listenForLatestMsg() {
        val fromID = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromID")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessages::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecycelerView()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessages::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecycelerView()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

//    private fun setDumyRow() {
//
//
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//
//    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue((User::class.java))
                Log.d("LatestMessages","Current USer: ${currentUser?.username}")


            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun Is_user_logged_in(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                 startActivity(intent)
            }
            R.id.sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

     override fun onCreateOptionsMenu(menu : Menu?): Boolean{
        menuInflater.inflate(R.menu.nav_menu, menu)
        return  super.onCreateOptionsMenu(menu)

    }
}

class LatestMessageRow(val chatMessages: ChatMessages): Item<GroupieViewHolder>(){
    var chatPartenerUser: User? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.latest_msg_textview_lates_messages).text = chatMessages?.text

        val chatFromID: String
        if(chatMessages.fromId == FirebaseAuth.getInstance().uid)
        {
            chatFromID = chatMessages.toId
        } else {
            chatFromID = chatMessages.fromId
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatFromID")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartenerUser = snapshot.getValue(User::class.java)
//                user?.username
                viewHolder.itemView.findViewById<TextView>(R.id.username_textview_lates_messages).text = chatPartenerUser?.username
                val profilePic = viewHolder.itemView.findViewById<ImageView>(R.id.profile_pic_latest_messages)
                Picasso.get().load(chatPartenerUser?.profileImageUrl).into(profilePic)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

}