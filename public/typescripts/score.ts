// TODO use enums and use Object.values to get this list for the drop down list.
import {gplaylist, Song} from "./types.js"

export namespace Score {
  export function setup(): void {
    fieldset = $("#score")
    fieldset.on('change', 'select', function () {
      const newScore = $(this).val()
      const source = $(this).attr('source')!.toLowerCase()
      $.put(`score/${source}/${newScore}/${gplaylist.currentPlayingSong().file}`, function () {
        console.log(`Successfully updated ${source} score to ${newScore}`)
      })
    })
  }

  export function show(song: Song): void {
    clearScores()
    $.get("score/" + song.file, score => updateScore(score))
  }
}

function clearScores(): void {
  fieldset.empty()
  fieldset.append($("<div>Waiting for score...<\div>"))
}

function updateScore(score: ScoreResult): void {
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

  fieldset.empty()
  fieldset
    .append(elem("legend", score.score ? `${score.score} (from ${score.source})` : "No score"))
    .append(scoreMenu("song"))
    .append(scoreMenu("album"))
    .append(scoreMenu("artist"))
}

let fieldset: JQuery<HTMLElement>

type Score = "Default" | "Crappy" | "Meh" | "Okay" | "Good" | "Great" | "Amazing"
type Source = "Artist" | "Album" | "Song"

class ScoreResult {
  constructor(
    readonly score: Score,
    readonly source: Source,
    readonly song: Score,
    readonly album: Score,
    readonly artist: Score,
  ) {}
}


$(function () {
  Score.setup()
})
