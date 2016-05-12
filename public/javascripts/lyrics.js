$(function() {
    function showLyrics(lyrics) {
        $('#lyrics').html(lyrics)
    }
    Lyrics = {}
    Lyrics.show = function(s) {
        showLyrics("Loading lyrics...")
        $.get("lyrics/" + s.file, l => showLyrics(l))
    }
})
