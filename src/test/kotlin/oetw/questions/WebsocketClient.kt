package oetw.questions

import org.slf4j.LoggerFactory
import java.net.URI
import javax.websocket.ClientEndpoint
import javax.websocket.CloseReason
import javax.websocket.ContainerProvider
import javax.websocket.OnClose
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session

@ClientEndpoint
class WebsocketClient {

	private val log by lazy { LoggerFactory.getLogger(javaClass) }

	private var session: Session? = null
	lateinit var gameId: String
	var messageHandler: Consumer? = null

	companion object {
		fun connect(gameId: String): WebsocketClient {
			val client = WebsocketClient()
			client.gameId = gameId
			return connect(client)
		}

		fun connect(client: WebsocketClient): WebsocketClient {
			try {
				ContainerProvider.getWebSocketContainer().connectToServer(client, URI("ws://localhost:8081/game/${client.gameId}"))
				return client
			} catch (e: Exception) {
				throw RuntimeException(e)
			}
		}
	}

	fun disconnect() {
		session?.close()
		session = null
	}

	fun reconnect() {
		connect(this)
	}

	/**
	 * Callback hook for Connection open events.
	 *
	 * @param userSession the userSession which is opened.
	 */
	@OnOpen
	fun onOpen(userSession: Session) {
		this.session = userSession
	}

	/**
	 * Callback hook for Connection close events.
	 *
	 * @param userSession the userSession which is getting closed.
	 * @param reason the reason for connection close
	 */
	@OnClose
	fun onClose(userSession: Session, reason: CloseReason?) {
		this.session = null
	}

	/**
	 * Callback hook for Message Events. This method will be invoked when a client send a message.
	 *
	 * @param message The text message
	 */
	@OnMessage
	fun onMessage(message: String) {
		log.info("Client received '$message'")
		messageHandler?.invoke(message)
	}

	fun sendMessage(message: String) {
		log.info("Client sending '$message'")
		session?.asyncRemote?.sendText(message)
	}

}

typealias Consumer = (String) -> Unit
