package oetw.questions.messages

import com.fasterxml.jackson.annotation.JsonSubTypes
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.full.findAnnotation

internal class ServerMessageTest {

	@Test
	internal fun `all subclasses have an associated jsontype`() {
		val missing = ServerMessage::class.sealedSubclasses.minus(ServerMessage::class.findAnnotation<JsonSubTypes>()!!.value.map { it.value })
		assertEquals(0, missing.size, "Missing the following: $missing")
	}
}
