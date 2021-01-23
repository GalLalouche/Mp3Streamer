$(function() {
  const topLevel = $("#albums")
  topLevel.append($("<input type='button' value='Show all' id='show-all'/>"))
  const allAccordions = () => $(".ui-accordion-content")
  $("#show-all").click(function() {
    allAccordions().show();
  })
  topLevel.append($("<input type='button' value='Hide all' id='hide-all'/>"))
  $("#hide-all").click(function() {
    allAccordions().hide();
  })
  const genreList = $("<ol>").appendTo(topLevel)
  const button = (text, clazz) => elem("button", text).addClass(clazz)
  const createHideButton = () => button("Hide", "hide")

  function putArtist(actionType, text, success) {
    $.ajax({
      url: `artist/${actionType}/${text}`,
      data: text,
      type: "PUT",
      contentType: "text/plain",
      dataType: "text",
      success: success
    })
  }

  function toIsoDate(date) {
    let month = '' + (date.getMonth() + 1)
    let day = '' + date.getDate()
    const year = date.getFullYear()

    if (month.length < 2)
      month = '0' + month;
    if (day.length < 2)
      day = '0' + day;

    return [year, month, day].join('-');
  }
  function addArtist(artistName, albums) {
    const albumsElem = elem("ol")
    const artistElem = elem("li", `${artistName} `)
        .append(button("Ignore", "ignore-artist"))
        .append(button("Remove", "remove-artist"))
        .append(createHideButton())
        .append(albumsElem)
        .data("artistName", artistName)

    for (const album of albums)
      elem("li", `[${album.albumType}] ${album.title} (${toIsoDate(new Date(album.date))}) `)
          .data({"artistName": artistName, "year": album.year, "title": album.title})
          .appendTo(albumsElem)
          .append(button("Ignore", "ignore-album"))
          .append(button("Remove", "remove-album"))
          .append(createHideButton())
          .append(button("Google torrent", "google-torrent"))
    return artistElem
  }

  function addGenre(genre, artists) {
    const artistDiv = div()
    artists.forEach(o => addArtist(o.name, o.albums).appendTo(artistDiv))
    return genreList
        .append($(`<h5>${genre}</h5>`))
        .append(artistDiv)
  }

  $.get("albums/", function(e) {
    const byGenre = map_values(e.custom_group_by(e => e.genre), e => e.custom_sort_by(e => e.name))
    for (const [key, value] of Object.entries(byGenre).custom_sort_by(e => e[0]))
      genreList.append(addGenre(key, value))
    genreList.accordion({
      collapsible: true,
      heightStyle: "content",
    })
  })

  // buttons
  const hideParent = parent => () => parent.hide()

  function onClick(classSelector, f) {
    topLevel.on("click", "." + classSelector, e => f($(e.target).closest("li")))
  }

  onClick("hide", parent => parent.hide())
  onClick("ignore-artist", parent => putArtist("ignore", parent.data("artistName"), hideParent(parent)))
  onClick("remove-artist", parent => putArtist("remove", parent.data("artistName"), hideParent(parent)))
  onClick("ignore-album", parent => putJson("album/ignore", parent.data(), hideParent(parent)))
  onClick("remove-album", parent => putJson("album/remove", parent.data(), hideParent(parent)))

  onClick("google-torrent", parent => {
    const data = parent.data()
    window.open(`https://rutracker.net/forum/tracker.php?nm=${data.artistName} ${data.title}`)
  })
})
