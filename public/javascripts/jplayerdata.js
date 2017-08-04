$(function() {
  const randomSongUrl = "data/randomSong";
  // Init playlist
  playlist = new jPlayerPlaylist({
    jPlayer: "#jquery_jplayer_1",
    cssSelectorAncestor: "#jp_container_1"
  }, [], {
    swfPath: "../js",
    supplied: "webmv, ogv, m4a, oga, mp3, flac"
  });
  // Modify next to fetch a random song if in shuffle mode and at the last song
  playlist.oldNext = playlist.next;
  const shouldLoadNextSongFromRandom = () => playlist.isLastSongPlaying()
  playlist.next = function() {
    if (shouldLoadNextSongFromRandom())
      loadNextRandom(true);
    else
      playlist.oldNext();
  };
  // On play event hook
  // TODO don't call if the same song?
  $(playlist.cssSelector.jPlayer).data("jPlayer").onPlay = function() {
    const currentlyPlayingSong = playlist.currentPlayingSong();
    const songInfo = `${currentlyPlayingSong.artistName} - ${currentlyPlayingSong.title}`;
    $(".jp-currently-playing").html(songInfo);
    document.title = songInfo;
    $('#favicon').remove();
    $('head').append(`<link href="${$("img.poster")[0].src}" id="favicon" rel="shortcut icon">`);
    Lyrics.show(currentlyPlayingSong)
    External.show(currentlyPlayingSong)
    Volume.setPeak(currentlyPlayingSong)
  };
  // Poster classes
  const poster = $("#jp_poster_0")
  poster.addClass("poster");
  $("#jquery_jplayer_1").addClass("poster");
  // Mute for debugging if requested
  if (mute)
    $(".jp-mute").click()
  else
    $(".jp-volume-max").click();

  function loadNextRandom(playNow) {
    $.get(randomSongUrl, function(data) {
      playlist.add(data, playNow);
    });
  }

  loadNextRandom(true)
  // Fetches new songs before current song ends.
  setInterval(function() {
    const media = $(playlist.cssSelector.jPlayer).data("jPlayer").htmlElement.media;
    const isSongNearlyFinished = media.duration - media.currentTime < WAIT_DELAY;
    if (shouldLoadNextSongFromRandom() && isSongNearlyFinished)
      loadNextRandom(false);
  }, (WAIT_DELAY - 5) * 1000);

  // Technically CSS, but can't be done with pure css it seems.
  poster.css("margin-left", (($(".jp-video").width() - poster.width()) / 2) + "px")
});
