// Since jplayer.playlist.js is too freaking big, this extracts my customization
$(function() {
  /* Setup events */
  const playlistElement = $(".jp-playlist")
  const playlist = gplaylist
  const playlistItem = "> ul > li"
  // Display full song info on hover when the text overflows.
  playlistElement.on("mouseover", playlistItem, function() {
    const listItem = $(this)
    // The listItem can't overflow; what can overflow is the width-limited descendent.
    if (listItem.find(".width-limited-playlist-span").custom_overflown()) {
      const displayedIndex = playlist.getDisplayedIndex(listItem.index())
      const song = playlist.songs()[displayedIndex]
      listItem.custom_tooltip(playlist.toString(song))
    }
  })
  // Move to song on click.
  playlistElement.on("click", playlistItem, function(e) {
    if (e.target.localName !== "span" && e.target.localName !== "img")
      return // Only listens to clicks on the text or poster image, to avoid handling misclicks near the buttons.
    const listItem = $(this)
    const clickedIndex = playlist.getDisplayedIndex(listItem.index())
    if (gplaylist.currentIndex() === clickedIndex)
      return // Clicked song is currently playing.
    playlist.play(clickedIndex)
  })

  /* Metadata stuff */
  const isClassicalPiece = media => !!media.composer
  playlistUtils.isClassicalPiece = isClassicalPiece
  function additionalData(media) {
    if (isClassicalPiece(media).isFalse())
      return [
        `${media.albumName}${media.discNumber ? "[" + media.discNumber + "]" : ""}`,
        media.track,
        media.year,
      ]

    const titleContainsComposer = media.albumName.toLowerCase().includes(media.composer.toLowerCase())
    const pieceTitle = titleContainsComposer ? media.albumName : `${media.composer}'s ${media.albumName}`
    const opusSuffix = media.opus ? `, ${media.opus}` : ''
    return [
      pieceTitle + opusSuffix,
      media.year,
      media.conductor,
      media.orchestra,
      media.performanceYear,
      media.track,
    ].filter(x => x)
  }

  function formattedMetadata(media) {
    const res = additionalData(media)
    const head = `<span dir="ltr">${res[0]}</span>`
    res.shift()
    res.push(media.bitrate + "kbps")
    return `${head}, <span dir="ltr">${res.join(", ")}</span>`
  }

  playlistUtils.mediaMetadata = media => [
    media.title,
    media.artistName,
  ].concat(additionalData(media)).concat([
    media.duration.timeFormat(),
  ]).join(", ")

  playlistUtils.mediaMetadataHtml = function(media) {
    const metadata =
        `<span class="jp-artist" dir="ltr">${media.artistName}</span> ` +
        `(<span class="jp-parens">${formattedMetadata(media)}</span>`

    // Duration is appended manually outside of metadata to ensure that it is always displayed, even if
    // metadata overflows. That's the reason for the odd parens too.
    return `<span class='${this.options.playlistOptions.itemClass}' tabindex='1'>
              <span class="width-limited-playlist-span">
                <span class="jp-title">${media.title}</span> <span class="jp-metadata">${metadata}</span>
              </span>
              <span class="jp-list-duration">, ${media.duration.timeFormat()})</span>
            </span>`
  }
})

playlistUtils = {}
