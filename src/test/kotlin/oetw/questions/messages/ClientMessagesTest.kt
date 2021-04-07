package oetw.questions.messages

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.*
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.UUID.randomUUID
import java.util.stream.Stream
import kotlin.reflect.KClass

class ClientMessagesTest {

	private lateinit var mapper: ObjectMapper;

	@BeforeEach
	internal fun setUp() {
		mapper = ObjectMapper().registerModule(KotlinModule())
	}

	@ParameterizedTest
	@MethodSource("messages")
	internal fun serialization(message: ReceivingMessage, type: String) {
		val json = mapper.writeValueAsString(message)
		val tree = mapper.readTree(json)
		val actualType = tree.get("type").asText()
		Assertions.assertEquals(type, actualType)
	}

	companion object {
		@JvmStatic
		fun messages() = Stream.of(
			of(HelloMessage("asdf"), "hello_message"),
			of(MultipleChoiceAnswerMessage("jwt", randomUUID(), randomUUID()), "multiple_choice_answer_message"),
			of(OpenQuestionAnswerMessage("jwt", randomUUID(), "some answer"), "open_question_answer_message"),
			of(ResumeMessage("jwt"), "resume_message"),
			of(AdminHelloMessage("asdf"), "admin_hello_message"),
			of(NextMessage("jwt"), "admin_next_message")
		)
	}


}
