package oetw.questions.messages

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import oetw.questions.InfoSlide
import oetw.questions.MultipleChoiceSlide
import oetw.questions.OpenQuestionSlide
import oetw.questions.Ranking
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type")
@JsonSubTypes(
	Type(value = WelcomeMessage::class, name = "welcome_message"),
	Type(value = WaitingForPlayersMessage::class, name = "waiting_for_players_message"),
	Type(value = RejectedUsernameMessage::class, name = "rejected_username_message"),
	Type(value = InfoMessage::class, name = "info_message"),
	Type(value = ErrorMessage::class, name = "error_message"),
	Type(value = MultipleChoiceMessage::class, name = "multiple_choice_question_message"),
	Type(value = OpenQuestionMessage::class, name = "open_question_message"),
	Type(value = RejectedAnswerMessage::class, name = "reject_answer_message"),
	Type(value = AnsweredMessage::class, name = "answered_message"),
	Type(value = MissingAnswerMessage::class, name = "missing_answer_message"),
	Type(value = CorrectAnswerMessage::class, name = "correct_answer_message"),
	Type(value = IncorrectAnswerMessage::class, name = "incorrect_answer_message"),
	Type(value = NoSuchGameMessage::class, name = "no_such_game_message"),
	Type(value = ClosingMessage::class, name = "closing_message"),
	Type(value = CurrentScoreMessage::class, name = "current_score_message"),
	Type(value = ScoreUpdateMessage::class, name = "score_update_message"),
	Type(value = CurrentPositionMessage::class, name = "current_position_message"),
	Type(value = PlayerJoinedMessage::class, name = "player_joined_message"),
	Type(value = RoundResultsMessage::class, name = "round_result_message"),
	Type(value = WelcomeBackMessage::class, name = "welcome_back_message")
)
sealed class ServerMessage

/**
 * As a response to [HelloMessage]. Indicating that the provided username was accepted and further communication
 * should use the provided jwt.
 */
data class WelcomeMessage(val jwt: String) : ServerMessage()
class WelcomeBackMessage() : ServerMessage()

class PlayerJoinedMessage(val username: String) : ServerMessage()
class RoundResultsMessage(val correctAnswer: String, val top5: Map<String, Int>) : ServerMessage()

/**
 * Indicating the current game state is waiting for players.
 */
class WaitingForPlayersMessage() : ServerMessage()

/**
 * As a response to [HelloMessage]. Indicating that the provided username was rejected.
 */
data class RejectedUsernameMessage(val reason: String) : ServerMessage()

data class OpenQuestionMessage(val uuid: UUID, val question: String, val imageUrl: String?) : ServerMessage() {
	constructor(slide: OpenQuestionSlide) : this(slide.uuid, slide.question, slide.imageUrl)
}

data class Answer(val uuid: UUID, val answer: String)
data class MultipleChoiceMessage(val uuid: UUID, val question: String, val answers: List<Answer>) : ServerMessage() {
	constructor(slide: MultipleChoiceSlide) : this(slide.uuid, slide.question, slide.answers.map { Answer(it.uuid, it.answer) })
}

class MissingAnswerMessage(val score: Int) : ServerMessage()
class CorrectAnswerMessage(val score: Int) : ServerMessage()
class IncorrectAnswerMessage(val score: Int) : ServerMessage()
class CurrentScoreMessage(val score: Int) : ServerMessage()
class CurrentPositionMessage(val score: Int, val position: Int) : ServerMessage()
class AnsweredMessage() : ServerMessage()
class RejectedAnswerMessage() : ServerMessage()
class NoSuchGameMessage(val gameId: String) : ServerMessage()
class ClosingMessage(val scores: Map<String, Int>) : ServerMessage()
class ScoreUpdateMessage(rankings: List<Ranking>) : ServerMessage()

data class InfoMessage(val title: String, val text: String, val imageUrl: String) : ServerMessage() {
	constructor(slide: InfoSlide) : this(slide.title, slide.text, slide.imageUrl)
}

data class ErrorMessage(val stackTrace: String) : ServerMessage() {
	companion object {
		fun from(e: Throwable): ErrorMessage {
			val writer = StringWriter();
			e.printStackTrace(PrintWriter(writer));
			return ErrorMessage(writer.toString());
		}
	}
}



