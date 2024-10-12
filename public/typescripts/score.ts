// TODO use enums and use Object.values to get this list for the drop down list.
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

class ScoreOps {
  private readonly fieldset = $("#score")
  constructor() {
    this.fieldset.on('change', 'select', function () {
      const newScore = $(this).val()
      const source = $(this).attr('source')
      $.put(`score/${source}/${newScore}/${gplaylist.currentPlayingSong().file}`, function (e) {
        console.log(`Successfully updated ${source} score to ${newScore}`)
      })
    })
  }
  show(song: Song): void {
    this.clearScores()
    $.get("score/" + song.file, score => this.updateScore(score))
  }

  private clearScores(): void {
    this.fieldset.empty()
    this.fieldset.append($("<div>Waiting for score...<\div>"))
  }

  private updateScore(score: ScoreResult): void {
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
  (window as any).Score = new ScoreOps()
})