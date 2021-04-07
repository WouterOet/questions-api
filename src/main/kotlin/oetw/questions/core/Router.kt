package oetw.questions.core

import oetw.questions.Games
import oetw.questions.Outcome
import oetw.questions.messages.AdminHelloMessage
import oetw.questions.messages.AuthenticatedAdminMessage
import oetw.questions.messages.AuthenticatedClientMessage
import oetw.questions.messages.HelloMessage
import oetw.questions.messages.MultipleChoiceAnswerMessage
import oetw.questions.messages.NextMessage
import oetw.questions.messages.NoSuchGameMessage
import oetw.questions.messages.OpenQuestionAnswerMessage
import oetw.questions.messages.ReceivingMessage
import oetw.questions.messages.ResumeMessage
import javax.enterprise.context.ApplicationScoped
import javax.websocket.Session

@ApplicationScoped
class Router(private val games: Games) {

	fun route(gameId: String, session: Session, message: ReceivingMessage): Outcome {
		val game = games.get(gameId) ?: return Outcome(answers = listOf(NoSuchGameMessage(gameId)))

		when (message) {
			is HelloMessage, is AdminHelloMessage -> Unit
			is AuthenticatedClientMessage -> message.verify()
			is AuthenticatedAdminMessage -> message.verify()
		}

		return when (message) {
			is AdminHelloMessage -> game.acceptAdmin(message, session)
			is HelloMessage -> game.acceptNewPlayer(message, session)
			is ResumeMessage -> game.acceptExistingPlayer(message, session)
			is NextMessage -> game.nextSlide()
			is MultipleChoiceAnswerMessage -> game.acceptMCAnswer(message)
			is OpenQuestionAnswerMessage -> game.acceptOpenAnswer(message)
		}
	}
}
