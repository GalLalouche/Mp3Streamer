$(() => {
  const buttonAux = (id, text) => button({"id": id}, text)

  const poster = $("#jp_poster_0")
  poster.addClass("poster")
  const div = poster.closest("div")
  const posterAndButtonsDiv = table({"id": "poster-table"}).append(
      tr().append(
          td({"class": "poster-buttons left-poster-buttons"}).append(
              buttonAux("update_playlist", "Update playlist"),
              buttonAux("update_state", "Update state"),
              buttonAux("update_backup", "Update backup"),
              buttonAux("update_clipboard", "Update clipboard"),
          ),
          td().append(poster),
          td({"class": "poster-buttons right-poster-buttons"}).append(
              buttonAux("load_playlist", "Load playlist"),
              buttonAux("load_state", "Load state"),
              buttonAux("load_backup", "Load backup"),
              buttonAux("load_clipboard", "Load clipboard"),
          ),
      )
  )
  div.prepend(posterAndButtonsDiv)

  poster[0].addEventListener('load', function() {
    getColorAsync(poster.attr("src"), rgb => {
      document.body.style.backgroundColor = rgb2String(makeLighter(rgb, 0.5))
    })
  })
})
