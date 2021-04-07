package oetw.questions

import oetw.questions.core.Game
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class Games {

	private val games = mutableMapOf<String, Game>()

	init {
		games["Maui"] = PremadeGames.theITGame()
	}

	fun store(gameId: String, slides: Slides) = games.putIfAbsent(gameId, Game(gameId, SlideSet(slides.slides))) == null

	fun get(gameId: String) = games[gameId]
}
