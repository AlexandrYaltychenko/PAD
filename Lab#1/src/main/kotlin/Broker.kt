import com.google.gson.Gson
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.runBlocking
import protocol.ClientType
import protocol.Message
import util.decode
import util.encode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

val queue: Channel<Message> = Channel()

fun main(args: Array<String>) {
    runServer()
}

fun backupQueue(){
    runBlocking {
        println("BACKUPING...")
    }
}

suspend fun handleClient(client: Socket) {
    println("handling client...")
    val reader = BufferedReader(InputStreamReader(client.inputStream))
    val writer = PrintWriter(client.outputStream)
    val msg = reader.readLine().decode()
    if (msg.clientType == ClientType.RECEIVER) {
        println("CONNECTED RECEIVER " + msg.clientUid)
        println("SENT MSG " + msg.msg)
        try {
            println("QUEUE IS " + queue.encode())
        } catch (e: Exception) {
            println(e.message)
        }
        if (queue.isEmpty)
            writer.println(Message(clientType = ClientType.SERVER, msg = "IDLE"))
        else
            writer.println(queue.receive().encode())
        writer.flush()
        println("QUEUE PROCESSED!")
    } else if (msg.clientType == ClientType.SENDER) {
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