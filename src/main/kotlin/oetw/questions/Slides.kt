package oetw.questions

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import oetw.questions.messages.MultipleChoiceAnswerMessage
import oetw.questions.messages.OpenQuestionAnswerMessage
import java.time.LocalDateTime
import java.util.*

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type")
@JsonSubTypes(
	JsonSubTypes.Type(value = StartSlide::class, name = "start_slide"),
	JsonSubTypes.Type(value = MultipleChoiceSlide::class, name = "multiple_choice_slide")
)
sealed class Slide

class Slides(val slides: List<Slide>)

class StartSlide : Slide()
data class Answer(val uuid: UUID = UUID.randomUUID(), val answer: String)
data class MultipleChoiceSlide(val uuid: UUID = UUID.randomUUID(), val question: String, val answers: List<Answer>, val correctAnswer: Answer, val note: String) : Slide() {

	val answersGiven: MutableMap<String, Pair<UUID, LocalDateTime>> = mutableMapOf()

	constructor(question: String, answers: List<Answer>, correctAnswer: Int, note: String = "") : this(
		question = question,
		answers = answers,
		correctAnswer = answers[correctAnswer],
		note = note
	)

	init {
		if (!answers.contains(correctAnswer)) throw IllegalArgumentException()
	}

	fun acceptAnswer(username: String, uuid: UUID): Boolean = answersGiven.putIfAbsent(username, uuid to LocalDateTime.now()) == null
}

class OpenQuestionSlide(val uuid: UUID = UUID.randomUUID(), val question: String, val answer: String, val note: String = "", val imageUrl: String = "") : Slide() {
	val answersGiven: MutableMap<String, Pair<String, LocalDateTime>> = mutableMapOf()

	fun acceptAnswer(username: String, answer: String) = answersGiven.putIfAbsent(username, answer to LocalDateTime.now()) == null
}

interface AnswerSlide {
	fun results() : Map<String, Pair<Boolean, LocalDateTime>>
	fun correctAnswer() : String
}

data class McAnswerSlide(private val mcSlide: MultipleChoiceSlide) : Slide(), AnswerSlide {
	override fun results() = mcSlide.answersGiven.map {
		it.key to ((mcSlide.correctAnswer.uuid == it.value.first) to it.value.second) }.toMap()

	override fun correctAnswer() = mcSlide.correctAnswer.answer

}

data class OpenAnswerSlide(private val openQuestionSlide: OpenQuestionSlide) : Slide(), AnswerSlide {
	override fun results() = openQuestionSlide.answersGiven.map {
		it.key to ((it.value.first.toLowerCase().contains(openQuestionSlide.answer.toLowerCase())) to it.value.second) }.toMap()

	override fun correctAnswer() = openQuestionSlide.answer
}

data class InfoSlide(val title: String, val text: String, val imageUrl: String = "") : Slide()

class ScoreUpdateSlide : Slide()
class ClosingSlide : Slide()

class SlideSet(list: List<Slide>) {

	private val cursor: Iterator<Slide> = (setOf(StartSlide()) + list.flatMap {
		when (it) {
			is InfoSlide,
			is McAnswerSlide,
			is StartSlide,
			is OpenAnswerSlide,
			is ClosingSlide,
			is ScoreUpdateSlide -> listOf(it)
			is MultipleChoiceSlide -> listOf(it, McAnswerSlide(it))
			is OpenQuestionSlide -> listOf(it, OpenAnswerSlide(it))
		}
	} + setOf(InfoSlide("That's it", "How do you think you did?"), ClosingSlide())).iterator()

	private var currentSlide = cursor.next()

	fun currentSlide() = currentSlide;

	fun nextSlide(): Slide {
		if(cursor.hasNext()) currentSlide = cursor.next()
		return currentSlide
	}

	fun acceptNewPlayers() = currentSlide is StartSlide

	fun acceptMCAnswer(message: MultipleChoiceAnswerMessage): Boolean {
		val fixedCurrentSlide = currentSlide
		return if(fixedCurrentSlide is MultipleChoiceSlide && fixedCurrentSlide.uuid == message.questionUUID) {
			fixedCurrentSlide.acceptAnswer(message.username, message.answerUUID)
		} else {
			false
		}
	}

	fun acceptOpenAnswer(message: OpenQuestionAnswerMessage): Boolean {
		val fixedCurrentSlide = currentSlide
		return if(fixedCurrentSlide is OpenQuestionSlide && fixedCurrentSlide.uuid == message.questionUUID) {
			fixedCurrentSlide.acceptAnswer(message.username, message.answer)
		} else {
			false
		}
	}

}

