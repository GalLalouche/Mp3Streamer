$(function() {
  const topLevel = $("#albums")
  function topLevelButton(value, action) {
    $(`<input type='button' value='${value}'/>`)
        .click(action)
        .appendTo(topLevel)
  }
  const allAccordions = () => $(".ui-accordion-content")
  topLevelButton("Show all", function() {
    allAccordions().show();
  })
  topLevelButton("Hide all", function() {
    allAccordions().hide();
  })
  topLevelButton("Sort by genre", function() {
    sortByGenre()
  })
  let albums = null
  topLevelButton("Sort by year", function() {
    const albumsByYear = albums.flatMap(x => {
      const albums = x.albums
      albums.forEach(a => {
        a.artistName = x.name
        a.genre = x.genre
        a.name = x.genre
      })
      return albums
    }).custom_group_by(e => new Date(e.date).getFullYear())
    const andThenByGenre = map_values(albumsByYear, e => e.custom_group_by(e => e.genre))
    const result = Object.entries(andThenByGenre).custom_sort_by(e => -e[0])
        .flatMap(e => {
          const [key, value] = e
          const asArray = Object.entries(value).map(e => {
            const [genre, albums] = e
            return ({name: genre, albums: albums})
          })
          return addTopLevelElement(key, asArray)
        })

    makeAccordion(result)
  })

  function makeAccordion(array) {
    topLevel.children("ol").remove()
    const topList = $("<ol>").appendTo(topLevel)
    for (const e of array)
      topList.append(e)
    topList.accordion({
      collapsible: true,
      heightStyle: "content",
    })
  }
  const button = (text, clazz) => elem("button", text).addClass(clazz)
  const createHideButton = () => button("Hide", "hide")

  function putArtist(actionType, text, success) {
    assert(text, "No artist data extracted")
    $.ajax({
      url: `artist/${actionType}/${text}`,
      data: text,
      type: "PUT",
      contentType: "text/plain",
      dataType: "text",
      success: success
    })
  }

  function putAlbum(actionType, text, success) {
    assert(text, "No album data extracted")
    putJson(actionType, text, success)
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
  function addEntry(entryName, albums, isYearEntry) {
    const albumsElem = elem("ol")
    const entryElem = elem("li", `${entryName} `)
    if (isYearEntry.isFalse())
      entryElem
          .append(button("Ignore", "ignore-artist"))
          .append(button("Remove", "remove-artist"))
          .append(createHideButton())
          .data("artistName", entryName)

    entryElem
        .append(albumsElem)

    for (const album of albums) {
      const value = isYearEntry
          ? `[${album.albumType}] ${album.artistName} - ${album.title} (${toIsoDate(new Date(album.date))}) `
          : `[${album.albumType}] ${album.title} (${toIsoDate(new Date(album.date))}) `
      elem("li", value)
          .data({
            "artistName": isYearEntry ? album.artistName : entryName,
            "year": album.year,
            "title": album.title
          })
          .appendTo(albumsElem)
          .append(button("Ignore", "ignore-album"))
          .append(button("Remove", "remove-album"))
          .append(createHideButton())
          .append(button("Google torrent", "google-torrent"))
          .append(button("Copy to clipboard", "copy-to-clipboard"))
    }
    return entryElem
  }

  const yearRe = /\d{4}/
  function addTopLevelElement(key, entries) {
    const elementDiv = div()
    const isYearEntry = yearRe.test(key)
    entries.forEach(o => addEntry(o.name, o.albums, isYearEntry).appendTo(elementDiv))
    return [$(`<h5>${key}</h5>`), elementDiv]
  }

  function sortByGenre() {
    const byGenre = map_values(albums.custom_group_by(e => e.genre), e => e.custom_sort_by(e => e.name))
    const result = Object.entries(byGenre).custom_sort_by(e => e[0])
        .flatMap(e => {
          const [key, value] = e
          return addTopLevelElement(key, value)
        })
    makeAccordion(result)
  }
  $.get("albums/", function(e) {
    albums = e
    sortByGenre()
  })

  const hideParent = parent => () => parent.hide()
  function onClick(classSelector, f) {
    topLevel.on("click", "." + classSelector, e => f($(e.target).closest("li")))
  }

  onClick("hide", parent => parent.hide())
  onClick("ignore-artist", parent => putArtist("ignore", parent.data("artistName"), hideParent(parent)))
  onClick("remove-artist", parent => putArtist("remove", parent.data("artistName"), hideParent(parent)))
  onClick("ignore-album", parent => putAlbum("album/ignore", parent.data(), hideParent(parent)))
  onClick("remove-album", parent => putAlbum("album/remove", parent.data(), hideParent(parent)))
  onClick("google-torrent", parent => {
    const data = parent.data()
    window.open(`https://rutracker.net/forum/tracker.php?nm=${data.artistName} ${data.title}`)
  })
  onClick("copy-to-clipboard", parent => {
    const data = parent.data()
    copyTextToClipboard(`${data.artistName} ${data.title}`)
  })
})
