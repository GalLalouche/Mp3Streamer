$(function() {
  const fieldset = $("#score")
  // TODO this should use a similar color scheme to external links
  function clearScores() {
    fieldset.empty()
    fieldset.append($("<div>Waiting for score...<\div>"))
  }
  function updateScore(score) {
    function scoreMenu(title) {
      const result = $("<select>")
      const normalizedTitle = title.toLowerCase()
      for (const s of ["Default", "Crappy", "Meh", "Okay", "Good", "Great", "Amazing", "Classic"]) {
        const option = elem("option", s)
        if (s === score[normalizedTitle])
          option.attr("selected", "selected")
        result.append(option)
      }
      result.attr('source', normalizedTitle)
      return $("<div>").append(span(`${title}`)).append(result)
    }
    fieldset.empty()
    if (score.score)
      fieldset
          .append(elem("legend", `${score.score} (from ${score.source})`))
          .append(scoreMenu("Song"))
          .append(scoreMenu("Album"))
          .append(scoreMenu("Artist"))
    else
      fieldset
          .attr("legend", `No score`)
  }
  Score.show = function(song) {
    clearScores()
    $.get("score/" + song.file, score => updateScore(score))
  }
  fieldset.on('change', 'select', function() {
    const newScore = $(this).val()
    const source = $(this).attr('source')
    $.put(`score/${source}/${newScore}/${gplaylist.currentPlayingSong().file}`, function(e) {
      console.log(`Successfully updated ${source} score to ${newScore}`)
    })
  })
})
Score = {}