$(() => {
// <button id="update_playlist">Update playlist</button>
//   <button id="load_playlist">Load playlist</button>
//   <br/>
//   <button id="update_state">Update state</button>
//   <button id="load_state">Load state</button>
//   <br/>
//   <button id="update_backup">Update backup</button>
//   <button id="load_backup">Load backup</button>
  const buttonAux = (id, text) => button({"id": id}, text)
  const poster = $("#jp_poster_0")
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
})