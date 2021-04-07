package oetw.questions.messages

import com.auth0.jwt.interfaces.DecodedJWT
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import oetw.questions.core.Authentication
import java.util.*
import com.fasterxml.jackson.annotation.JsonSubTypes.Type

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type")
@JsonSubTypes(
	Type(value = HelloMessage::class, name = "hello_message"),
	Type(value = MultipleChoiceAnswerMessage::class, name = "multiple_choice_answer_message"),
	Type(value = OpenQuestionAnswerMessage::class, name = "open_question_answer_message"),
	Type(value = ResumeMessage::class, name = "resume_message"),
	Type(value = AdminHelloMessage::class, name = "admin_hello_message"),
	Type(value = NextMessage::class, name = "admin_next_message")
)
sealed class ReceivingMessage

sealed class AdminClientMessage : ReceivingMessage()
sealed class AuthenticatedAdminMessage(val jwt: String) : AdminClientMessage() {
	fun verify() = Authentication.verifyAdminJwtToken(jwt)
}

class AdminHelloMessage(val username: String) : AdminClientMessage()
class NextMessage(jwt: String) : AuthenticatedAdminMessage(jwt)

sealed class ClientMessage() : ReceivingMessage()
sealed class AuthenticatedClientMessage(val jwt: String) : ClientMessage() {

	private lateinit var decodedJWT: DecodedJWT

	val username: String
		@JsonIgnore
		get() = decodedJWT.claims["username"]!!.asString()

	fun verify() {
		decodedJWT = Authentication.verifyJwtToken(jwt)
	}
}

class HelloMessage(val username: String) : ClientMessage()

class MultipleChoiceAnswerMessage(jwt: String, val questionUUID: UUID, val answerUUID: UUID) : AuthenticatedClientMessage(jwt)
class OpenQuestionAnswerMessage(jwt: String, val questionUUID: UUID, val answer: String) : AuthenticatedClientMessage(jwt)
class ResumeMessage(jwt: String) : AuthenticatedClientMessage(jwt)
