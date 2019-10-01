$(() => {
  const buttonAux = (id, text) => button({"id": id}, text)

  const poster = $("#jp_poster_0")
  poster.addClass("poster")
  const parent = poster.parent()
  const posterAndButtonsDiv = table({"id": "poster-table"}).append(
      tr().append(
          td({"class": "poster-buttons left-poster-buttons"}).append(
              buttonAux("update_playlist", "Update playlist"),
              buttonAux("update_state", "Update state"),
              buttonAux("update_backup", "Update backup"),
          ),
          td().append(poster),
          td({"class": "poster-buttons right-poster-buttons"}).append(
              buttonAux("load_playlist", "Load playlist"),
              buttonAux("load_state", "Load state"),
              buttonAux("load_backup", "Load backup"),
          ),
      )
  )
  parent.prepend(posterAndButtonsDiv)

  const colorThief = new ColorThief()
  const posterElement = poster[0]
  posterElement.addEventListener('load', updateBackground)
  function updateBackground() {
    const rgb = colorThief.getColor(posterElement)
    const hsl = rgb2hsl(rgb)
    // Make it a lot lighter (l is measured in %).
    const lighter = hsl[2] + (100 - hsl[2]) / 1.2
    document.body.style.backgroundColor = `hsl(${hsl[0]}, ${hsl[1]}%, ${lighter}%)`
  }
})
