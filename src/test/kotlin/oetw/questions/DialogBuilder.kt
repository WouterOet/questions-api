package oetw.questions

import com.fasterxml.jackson.databind.ObjectMapper
import oetw.questions.messages.AdminClientMessage
import oetw.questions.messages.ClientMessage
import oetw.questions.messages.NextMessage
import oetw.questions.messages.ServerMessage
import org.junit.jupiter.api.Assertions
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass

class Context(private val map: MutableMap<String, Any> = mutableMapOf()) {

	var jwt: String
		get() = map["jwt"]!! as String
		set(value) {
			map["jwt"] = value
		}

	fun put(key: String, value: Any) {
		map[key] = value
	}

	fun get(key: String) = map[key]!!
}

abstract class Dialog<out T : Dialog<T>>(
	internal val client: WebsocketClient,
	internal val objectMapper: ObjectMapper,
	internal val context: Context = Context()
) {

	private val incomingMessages: Queue<ServerMessage> = ConcurrentLinkedQueue()

	abstract fun self(): T

	init {
		client.messageHandler = { incomingMessages.add(objectMapper.readValue(it, ServerMessage::class.java)) }
	}

	internal fun getMessage(): ServerMessage {
		for (x in 0..100) {
			val poll: ServerMessage? = incomingMessages.poll()
			poll?.let { return it }
			sleep(10)
		}
		throw RuntimeException("No message available")
	}

	fun expectMessage(expected: ServerMessage): T {
		val actual = getMessage()

		Assertions.assertEquals(expected, actual)
		return self()
	}

	fun expectMessage(expected: KClass<out ServerMessage>) =
		expectMessage(expected) { _, _ -> Unit }

	fun <R: ServerMessage> expectMessage(expected: KClass<out R>, assertions: (R) -> Unit) =
		expectMessage(expected) { _, message -> assertions.invoke(message)}

	fun <R : ServerMessage> expectMessage(expected: KClass<R>, contextUser: (Context, R) -> Unit): T {
		val actual = getMessage()

		Assertions.assertEquals(expected, actual::class, "The actual value was $actual")
		val message = expected.javaObjectType.cast(actual)

		contextUser.invoke(context, message)

		return self()
	}


	fun reconnect(): T {
		client.reconnect()
		return self()
	}

	fun disconnect(): T {
		client.disconnect()
		return self()
	}

	fun done() {
		Assertions.assertEquals(0, incomingMessages.size, "There are leftover messages")
	}

	fun dropMessage(): T {
		getMessage()
		return self()
	}

	fun sendRaw(message: String): T {
		client.sendMessage(message)
		return self()
	}
}

class ClientDialog(websocketClient: WebsocketClient,
				   private val adminDialog: AdminDialog,
				   objectMapper: ObjectMapper) : Dialog<ClientDialog>(websocketClient, objectMapper) {

	override fun self() = this

	fun asAdmin() = adminDialog

	fun sendMessage(message: ClientMessage): ClientDialog {
		client.sendMessage(objectMapper.writeValueAsString(message))

		return self()
	}

	fun sendMessage(clientUser: (Context) -> ClientMessage) = sendMessage(clientUser.invoke(context))

	fun <R: ServerMessage> expectMessageAndReply(expected: KClass<R>, contextUser: (Context, R) -> ClientMessage): ClientDialog {
		val actual = getMessage()

		Assertions.assertEquals(expected, actual::class)
		val message = expected.javaObjectType.cast(actual)

		val reply = contextUser.invoke(context, message)
		sendMessage(reply)
		return self()
	}

	fun skipSlides(skip: Int): ClientDialog {
		for (i in 1..skip) {
			this.asAdmin().nextSlide()
				.dropMessage()
		}
		return this
	}
}

class AdminDialog(websocketClient: WebsocketClient,
				  objectMapper: ObjectMapper) : Dialog<ClientDialog>(websocketClient, objectMapper) {

	internal lateinit var clientDialog: ClientDialog

	override fun self() = clientDialog

	fun sendMessage(message: AdminClientMessage): ClientDialog {
		val messageInJson = objectMapper.writeValueAsString(message)
		println(messageInJson)
		client.sendMessage(messageInJson)

		return self()
	}

	fun sendMessage(contextUser: (Context) -> AdminClientMessage): ClientDialog {
		val message = contextUser.invoke(context)
		return sendMessage(message)
	}

	fun nextSlide(): ClientDialog {
		sendMessage { context -> NextMessage(context.jwt) }
		sleep(10)
		return self()
	}

}

class DialogBuilder(
	client: WebsocketClient,
	adminClient: WebsocketClient,
	objectMapper: ObjectMapper
) {

	private val adminDialog = AdminDialog(adminClient, objectMapper)
	private val clientDialog: ClientDialog = ClientDialog(client, adminDialog, objectMapper)

	init {
		adminDialog.clientDialog = clientDialog
	}

	fun start() = clientDialog
}
