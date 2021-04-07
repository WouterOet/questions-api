package oetw.questions

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.test.junit.QuarkusTest
import oetw.questions.messages.AdminHelloMessage
import oetw.questions.messages.AnsweredMessage
import oetw.questions.messages.ClosingMessage
import oetw.questions.messages.CorrectAnswerMessage
import oetw.questions.messages.CurrentPositionMessage
import oetw.questions.messages.CurrentScoreMessage
import oetw.questions.messages.ErrorMessage
import oetw.questions.messages.HelloMessage
import oetw.questions.messages.IncorrectAnswerMessage
import oetw.questions.messages.InfoMessage
import oetw.questions.messages.MissingAnswerMessage
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import javax.inject.Inject
import kotlin.reflect.KClass

@QuarkusTest
class FullTest {

	private val log by lazy { LoggerFactory.getLogger(javaClass) }

	companion object {
		private var gameCounter: Int = 1
	}

	@Inject
	lateinit var objectMapper: ObjectMapper

	lateinit var client: WebsocketClient

	lateinit var adminClient: WebsocketClient

	lateinit var builder: ClientDialog

	@BeforeEach
	internal fun setUp() {
		setupGame()

		client = WebsocketClient.connect("$gameCounter")
		adminClient = WebsocketClient.connect("$gameCounter")
		builder = DialogBuilder(client, adminClient, objectMapper).start()

		gameCounter += 1
	}

	private fun setupGame() {
		val httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()
		val body = objectMapper.writeValueAsString(TestGames.scenario_1)
		log.info("Body: '$body'")
		val request = HttpRequest.newBuilder(URI("http://localhost:8081/creation/$gameCounter"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(body))
			.build()

		val statusCode = httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode()
		assertEquals(200, statusCode)
	}

	@Test
	internal fun `duplicate username`() {
		builder
			.sendMessage(HelloMessage("DeviantOrbit"))
			.expectMessage(WelcomeMessage::class)
			.expectMessage(WaitingForPlayersMessage::class)
			.sendMessage(HelloMessage("DeviantOrbit"))
			.expectMessage(RejectedUsernameMessage("Username already taken"))
			.done()
	}

	@Test
	internal fun `invalid jwt token with resume`() {
		builder
			.sendMessage(ResumeMessage("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.WDlNbJFe8ZX6C1mS27xwxg-9tk8vtkk6sDgucRj8xW0"))
			.expectMessage(ErrorMessage::class)
	}

	@Test
	internal fun `join after start`() {
		builder
			.asAdmin().sendMessage(AdminHelloMessage("SuperAdmin"))
			.asAdmin().expectMessage(WelcomeMessage::class) { context, message -> context.jwt = message.jwt }
			.asAdmin().nextSlide()
			.sendMessage(HelloMessage("DeviantOrbit"))
			.expectMessage(RejectedUsernameMessage("Game already started"))
			.done()
	}

	@Test
	internal fun `send duplicate answer`() {
		builder
			.asAdmin().sendMessage(AdminHelloMessage("SuperAdmin"))
			.asAdmin().expectMessage(WelcomeMessage::class) { context, message -> context.jwt = message.jwt }
			.sendMessage(HelloMessage("DeviantOrbit"))
			.expectMessage(WelcomeMessage::class) { context, message -> context.jwt = message.jwt }
			.expectMessage(WaitingForPlayersMessage::class)
			.asAdmin().nextSlide()
			.expectMessage(InfoMessage::class)
			.asAdmin().nextSlide()
			.expectMessageAndReply(MultipleChoiceMessage::class) { context, message ->
				context.put("temp_message_uuid", message.uuid)
				context.put("temp_answer_uuid", message.answers[0].uuid)
				MultipleChoiceAnswerMessage(context.jwt, message.uuid, message.answers[0].uuid)
			}
			.expectMessage(AnsweredMessage::class)
			.sendMessage { context -> MultipleChoiceAnswerMessage(context.jwt, context.get("temp_message_uuid") as UUID, context.get("temp_answer_uuid") as UUID) }
			.expectMessage(RejectedAnswerMessage::class)
	}

	@Test
	internal fun `full happy flow`() {
		builder
			.connectAdmin()
			.connectClient()
			.expectMessage(WaitingForPlayersMessage::class)
			.asAdmin().expectMessage(PlayerJoinedMessage::class) { message -> assertEquals("DeviantOrbit", message.username) }
			.asAdmin().nextSlide()
			.asAdmin().expectMessage(InfoMessage::class)
			.expectMessage(InfoMessage::class)
			.asAdmin().nextSlide()
			.asAdmin().expectMessage(MultipleChoiceMessage::class)
			.expectMessageAndReply(MultipleChoiceMessage::class) { context, message -> MultipleChoiceAnswerMessage(context.jwt, message.uuid, message.answers[2].uuid) }
			.expectMessage(AnsweredMessage::class)
			.asAdmin().nextSlide()
			.asAdmin().expectMessage(RoundResultsMessage::class)
			.expectMessage(IncorrectAnswerMessage::class) { message -> assertEquals(1, message.score) }
			.asAdmin().nextSlide()
			.asAdmin().expectMessage(MultipleChoiceMessage::class)
			.expectMessageAndReply(MultipleChoiceMessage::class) { context, message -> MultipleChoiceAnswerMessage(context.jwt, message.uuid, message.answers[3].uuid) }
			.expectMessage(AnsweredMessage::class)
			.asAdmin().nextSlide()
			.asAdmin().expectMessage(RoundResultsMessage::class) { message ->
				assertEquals("a3", message.correctAnswer)
				assertEquals(1, message.top5.size)
			}
			.expectMessage(CorrectAnswerMessage::class) { message -> assertEquals(101, message.score) }
			.asAdmin().nextSlide()
			.asAdmin().expectMessage(ScoreUpdateMessage::class)
			.expectMessage(CurrentPositionMessage::class)
			.asAdmin().nextSlide()
			.asAdmin().expectMessage(MultipleChoiceMessage::class)
			// Not answering
			.expectMessage(MultipleChoiceMessage::class)
			.asAdmin().nextSlide()
			.asAdmin().expectMessage(RoundResultsMessage::class)
			.expectMessage(MissingAnswerMessage::class) { message -> assertEquals(101, message.score) }
			.asAdmin().nextSlide()
			.asAdmin().expectMessage(OpenQuestionMessage::class)
			.expectMessageAndReply(OpenQuestionMessage::class) { context, message -> OpenQuestionAnswerMessage(context.jwt, message.uuid, "someone") }
			.expectMessage(AnsweredMessage::class)
			.asAdmin().nextSlide()
			.asAdmin().expectMessage(RoundResultsMessage::class)
			.expectMessage(CorrectAnswerMessage::class) { message -> assertEquals(201, message.score) }
			.asAdmin().nextSlide()
			.expectMessage(InfoMessage ("That's it", "How do you think you did?", ""))
			.asAdmin().expectMessage(InfoMessage ("That's it", "How do you think you did?", ""))
			.asAdmin().nextSlide()
			.expectMessage(InfoMessage("See main screen", "So excited!", ""))
			.asAdmin().expectMessage(ClosingMessage::class) { message -> assertEquals(mapOf("DeviantOrbit" to 201), message.scores)}
	}

	@Test
	internal fun `resume after game starts, info slide`() {
		builder
			.connectAdmin()
			.connectClient()
			.expectMessage(WaitingForPlayersMessage::class)
			.asAdmin().nextSlide()
			.expectMessage(InfoMessage::class)
			.disconnect()
			.reconnect()
			.sendMessage() { context -> ResumeMessage(context.jwt) }
			.expectMessage(WelcomeBackMessage::class)
			.expectMessage(InfoMessage::class)
	}

	@Test
	internal fun `error message on invalid json`() {
		builder
			.connectClient()
			.expectMessage(WaitingForPlayersMessage::class)
			.sendRaw("""{"not_valid_stuff": "this is"}""")
			.expectMessage(ErrorMessage::class)
	}

	@Test
	internal fun `resume on Waiting For Players Slide`() = resume(WaitingForPlayersMessage::class, 0)

	@Test
	internal fun `resume on Info Slide`() = resume(InfoMessage::class, 1)

	@Test
	internal fun `resume on MultipleChoice Slide`()  = resume(MultipleChoiceMessage::class, 2)

	@Test
	internal fun `resume on MC Answer`() = resume(CurrentScoreMessage::class, 3)

	internal fun resume(message: KClass<out ServerMessage>, skip: Int) {
		builder
			.connectAdmin()
			.connectClient()
			.expectMessage(WaitingForPlayersMessage::class)
			.skipSlides(skip)
			.disconnect()
			.reconnect()
			.sendMessage() { context -> ResumeMessage(context.jwt) }
			.expectMessage(WelcomeBackMessage::class)
			.expectMessage(message)
	}

	fun ClientDialog.connectAdmin() =
		this.asAdmin().sendMessage(AdminHelloMessage("SuperAdmin"))
			.asAdmin().expectMessage(WelcomeMessage::class) { context, message -> context.jwt = message.jwt }


	fun ClientDialog.connectClient() =
		this.sendMessage(HelloMessage("DeviantOrbit"))
			.expectMessage(WelcomeMessage::class) { context, message -> context.jwt = message.jwt }
}
