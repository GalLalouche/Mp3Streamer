$(() => {
  const buttonAux = (id, text) => button({"id": id}, text)

  const poster = $("#jp_poster_0")
  poster.addClass("poster")
  const div = poster.closest("div")
  const posterAndButtonsDiv = table({"id": "poster-table"}).append(
      tr().append(
          td({"class": "poster-buttons left-poster-buttons"}).append(
              $("<input id=playlist_name placeholder='Remote playlist name' class='poster-buttons'>"),
              buttonAux("update_backup", "Update backup"),
          ),
          td().append(poster),
          td({"class": "poster-buttons right-poster-buttons"}).append(
              buttonAux("load_playlist", "Load playlist"),
              buttonAux("load_backup", "Load backup"),
          ),
      )
  )
  div.prepend(posterAndButtonsDiv)
  Poster.playlistName = $('#playlist_name')
  Poster.playlistName.val(localStorage.getItem(PLAYLIST_NAME_KEY))

  poster[0].addEventListener('load', function() {
    getColorAsync(poster.attr("src"), rgb => {
      const color = rgb.makeLighter(0.5)
      document.body.style.backgroundColor = color.toString()
      Poster.rgbListeners.forEach(l => l(color))
    })
  })
})

const Poster = {}
Poster.rgbListeners = []
Poster.playlistName = {}
const PLAYLIST_NAME_KEY = "playlist_name"
