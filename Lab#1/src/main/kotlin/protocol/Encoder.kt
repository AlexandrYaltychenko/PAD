package protocol

import com.google.gson.Gson

object Encoder {
    fun encode(message: Message) = Gson().toJson(message)
    fun decode(message: String) = Gson().fromJson(message, Message::class.java)
}