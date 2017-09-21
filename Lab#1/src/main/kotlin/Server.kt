import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import protocol.ClientType
import protocol.Encoder
import protocol.Message
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

val queue: Channel<Message> = Channel()

fun main(args: Array<String>) {
    runServer()
}

suspend fun handleClient(client: Socket) {
    println("handling client...")
    val reader = BufferedReader(InputStreamReader(client.inputStream))
    val writer = PrintWriter(client.outputStream)
    val msg = Encoder.decode(reader.readLine())
    if (msg.clientType == ClientType.RECEIVER) {
        println("CONNECTED RECEIVER " + msg.clientUid)
        println("SENT MSG " + msg.msg)
        if (queue.isEmpty)
            writer.println(Message(clientType = ClientType.SERVER, msg = "IDLE"))
        else
            writer.println(Encoder.encode(queue.receive()))
        writer.flush()
    }
    else if (msg.clientType == ClientType.SENDER) {
        println("CONNECTED SENDER " + msg.clientUid)
        println("GOT MSG " + msg.msg)
        queue.send(msg)
    }
    writer.close()
    reader.close()
    client.close()
}

fun runServer() {
    println("Starting server...")
    val server = ServerSocket(14141)
    while (true) {
        println("Waiting for a client...")
        val client = server.accept()
        launch(newSingleThreadContext("context")) {
            handleClient(client)
        }
    }
}