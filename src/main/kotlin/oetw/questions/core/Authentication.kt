package oetw.questions.core

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

object Authentication {
	private val ALGORITHM: Algorithm = Algorithm.HMAC256("secret");
	private val CLIENT_VERIFICATION = JWT.require(ALGORITHM)
		.withIssuer("questions")
		.withClaim("type", "client")
		.build()
	private val ADMIN_VERIFICATION = JWT.require(ALGORITHM)
		.withIssuer("questions")
		.withClaim("type", "admin")
		.build()

	fun verifyJwtToken(rawToken: String): DecodedJWT = CLIENT_VERIFICATION.verify(rawToken)

	fun createClientJwt(username: String): String = getJwt(username, "client")

	private fun getJwt(username: String, type: String): String {
		return JWT.create()
			.withIssuer("questions")
			.withClaim("username", username)
			.withClaim("type", type)
			.withExpiresAt(
				Date.from(LocalDateTime.now().plus(5, ChronoUnit.MINUTES).toInstant(ZoneOffset.UTC))
			)
			.sign(ALGORITHM)
	}

	fun createAdminJwt(username: String): String = getJwt(username, "admin")
	fun verifyAdminJwtToken(rawToken: String): DecodedJWT = ADMIN_VERIFICATION.verify(rawToken)
}
