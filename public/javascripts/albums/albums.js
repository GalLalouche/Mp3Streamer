$(function() {
  /** Setup **/
  const topLevel = $("#albums")
  function topLevelButton(value, action) {
    $(`<input type='button' value='${value}'/>`)
        .click(action)
        .appendTo(topLevel)
  }
  const allAccordions = () => $(".ui-accordion-content")
  topLevelButton("Show all", () => allAccordions().show())
  topLevelButton("Hide all", () => allAccordions().hide())
  topLevelButton("Sort by genre", sortByGenre)
  topLevelButton("Sort by year", sortByYear)
  topLevelButton("Sort by missing album count", sortByMissingAlbumsCount)

  let albumsByArtist = null
  $.get("albums/", function(e) {
    albumsByArtist = e
    sortByGenre()
  })

  /** Top-level button implementations **/
  function sortByGenre() {
    const byGenre = map_values(albumsByArtist.custom_group_by(e => e.genre), e => e.custom_sort_by(e => e.name))
    const result = Object.entries(byGenre).custom_sort_by(e => e[0])
        .flatMap(e => {
          const [key, value] = e
          return addTopLevelElement(key, value, TopSorting.BY_GENRE)
        })
    makeAccordion(result)
  }

  function sortByYear() {
    const albumsByYear = albumsByArtist.flatMap(x => {
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
          return addTopLevelElement(key, asArray, TopSorting.BY_YEAR)
        })

    makeAccordion(result)
  }

  function sortByMissingAlbumsCount() {
    const albumsByMissingAlbumsThenByArtist = albumsByArtist.custom_group_by(e => e.albums.length)
    const result = Object.entries(albumsByMissingAlbumsThenByArtist)
        .custom_sort_by(e => parseInt(e[0]))
        .flatMap(e => {
          const [missingAlbumCount, albumsByArtist] = e
          const sortedByDate =
              albumsByArtist.custom_sort_by(e => e.albums.map(e => e.date).custom_max()).reverse()
          return addTopLevelElement(missingAlbumCount, sortedByDate, TopSorting.BY_MISSING_ALBUMS)
        })

    makeAccordion(result)
  }

  /** Top-level button utility functions. */
  const TopSorting = {
    // TODO replace this "enum" with proper classes/ADTs, since switch casing on it is fugly.
    BY_GENRE: "BY_ARTIST",
    BY_YEAR: "BY_GENRE",
    BY_MISSING_ALBUMS: "BY_ARTIST_FROM_MISSING",
  }

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

  function addTopLevelElement(key, entries, topSorting) {
    function addEntry(entryName, albums, genre) {
      const albumsElem = elem("ol")
      const mkEntryElem = () => {
        switch (topSorting) {
          case TopSorting.BY_YEAR:
          case TopSorting.BY_GENRE:
            return elem("li", `${entryName} `)
          case TopSorting.BY_MISSING_ALBUMS:
            return elem("li", `${entryName} (${genre}) `)
          default:
            throw new AssertionError()
        }
      }
      const entryElem = mkEntryElem()
      switch (topSorting) {
        case TopSorting.BY_YEAR:
          entryElem
              .append(button("Ignore", "ignore-artist"))
              .append(button("Remove", "remove-artist"))
              .append(createHideButton())
              .data("artistName", entryName)
          break
        case TopSorting.BY_GENRE:
          break
      }
      entryElem
          .append(albumsElem)

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

      for (const album of albums) {
        function value() {
          switch (topSorting) {
            case TopSorting.BY_YEAR:
              return `[${album.albumType}] ${album.artistName} - ${album.title} (${toIsoDate(new Date(album.date))}) `
            case TopSorting.BY_GENRE:
            case TopSorting.BY_MISSING_ALBUMS:
              return `[${album.albumType}] ${album.title} (${toIsoDate(new Date(album.date))}) `
            default:
              throw new AssertionError()
          }
        }
        function artistName() {
          switch (topSorting) {
            case TopSorting.BY_YEAR:
              return album.artistName
            case TopSorting.BY_GENRE:
            case TopSorting.BY_MISSING_ALBUMS:
              return entryName
            default:
              throw new AssertionError()
          }
        }
        elem("li", value())
            .data({
              "artistName": artistName(),
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
    const elementDiv = div()
    for (let i = 0; i < entries.length; i++) {
      const o = entries[i]
      addEntry(o.name, o.albums, o.genre).appendTo(elementDiv)
    }
    return [$(`<h5>${key}</h5>`), elementDiv]
  }

  /** Per entry buttons **/
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
