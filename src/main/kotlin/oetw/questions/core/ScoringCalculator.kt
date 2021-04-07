package oetw.questions.core

import java.time.LocalDateTime

class ScoringCalculator {

	fun calculate(results: Map<String, Pair<Boolean, LocalDateTime>>) =
		results.map {
			it.key to if(it.value.first) 100 else 1
		}.toMap()

}
