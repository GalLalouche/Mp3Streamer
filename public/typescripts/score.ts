// TODO use enums and use Object.values to get this list for the drop down list.
import {gplaylist, Song} from "./types.js"

export namespace Score {
  export function setup(): void {
    fieldset = $("#score")
    fieldset.on('change', 'select', async function () {
      const newScore = $(this).val() as Score
      const source = $(this).attr('source')!.toLowerCase() as Source
      return setScore(gplaylist.currentPlayingSong(), source, newScore)
    })
    fieldset.on('click', 'button', async function () {
      $.ajax({url: "score/" + gplaylist.currentPlayingSong().file, type: "PATCH"})
    })
  }

  export function show(song: Song): void {
    clearScores()
    $.get("score/" + song.file, score => updateScore(song, score))
  }

  export function popup(song: Song): void {
    $.get("score/" + song.file, scoreResult => scoreSliderDialog(song, scoreResult))
  }
}

async function setScore(song: Song, source: Source, score: Score): Promise<void> {
  return $.put(`score/${source.toLowerCase()}/${score}/${song.file}`, function () {
    console.log(`Successfully updated ${source} score to ${score}`)
  })
}

function scoreSliderDialog(song: Song, scoreResult: ScoreResult): void {
  let modified = false

  const style = $('<style>').text(`
     .slider-container {
        position: relative;
        margin: 20px 10px;
    }
    .ui-slider {
        margin: 0 8px;  /* Add margin to match tick mark container */
    }
    .slider-tick-marks {
        display: flex;
        justify-content: space-between;
        padding: 0 10px;  /* Match the margin of ui-slider */
        margin-top: 5px;
        width: calc(100% - 20px);  /* Account for padding */
    }
    .slider-tick-marks span {
        position: relative;
        display: flex;
        width: 1px;
        background: #ccc;
        height: 5px;
    }
    .slider-tick-marks span::after {
        position: absolute;
        top: 6px;
        content: attr(data-value);
        font-size: 12px;
        color: #888;
        transform: translateX(-50%);  /* Center the number under the tick */
    }
    .slider-label {
        margin-left: 0px;
        display: block;
        margin-top: 5px;
    }
    .ui-dialog .ui-dialog-content {
        padding-top: 0;
    }
    `)
  $('head').append(style)

  const dialogDiv = div()

  for (const source of ["Song", "Album", "Artist"] as const) {
    const slider = div().slider({
      min: 0,
      max: MAX_ORDINAL,
      value: toOrdinal(scoreFor(scoreResult, source)),
      step: 1,
      slide: async function (e, ui) {
        assertOrdinal(ui.value!)
        const newScore = fromOrdinal(ui.value)
        await setScore(song, source, newScore)
        modified = true
      },
    })

    const label = span({
      class: 'slider-label',
      text: `${source} (${titleFor(source, song)}) score`,
      css: {marginLeft: '10px', marginBottom: '10px'},
    })

    const tickMarks = div({class: 'slider-tick-marks'})
    for (let tick = 0; tick <= MAX_ORDINAL; tick++) {
      assertOrdinal(tick)
      tickMarks.append(span({'data-value': fromOrdinal(tick)}))
    }

    const sliderForSource = div({
      class: 'slider-container',
      css: {margin: '20px 10px'},
    }).append(label, slider, tickMarks)

    dialogDiv.append(sliderForSource)
  }

  function handler(e: JQuery.ClickEvent): void {
    if (dialogDiv.dialog("widget").has(e.target).length == 0) {
      dialogDiv.dialog("close")
    }
  }

  dialogDiv.dialog({
    autoOpen: true,
    width: 400,
    modal: true,
    title: "Editing score",
    close: () => {
      if (modified && song === gplaylist.currentPlayingSong())
        Score.show(song)
      dialogDiv.remove()
      $('.ui-widget-overlay').off("click", handler)
    },
  })
  $('.ui-widget-overlay').on("click", handler)
}

function clearScores(): void {
  fieldset.empty()
  fieldset.append($("<div>Waiting for score...<\div>"))
}

function updateScore(song: Song, score: ScoreResult): void {
  function scoreMenu(key: keyof ScoreResult): JQuery<HTMLElement> {
    const result = $("<select>")
    for (const s of ["Default", "Crappy", "Meh", "Okay", "Good", "Great", "Amazing"]) {
      const option = elem("option", s)
      if (s === score[key])
        option.attr("selected", "selected")
      result.append(option)
    }
    result.attr('source', key.capitalize()).css("margin-left", "20px")
    return div().append(span(`${key}`)).append(result).on('keydown', function (event) {
      // Prevent keys from changing the score, as sometimes things like pausing/unpausing can
      // accidentally change the score!
      event.preventDefault()
      console.log("Score key shortcuts are disabled.")
    })
  }

  fieldset.empty()
  fieldset
    .css("display", "flex")
    .append(elem("legend", score.score ? `${score.score} (from ${score.source})` : "No score"))
    .append(
      div({style: "display: flex; flex-direction: column;"})
        .append(scoreMenu("song"))
        .append(scoreMenu("album"))
        .append(scoreMenu("artist")),
    )
    .append(button("Open score file").css("margin-left", "20px"))
}

let fieldset: JQuery<HTMLElement>

type Score = "Default" | "Crappy" | "Meh" | "Okay" | "Good" | "Great" | "Amazing"

function toOrdinal(score: Score): number {
  switch (score) {
    case "Default":
      return 0
    case "Crappy":
      return 1
    case "Meh":
      return 2
    case "Okay":
      return 3
    case "Good":
      return 4
    case "Great":
      return 5
    case "Amazing":
      return 6
  }
}

const MAX_ORDINAL = 6

function assertOrdinal(n: number): asserts n is 0 | 1 | 2 | 3 | 4 | 5 | 6 {
  assert(n >= 0 && n <= MAX_ORDINAL)
}

function fromOrdinal(tick: 0 | 1 | 2 | 3 | 4 | 5 | 6): Score {
  switch (tick) {
    case 0:
      return "Default"
    case 1:
      return "Crappy"
    case 2:
      return "Meh"
    case 3:
      return "Okay"
    case 4:
      return "Good"
    case 5:
      return "Great"
    case 6:
      return "Amazing"
  }
}

type Source = "Artist" | "Album" | "Song"

type ScoreResult = {
  readonly score: Score,
  readonly source: Source,
  readonly song: Score,
  readonly album: Score,
  readonly artist: Score,
}

function scoreFor(result: ScoreResult, source: Source): Score {
  switch (source) {
    case "Artist":
      return result.artist
    case "Album":
      return result.album
    case "Song":
      return result.song
  }
}

function titleFor(source: Source, song: Song): string {
  switch (source) {
    case "Artist":
      return song.artistName
    case "Album":
      return song.albumName
    case "Song":
      return song.title
  }
}


$(function () {
  Score.setup()
})
