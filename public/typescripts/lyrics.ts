$(function () {
    const lyricsDiv = $('#lyrics')
    // Wraps the actual lyrics with an extra div. Ensures the scroll bar is always on the right regardless of
    // text direction, and that there is a margin between the lyrics with the bar. Also ensures the bar is only
    // on the lyrics, not the buttons and text box.
    const lyricBox = div({id: 'lyric-box'}).appendTo(lyricsDiv)
    const lyricsContent = div({id: 'lyric-contents'}).appendTo(lyricBox)
    lyricsDiv.appendBr()
    const lyricsPusher = div().appendTo(lyricsDiv)
    const lyricsUrlBox = $("<input id='lyrics-url' placeholder='Lyrics URL' type='text'/>").appendTo(lyricsPusher)
    const updateLyricsButton = button("Update lyrics").appendTo(lyricsPusher)
    const instrumentalSongButton = button("Instr. Song").appendTo(lyricsPusher)
    const instrumentalArtistButton = button("Instr. Artist").appendTo(lyricsPusher)
    validateBoxAndButton(lyricsUrlBox, updateLyricsButton, isValidUrl, updateLyrics)

    lyricsUrlBox.keyup(function () { // the validateBoxAndButton only applies to updateLyricsButton
        const box = $(this)

        function disable(b: JQuery<HTMLElement>): void {
            b.prop('disabled', box.val() !== "")
        }

        disable(instrumentalArtistButton)
        disable(instrumentalSongButton)
    })

    function setInstrumental(type: string): void {
        $.post(`lyrics/instrumental/${type}/${gplaylist.currentPlayingSong().file}`, s => showLyrics(s as string))
    }

    instrumentalSongButton.click(() => setInstrumental("song"))
    instrumentalArtistButton.click(() => setInstrumental("artist"))

    function updateLyrics() {
        const url = lyricsUrlBox.val() as string
        const songPath = gplaylist.currentPlayingSong().file
        $.ajax(
            "lyrics/push/" + songPath,
            {
                type: "POST",
                data: url,
                success: c => showLyrics(c),
                error: c => showLyrics(c.statusText),
                contentType: "text/plain",
            })
        clearButtons()
        lyricsContent.html("Pushing lyrics...")
    }

    function clearButtons() {
        lyricsUrlBox.val("")
        lyricsUrlBox.trigger("keyup")
    }

    const HEBREW_REGEX = /[\u0590-\u05FF]/

    let previousContent = ""

    function showLyrics(content: string): void {
        clearButtons()
        autoScroll = true
        lyricsContent.html(content)
        const hasHebrew = content.search(HEBREW_REGEX) >= 0
        // The "direction" property also changes the overflow ruler; fuck that.
        lyricsContent.css("direction", hasHebrew ? "rtl" : "ltr")
        if (previousContent === lyricsContent.html()) // Ensure the same HTML formatting is used for comparison
            return // HTML wasn't changed, so don't reset the baselines
        previousContent = lyricsContent.html() // Ensure the same HTML formatting is used for comparison
        scrollBaseline = 0
        timeBaseline = 0
    }

    let autoScroll = false
    let scrollBaseline = 0
    let timeBaseline = 0

    const scrollableElement = lyricBox

    function scrollLyrics() {
        // Don't start scrolling right at the beginning of the song if there is no baseline set
        const heightBaseline = scrollBaseline || (lyricsContent.height()! / -4)
        const timePercentage = (gplayer.percentageOfSongPlayed() - timeBaseline) / 100.0
        autoScroll = true
        scrollableElement.scrollTop(scrollableElement.prop('scrollHeight') * timePercentage + heightBaseline)
    }

    setInterval(scrollLyrics, 100)

    scrollableElement.scroll(function () { // When the user scrolls manually, reset the baselines
        if (autoScroll.isFalse()) {
            scrollBaseline = scrollableElement.scrollTop()!
            timeBaseline = scrollBaseline && gplayer.percentageOfSongPlayed()
        }
        autoScroll = false
    })

    class Lyrics {
        show(song: Song): void {
            clearButtons()
            autoScroll = true
            lyricsContent.html("Fetching lyrics...")
            $.get("lyrics/" + song.file, function (l) {
                showLyrics(l)
                scrollLyrics()
            })
        }
    }

    (window as any).Lyrics = new Lyrics()
})
