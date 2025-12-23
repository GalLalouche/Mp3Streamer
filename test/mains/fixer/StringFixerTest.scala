package mains.fixer

import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.tagobjects.Slow

import common.test.AuxSpecs

class StringFixerTest extends AnyFreeSpec with AuxSpecs {
  private val $ = TestModuleConfiguration().injector.instance[StringFixer]

  private def verifyFix(original: String, fix: String): Unit = $(original) shouldReturn fix
  private def verifyEmptyFix(original: String): Unit = verifyFix(original, original)

  "Edge cases" in {
    verifyEmptyFix("")
    verifyFix("    ", "")
  }
  "Stupid spaces" in {
    verifyFix("  Foo Bar ", "Foo Bar")
    // List is from https://stackoverflow.com/a/28295597/736508
    val badChars = Vector('\u00A0', '\u2007', '\u202F')
    verifyFix(badChars.reverse.mkString + "  foo bar " ++ badChars, "Foo Bar")
    verifyFix("Missed Call (1)", "Missed Call (1)")
  }
  "Ordered numerals" in {
    verifyEmptyFix("1st")
    verifyEmptyFix("2nd")
    verifyEmptyFix("3rd")
    verifyEmptyFix("4th")
    verifyEmptyFix("27th")
    verifyEmptyFix("101st")
  }
  "I and a" in {
    verifyFix("a foo", "A Foo")
    verifyFix("foo a", "Foo a")
    verifyFix("i foo", "I Foo")
    verifyFix("foo i", "Foo I")
  }
  "Rock n' Roll" in {
    Vector(
      "rock 'N' roll",
      "rock 'n' roll",
      "Rock n' Roll",
      "Rock N' Roll",
    ).foreach(verifyFix(_, "Rock n' Roll"))
  }
  "vs" in {
    Vector(
      "Freddy vs Json",
      "Freddy VS json",
      "Freddy vs. Json",
      "Freddy Vs. Json",
      "Freddy Vs Json",
    ).foreach(verifyFix(_, "Freddy vs. Json"))
  }
  "Mixed caps" in {
    Vector("FOO", "FoO", "fOo").foreach(e => verifyFix(e, e))
  }
  "Capitalize" in {
    Vector("foobar", "Foobar").foreach(verifyFix(_, "Foobar"))
  }
  "Sentence" in {
    verifyFix("i am a rock", "I am a Rock")
    verifyFix("a rock am i", "A Rock am I")
    verifyFix("Rock 'Em like a rock", "Rock 'em Like a Rock")
  }
  "Is sorted" in {
    StringFixer.lowerCaseWords.sorted shouldReturn StringFixer.lowerCaseWords
  }
  "With dots" in {
    Vector("F.F.S.", "f.f.s.").foreach(verifyFix(_, "F.F.S."))
  }
  "Roman numerals" in {
    verifyFix("mmxvi", "MMXVI")
  }
  "Asciify" in {
    verifyFix("Köyliönjärven jäällä", "Koylionjarven Jaalla")
    verifyFix("Blóðstokkinn", "Blodstokkinn")
    verifyFix("Епосі нескорених поетів", "Eposi Neskorenikh Poetiv")
    verifyFix("Æther", "Aether")
    verifyFix("Rundtgåing", "Rundtgaing")
    verifyFix("Død", "Dod")
    verifyFix("Níðhöggr", "Nidhoggr")
    verifyFix("Palästinalied", "Palastinalied")
    verifyFix("niþer", "Nither")
    verifyFix("Ê´coute", "E'coute")
    verifyFix("Utgarđr", "Utgardr")
    verifyFix("Coup de Grâce", "Coup de Grace")
  }

  "with delimiters" in {
    verifyFix("foo/bar", "Foo/Bar")
  }
  "strips" in {
    verifyFix(" Foo Bar ", "Foo Bar")
  }
  "stupid apostrophe" in {
    verifyFix("I’m a stupid apostrophe", "I'm a Stupid Apostrophe")
    verifyFix("Don�t", "Don't")
    verifyFix(
      "I Don’t love you, Like I Loved you, whenever",
      "I don't Love You, Like I Loved You, Whenever",
    )
  }
  "dashes" in {
    // All of these dashes are different! Freaking unicode.
    verifyFix("A−B-C–D-E—F‐G-H", "A-B-C-D-E-F-G-H")
  }

  "Only minimal changes are done to Hebrew" in {
    verifyEmptyFix("אהבת נעוריי")
    verifyFix("אביב גדג’", "אביב גדג'")
    verifyFix("שב”ק", "שב\"ק")
    verifyFix("  אהבת נעוריי ", "אהבת נעוריי")
  }

  "colons and other delimiters" in {
    verifyFix("The band: The Album & The movie", "The Band: The Album & The Movie")
    verifyFix("The band (the album)", "The Band (The Album)")
    verifyFix("I. the Test", "I. The Test")
  }

  "Can handle °" in {
    verifyFix("100°", "100 Degrees")
  }

  // Slow, since it uses external python program
  "Asian languages" taggedAs Slow in {
    verifyEmptyFix("センチメートル")
    verifyEmptyFix("アン")
    verifyEmptyFix("한국")
  }

  "Ellipsis" in {
    verifyFix("…And the blind one came", "...And the Blind One Came")
  }
}
