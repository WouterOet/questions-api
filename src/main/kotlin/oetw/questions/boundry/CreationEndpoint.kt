package oetw.questions.boundry

import oetw.questions.Games
import oetw.questions.Slides
import org.slf4j.LoggerFactory
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("creation")
@Consumes(MediaType.APPLICATION_JSON)
class CreationEndpoint(private val games: Games) {

	private val log by lazy { LoggerFactory.getLogger(javaClass) }

	@POST
	@Path("/{gameId}")
	fun storeGame(@PathParam("gameId") gameId: String, slides: Slides): Response {
		log.info("Received new game with id $gameId")
		val result = games.store(gameId, slides)
		return if (result) Response.ok().build() else Response.status(Response.Status.BAD_REQUEST).entity("Already exists").build()
	}
}
