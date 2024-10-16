// TODO use enums and use Object.values to get this list for the drop down list.
import {gplaylist, Song} from "./types.js"

export type Score = "Default" | "Crappy" | "Meh" | "Okay" | "Good" | "Great" | "Amazing"
export type Source = "Artist" | "Album" | "Song"

class ScoreResult {
  constructor(
    readonly score: Score,
    readonly source: Source,
    readonly song: Score,
    readonly album: Score,
    readonly artist: Score,
  ) {}
}

export class ScoreOps {
  private static fieldset: JQuery<HTMLElement>
  static setup(): void {
    this.fieldset = $("#score")
    this.fieldset.on('change', 'select', function () {
      const newScore = $(this).val()
      const source = $(this).attr('source')!.toLowerCase()
      $.put(`score/${source}/${newScore}/${gplaylist.currentPlayingSong().file}`, function () {
        console.log(`Successfully updated ${source} score to ${newScore}`)
      })
    })
  }
  static show(song: Song): void {
    this.clearScores()
    $.get("score/" + song.file, score => this.updateScore(score))
  }

  private static clearScores(): void {
    this.fieldset.empty()
    this.fieldset.append($("<div>Waiting for score...<\div>"))
  }

  private static updateScore(score: ScoreResult): void {
    function scoreMenu(key: keyof ScoreResult): JQuery<HTMLElement> {
      const result = $("<select>")
      for (const s of ["Default", "Crappy", "Meh", "Okay", "Good", "Great", "Amazing"]) {
        const option = elem("option", s)
        if (s === score[key])
          option.attr("selected", "selected")
        result.append(option)
      }
      result.attr('source', key.capitalize())
      return $("<div>").append(span(`${key}`)).append(result)
    }

    this.fieldset.empty()
    this.fieldset
      .append(elem("legend", score.score ? `${score.score} (from ${score.source})` : "No score"))
      .append(scoreMenu("song"))
      .append(scoreMenu("album"))
      .append(scoreMenu("artist"))
  }
}

$(function () {
  ScoreOps.setup()
})
