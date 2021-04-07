package oetw.questions.boundry

import com.fasterxml.jackson.databind.ObjectMapper
import oetw.questions.core.Router
import oetw.questions.messages.ErrorMessage
import oetw.questions.messages.ReceivingMessage
import oetw.questions.messages.ServerMessage
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.PathParam
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/game/{gameId}")
class WsEndpoint(
	private val objectMapper: ObjectMapper,
	private val router: Router) {

	private val log by lazy { LoggerFactory.getLogger(javaClass) }

	@OnMessage
	fun onMessage(session: Session, @PathParam("gameId") gameId: String, payload: String) {
		log.info("Game '{}' received message '{}'", gameId, payload)

		val outcome = router.route(gameId, session, objectMapper.readValue(payload, ReceivingMessage::class.java))
		outcome.answers.forEach { sendMessage(session, gameId, it) }
		outcome.broadcast.forEach { (session, message) -> sendMessage(session, gameId, message) }
		outcome.admin?.let { (sessions, message) -> sessions.forEach { sendMessage(it, gameId, message) } }
	}

	private fun sendMessage(session: Session, gameId: String, message: ServerMessage) {
		val messageAsJson = objectMapper.writeValueAsString(message)
		log.info("Sending message from game '{}': {}", gameId, messageAsJson)
		session.asyncRemote.sendText(messageAsJson)
	}

	@OnError
	fun error(session: Session, e: Throwable) {
		if (!(e.javaClass == IOException::class.java && e.message == "Connection reset by peer")) {
			log.error("Exception occurred in client context", e)
			sendMessage(session, "-1", ErrorMessage.from(e))
		}
	}
}
