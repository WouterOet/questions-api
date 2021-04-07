package oetw.questions

object TestGames {

	val scenario_1 = Slides(listOf(
		InfoSlide(
			title = "Hallo",
			text = "Blaat"
		),
		MultipleChoiceSlide(
			question = "q0",
			answers = listOf(
				Answer(answer = "a0"),
				Answer(answer = "a1"),
				Answer(answer = "a2"),
				Answer(answer = "a3")
			),
			correctAnswer = 3
		),
		MultipleChoiceSlide(
			question = "q1",
			answers = listOf(
				Answer(answer = "a0"),
				Answer(answer = "a1"),
				Answer(answer = "a2"),
				Answer(answer = "a3")
			),
			correctAnswer = 3
		),
		ScoreUpdateSlide(),
		MultipleChoiceSlide(
			question = "q2",
			answers = listOf(
				Answer(answer = "a0"),
				Answer(answer = "a1"),
				Answer(answer = "a2"),
				Answer(answer = "a3")
			),
			correctAnswer = 3
		),
		OpenQuestionSlide(
			question = "Name someone",
			answer = "Someone"
		)
	))
}
