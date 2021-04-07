package oetw.questions

import oetw.questions.core.Game

object PremadeGames {
	fun theITGame(): Game = Game(
		"The IT Game",
		SlideSet(
			listOf(
				InfoSlide(
					title = "Hi folks",
					text = "Welcome to this game!"
				),
				InfoSlide(
					title = "Start of easy",
					text = "You should know this stuff"
				),
				MultipleChoiceSlide(
					question = "cat Data.class | head -c 4 | hexdump",
					answers = listOf(
						Answer(answer = "ABADBABE"),
						Answer(answer = "BEEFBABE"),
						Answer(answer = "CAFEBABE"),
						Answer(answer = "DEADBEAF")
					),
					correctAnswer = 2,
					note = "Magic constant"
				),
				MultipleChoiceSlide(
					question = "How old is Java?",
					answers = listOf(
						Answer(answer = "15 years"),
						Answer(answer = "20 years"),
						Answer(answer = "25 years"),
						Answer(answer = "30 years")
					),
					correctAnswer = 2,
					note = "5/23/1995, Oak"
				),
				trueFalseQuestion("True or False: Java supports trailing commas?", true, "When declaring annotations and in array definitions"),
				MultipleChoiceSlide(
					question = "How old is Ubuntu",
					answers = listOf(
						Answer(answer = "15 years"),
						Answer(answer = "20 years"),
						Answer(answer = "25 years"),
						Answer(answer = "30 years")
					),
					correctAnswer = 0,
					note = "20 October 2004"
				),
				MultipleChoiceSlide(
					question = "HTTP 302 status code",
					answers = listOf(
						Answer(answer = "Moved Temporarily"),
						Answer(answer = "Found"),
						Answer(answer = "Moved Permanently"),
						Answer(answer = "See Other")
					),
					correctAnswer = 1,
					note = "Although it used to be Moved Temporarily, it is now defined as Found"
				),
				MultipleChoiceSlide(
					question = "How old is Kotlin?",
					answers = listOf(
						Answer(answer = "5 years"),
						Answer(answer = "7 years"),
						Answer(answer = "9 years"),
						Answer(answer = "11 years")
					),
					correctAnswer = 2
				),
				MultipleChoiceSlide(
					question = "I'm a teapot",
					answers = listOf(
						Answer(answer = "416"),
						Answer(answer = "417"),
						Answer(answer = "418"),
						Answer(answer = "419")
					),
					correctAnswer = 2,
					note = "Hyper Text Coffee Pot Control Protocol, April Fools"
				),
				ScoreUpdateSlide(),
				InfoSlide(
					title = "Next up",
					text = "Movie nerd facts"
				),
				MultipleChoiceSlide(
					question = "To see reality you take the...",
					answers = listOf(
						Answer(answer = "Blue pill"),
						Answer(answer = "Red pill")
					),
					correctAnswer = 1,
					note = "The matrix"
				),
				MultipleChoiceSlide(
					question = "In the series, the IT crowd, what is the most famous line?",
					answers = listOf(
						Answer(answer = "What's up doc?"),
						Answer(answer = "There is no spoon"),
						Answer(answer = "I'll be back"),
						Answer(answer = "Have you tried turning it of and on again?")
					),
					correctAnswer = 3
				),
				ScoreUpdateSlide(),
				InfoSlide(
					title = "Next up",
					text = "True or False?"
				),
				trueFalseQuestion("True or False: A day is always shorter than a year", false, "Venus has the longest day of any planet in our solar system. It completes one rotation every 243 Earth days. Its day lasts longer than its orbit. It orbits the Sun every 224.65 Earth days, so a day is nearly 20 Earth days longer than its year."),
				trueFalseQuestion("True or False: There is an island called Hawaii 2.", true, "Cards against Humanity bought an island in Maine to preserve wildlife."),
				trueFalseQuestion("True or False: If you eat 10 or more banana's a day you will probably die of radiation poisoning", false, "Although banana do give of radiation, eating 10 bananas a day would only increase you daily dose of radiation by 10%"),
				trueFalseQuestion("True or False: Babies have around 100 more bones than adults", true, "Al we grow older, bones fuse together thus reducing the number of them."),
				trueFalseQuestion("True or False: Traveling at light speed, it will take 4 days to reach Pluto", false, "Actually only about 4.5 hours. "),
				trueFalseQuestion("True or False: Once every 34 years the moon will be closer by than New Zealand", false, "When the moon is closest by it is about 363,105 kilometer, Distance The Netherlands: 18,526 kilometer"),
				trueFalseQuestion("True or False: The number 1 is a prime number", false, "No, just no"),
				trueFalseQuestion("True or False: Consumer grade alcohol in the US must be radio-active", true, "First we need to go to space where there is lots of radiation. This radiation creates Carbon-14, which is radio-active and has a half-life of 5,730 years. All organisms on this planet consume this radio-active carbon. Including for instance the wheat which is used to make spirits. Thus your spirit is radio-active. However there is a second way of making alcohol. From petrolium, however this is illegal in the US for consumergrade alcohol. This petrolium has been in the ground for millions of years and thus no longer contains Carbon-14 as it has decayed away. So to check if your spirit was made correctly, they check if it is radio-active."),
				trueFalseQuestion("France borders with our Kingdom", true, "France borders the Kingdom of the Netherlands through the French portion of Saint Martin."),
				ScoreUpdateSlide(),
				InfoSlide(
					title = "Next up",
					text = "You work here. This should be easy"
				),
				MultipleChoiceSlide(
					question = "The height of the Rabo tower is:",
					answers = listOf(
						Answer(answer = "95 m"),
						Answer(answer = "105 m"),
						Answer(answer = "115 m"),
						Answer(answer = "125 m")
					),
					correctAnswer = 2,
					note = "If you jump, you'll splat on the pavement at 45.37 m/s"
				),
				MultipleChoiceSlide(
					question = "How many times was the tower on fire?",
					answers = listOf(
						Answer(answer = "1"),
						Answer(answer = "2"),
						Answer(answer = "3"),
						Answer(answer = "5")
					),
					correctAnswer = 2,
					note = "This caused it to get the nickname: \"vuurtoren\""
				),
				MultipleChoiceSlide(
					question = "What was the net profit for 2019 for the Rabobank?",
					answers = listOf(
						Answer(answer = "1703 million"),
						Answer(answer = "2203 million"),
						Answer(answer = "2702 million"),
						Answer(answer = "3203 million")
					),
					correctAnswer = 1,
					note = "Time for a raise?"
				),
				MultipleChoiceSlide(
					question = "According to Wikipedia, how many FTE's does Rabobank have in The Netherlands?",
					answers = listOf(
						Answer(answer = "22,000"),
						Answer(answer = "27,000"),
						Answer(answer = "32,000"),
						Answer(answer = "37,000")
					),
					correctAnswer = 1
				),
				ScoreUpdateSlide(),
				InfoSlide("Next up", "Random questions"),
				MultipleChoiceSlide(
					question = "According to google: how large is the index behind Googles search engine?",
					answers = listOf(
						Answer(answer = "100,000,000 GB"),
						Answer(answer = "1.000,000,000 GB"),
						Answer(answer = "10,000,000,000 GB"),
						Answer(answer = "100,000,000,000 GB")
					),
					correctAnswer = 0,
					note = "This is according to Google. I thought it was a bit small, my homework folder is almost that size"
				),
				MultipleChoiceSlide(
					question = "How many planets our there in our solar system?",
					answers = listOf(
						Answer(answer = "6"),
						Answer(answer = "7"),
						Answer(answer = "8"),
						Answer(answer = "9")
					),
					correctAnswer = 2,
					note = "Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus, Neptune"
				),
				MultipleChoiceSlide(
					question = "The drone-ship on which SpaceX rockets land is called ...",
					answers = listOf(
						Answer(answer = "Welcome back"),
						Answer(answer = "How was your flight"),
						Answer(answer = "Of course I still love you"),
						Answer(answer = "This is the ship you are looking for")
					),
					correctAnswer = 2,
					note = "When the rocket lands, the boats rocks so hard due to the rockets exhaust, that the satellite link fails."
				),
				MultipleChoiceSlide(
					question = "How many countries are in the United Nations including non-member observer states?",
					answers = listOf(
						Answer(answer = "109"),
						Answer(answer = "125"),
						Answer(answer = "131"),
						Answer(answer = "3,143")
					),
					correctAnswer = 1,
					note = "Fun fact: there are in fact 3143 counties, counties not countries, in the US"
				),
				MultipleChoiceSlide(
					question = "listen",
					answers = listOf(
						Answer(answer = "15 km/h"),
						Answer(answer = "30 km/h"),
						Answer(answer = "45 km/h"),
						Answer(answer = "60 km/h")
					),
					correctAnswer = 0,
					note = "Panzer is the German word for 'armour' or specifically 'tank'. The first German tank was the A7V, build in 1918. What has the top speed of this tank on roads? "
				)
			)
		)
	)
}

fun trueFalseQuestion(question: String, answer: Boolean, note: String = "") = MultipleChoiceSlide(
	question = question,
	answers = listOf(
		Answer(answer = "True"),
		Answer(answer = "False")
	),
	correctAnswer = if (answer) 0 else 1,
	note = note
)
