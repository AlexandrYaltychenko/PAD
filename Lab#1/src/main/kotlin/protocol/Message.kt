package protocol

data class Message(val clientType: ClientType,
                   val type: String = "",
                   val clientUid: String = "",
                   val msg: String = "")