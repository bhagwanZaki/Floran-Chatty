package com.example.floran_chatty.model

class ChatMessages(val id: String ,val text: String, val fromId: String, val toId: String, val timestamp: Long){
    constructor():this("","","","",-1)
}