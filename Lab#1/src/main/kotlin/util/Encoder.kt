package util

import com.google.gson.Gson
import kotlinx.coroutines.experimental.channels.Channel
import protocol.Message

fun Message.encode() = Gson().toJson(this)
fun String.decode() = Gson().fromJson(this, Message::class.java)
suspend fun Channel<Message>.encode() : String{
    val list = mutableListOf<Message>()
    println("encoding channel..."+this.toString())
    if (!this.isEmpty) {
        this.close()
        while (!this.isEmpty){
            val msg = this.receive()
            println("IN CHANNEL " + msg.encode() + "isEmpty = " + this.isEmpty)
            list.add(msg)
        }
    }
    println("channel encoded...")
    return Gson().toJson(list)
}