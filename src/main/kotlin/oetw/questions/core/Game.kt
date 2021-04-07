package oetw.questions.core

import oetw.questions.AnswerSlide
import oetw.questions.ClosingSlide
import oetw.questions.InfoSlide
import oetw.questions.McAnswerSlide
import oetw.questions.MultipleChoiceSlide
import oetw.questions.OpenAnswerSlide
import oetw.questions.OpenQuestionSlide
import oetw.questions.Outcome
import oetw.questions.Players
import oetw.questions.ScoreUpdateSlide
import oetw.questions.Slide
import oetw.questions.SlideSet
import oetw.questions.StartSlide
import oetw.questions.messages.AdminHelloMessage
import oetw.questions.messages.AnsweredMessage
import oetw.questions.messages.ClosingMessage
import oetw.questions.messages.CurrentPositionMessage
import oetw.questions.messages.CurrentScoreMessage
import oetw.questions.messages.HelloMessage
import oetw.questions.messages.InfoMessage
import oetw.questions.messages.MultipleChoiceAnswerMessage
import oetw.questions.messages.MultipleChoiceMessage
import oetw.questions.messages.OpenQuestionAnswerMessage
import oetw.questions.messages.OpenQuestionMessage
import oetw.questions.messages.PlayerJoinedMessage
import oetw.questions.messages.RejectedAnswerMessage
import oetw.questions.messages.RejectedUsernameMessage
import oetw.questions.messages.ResumeMessage
import oetw.questions.messages.RoundResultsMessage
import oetw.questions.messages.ScoreUpdateMessage
import oetw.questions.messages.ServerMessage
import oetw.questions.messages.WaitingForPlayersMessage
import oetw.questions.messages.WelcomeBackMessage
import oetw.questions.messages.WelcomeMessage
import org.slf4j.LoggerFactory
import javax.websocket.Session

open class Game(val name: String, private val slides: SlideSet) {

	private val log by lazy { LoggerFactory.getLogger(javaClass) }
	private val finalPlayerMessage = InfoMessage("See main screen", "So excited!", "")
	private val players: Players = Players()
	private val admins = mutableListOf<Session>()

	open fun acceptNewPlayer(message: HelloMessage, session: Session): Outcome {
		return if (slides.acceptNewPlayers()) {
			val username = message.username
			val wasInserted = players.newPlayer(username, session)
			return if (wasInserted) {
				log.info("New user: {}", username)
				Outcome(
					answers = listOf(WelcomeMessage(Authentication.createClientJwt(username)), currentSlideAsMessage()),
					admin = admins to PlayerJoinedMessage(username)
				)
			} else {
				Outcome(answers = listOf(RejectedUsernameMessage("Username already taken")))
			}
		} else {
			Outcome(answers = listOf(RejectedUsernameMessage("Game already started")))
		}
	}

	private fun currentSlideAsMessage(slide: Slide = slides.currentSlide()): ServerMessage = when (slide) {
		is StartSlide -> WaitingForPlayersMessage()
		is MultipleChoiceSlide -> MultipleChoiceMessage(slide)
		is OpenQuestionSlide -> OpenQuestionMessage(slide)
		is InfoSlide -> InfoMessage(slide)
		is ClosingSlide -> finalPlayerMessage
		is McAnswerSlide, is OpenAnswerSlide, is ScoreUpdateSlide -> throw IllegalArgumentException("Should not reach this code. But it is only matter of time...")
	}

	open fun acceptExistingPlayer(message: ResumeMessage, session: Session): Outcome {
		players.acceptExistingPlayer(message.username, session)
		val currentMessage = when(val currentSlide = slides.currentSlide()) {
			is AnswerSlide -> CurrentScoreMessage(players.currentScore(message.username) ?: 0)
			is ScoreUpdateSlide -> TODO()
			else -> currentSlideAsMessage(currentSlide)
		}
		return Outcome(answers = listOf(WelcomeBackMessage(), currentMessage))
	}

	open fun nextSlide(): Outcome {
		return when (val slide = slides.nextSlide()) {
			is AnswerSlide -> answerResults(slide)
			is ScoreUpdateSlide -> {
				val currentRanking = players.currentRanking()
				Outcome(
					broadcast = currentRanking.mapIndexed { index, (session, ranking) -> session to CurrentPositionMessage(index + 1, ranking.score) }.toMap(),
					admin = admins to ScoreUpdateMessage(currentRanking.map { (_, ranking) -> ranking })
				)
			}
			is ClosingSlide -> {

				Outcome(
					broadcast = players.broadcast(finalPlayerMessage),
					admin = admins to ClosingMessage(players.scores())
				)
			}
			else -> {
				val message = currentSlideAsMessage(slide)
				Outcome(
					broadcast = players.broadcast(message),
					admin = admins to message
				)
			}
		}
	}

	private fun answerResults(slide: AnswerSlide): Outcome {
		val results = slide.results()
		return Outcome(
			broadcast = players.incrementScores(results),
			admin = admins to RoundResultsMessage(slide.correctAnswer(), players.top5())
		)
	}

	open fun acceptAdmin(message: AdminHelloMessage, session: Session): Outcome {
		admins.add(session)
		return Outcome(answers = listOf(WelcomeMessage(Authentication.createAdminJwt(message.username))))
	}

	open fun acceptMCAnswer(message: MultipleChoiceAnswerMessage) = answer(slides.acceptMCAnswer(message))

	open fun acceptOpenAnswer(message: OpenQuestionAnswerMessage) = answer(slides.acceptOpenAnswer(message))

	private fun answer(accepted: Boolean) =
		if (accepted) Outcome(answers = listOf(AnsweredMessage())) else Outcome(answers = listOf(RejectedAnswerMessage()))

}
