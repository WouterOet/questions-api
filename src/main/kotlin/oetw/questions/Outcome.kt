package oetw.questions

import oetw.questions.messages.ServerMessage
import javax.websocket.Session

class Outcome(
	val answers: List<ServerMessage> = listOf(),
	val broadcast: Map<Session, ServerMessage> = mapOf(),
	val admin: Pair<List<Session>, ServerMessage>? = null
)
