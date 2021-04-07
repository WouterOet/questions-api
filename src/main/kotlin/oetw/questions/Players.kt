package oetw.questions

import oetw.questions.core.ScoringCalculator
import oetw.questions.messages.CorrectAnswerMessage
import oetw.questions.messages.CurrentPositionMessage
import oetw.questions.messages.IncorrectAnswerMessage
import oetw.questions.messages.MissingAnswerMessage
import oetw.questions.messages.ServerMessage
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.websocket.Session

data class Ranking(val username: String, val score: Int)

class Players {
	private val players = ConcurrentHashMap<String, Session>()
	private var scores = ConcurrentHashMap<String, Int>()

	fun scores() = HashMap(scores)

	fun newPlayer(username: String, session: Session) = players.putIfAbsent(username, session) == null

	fun broadcast(message: ServerMessage) = players.values.map { it to message }.toMap()

	fun currentScore(username: String) = scores[username]

	fun currentRanking() =
		scores.mergeReduceFull(players) { username, score, session -> session to Ranking(username, score ?: 0) }
			.filterKeys { key -> key != null }
			.mapKeys { (key, _) -> key!! }
			.toList()
			.sortedWith(Comparator.comparingInt { (_, ranking) -> ranking.score })

	fun incrementScores(results: Map<String, Pair<Boolean, LocalDateTime>>): Map<Session, ServerMessage> {
		val increments = ScoringCalculator().calculate(results)
		scores = ConcurrentHashMap(scores.mergeReduce(increments) { _, value1, value2 ->
			(value1 ?: 0).plus((value2 ?: 0))
		})

		return players.mergeReduceFull(results) { key, value1, value2 ->
			value1!! to when (value2?.first) {
				null -> MissingAnswerMessage(scores[key] ?: 0)
				true -> CorrectAnswerMessage(scores[key] ?: 0)
				false -> IncorrectAnswerMessage(scores[key] ?: 0)
			}
		}
	}

	fun top5(): Map<String, Int> = scores.asIterable()
		.sortedWith(Comparator.comparingInt { (_, score) -> score })
		.take(5)
		.map { (username, score) -> username to score }
		.toMap()

	private fun <K, V1, V2, R> Map<K, V1>.mergeReduce(other: Map<K, V2>, reduce: (key: K, value1: V1?, value2: V2?) -> R): Map<K, R> =
		(this.keys + other.keys).associateWith { reduce(it, this[it], other[it]) }

	private fun <K1, K2, V1, V2, R> Map<K1, V1>.mergeReduceFull(other: Map<K1, V2>, reduce: (key1: K1, value1: V1?, value2: V2?) -> Pair<K2, R>): Map<K2, R> =
		(this.keys + other.keys).map { key -> reduce.invoke(key, this[key], other[key]) }.toMap()

	fun acceptExistingPlayer(username: String, session: Session) {
		players[username] = session
	}
}
