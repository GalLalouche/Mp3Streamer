package backend.recon

import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.{Assertion, FreeSpec}

import common.test.AuxSpecs

// Golden tests to verify album recon scores. It's machine learning for morons, basically.
class AlbumReconScorerTest extends FreeSpec with AuxSpecs {
  private val $ = TestModuleConfiguration().injector.instance[AlbumReconScorer]

  "Golden tests" - {
    def testingName(s1: String, s2: String): Double = ignoreArtist(s1 -> 2000, s2 -> 2000)
    def ignoreArtist(nameYear1: (String, Int), nameYear2: (String, Int)): Double = $(
      Album(nameYear1._1, nameYear2._2, Artist("Foo")),
      Album(nameYear2._1, nameYear2._2, Artist("Foo")),
    )
    def negativeTest(score: Double): Assertion = score should be < 0.8
    def positiveTest(score: Double): Assertion = score should be > 0.8
    "question mark" in {
      negativeTest(testingName("Live in Berlin", "Question mark"))
    }
    "singles" in {
      negativeTest(testingName("singles", "songs"))
    }
    "Addicts" in {
      positiveTest(testingName("Addicts: Black Meddle, Part 2", "Addicts Black Meddle II"))
    }
    "Metropolis" in {
      positiveTest(
        testingName("Metropolis, Pt. 2: Scenes From a Memory", "Metropolis II Scenes from a Memory"),
      )
    }
    "Hatches" in {
      positiveTest(ignoreArtist("Batten the Hatches" -> 2005, "BATTEN THE HATCHES" -> 2007))
    }

    "Common prefixes" - {
      "1" in {
        positiveTest(
          testingName("Plague of Butterflies", "Plague of Butterflies / Out of This Gloomy Light"),
        )
      }
      "2" in {
        positiveTest(testingName("High Visceral {Part One}", "High Visceral, I"))
      }
      "3" in {
        positiveTest(testingName("Lover's End, Part III: Skellefteå serenade", "Lover's End III"))
      }
      "4" in {
        positiveTest(
          testingName("The Very Best Of / The Complete Greatest Hits", "The Very Best Of"),
        )
      }
      "5" in {
        positiveTest(testingName("Retrospectacle: The Supertramp Anthology", "Retrospectacle"))
      }
      "Negative" in {
        negativeTest(testingName("Lover's End, Part III: Skellefteå serenade", "Love is Ending"))
      }
    }
  }
}
