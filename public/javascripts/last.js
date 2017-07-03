$(function() {
  const last_albums = elem("ul").appendTo($("#last_albums")).css("list-style", "none")
  const add = "plus"
  const remove = "times"
  function onMessage(msg) {
    const album = JSON.parse(msg.data)
    const listElement = elem("li", `${icon(add)} ${icon(remove)} ${album.artistName}: ${album.title}`)
    listElement.find(".fa-" + add).click(function() {
      listElement.remove()
      $.get("data/albums/" + album.dir, e => gplaylist.add(e, false))
    })
    listElement.find(".fa-" + remove).click(function() {
      listElement.remove()
    })
    last_albums.append(listElement)
  }

  // TODO instead of sleep-wait reconnecting, do this on search/scan
  openConnection("last_album", onMessage, true)
})