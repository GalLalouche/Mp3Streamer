$(function () {
  const randomSongUrl = "data/randomSong";
  // get a random song to start off with
  $.get(randomSongUrl, function (data) {
    // setup playlist
    playlist = new jPlayerPlaylist({
      jPlayer: "#jquery_jplayer_1",
      cssSelectorAncestor: "#jp_container_1"
    }, [data], {
      swfPath: "../js",
      supplied: "webmv, ogv, m4a, oga, mp3, flac"
    });
    $("#jp_poster_0").addClass("poster");
    $("#jquery_jplayer_1").addClass("poster");
    if (mute) // mute for debugging
      $(".jp-mute").click()
    else // or full volume for regular
      $(".jp-volume-max").click();
    $(".jp-shuffle").click(); // default to shuffle
    // save a reference to the old next method before changing it
    playlist.oldNext = playlist.next;
    // same as the old, but loads a new random song if in shuffle mode and at the end
    playlist.next = function () {
      if (shouldLoadNextSongFromRandom())
        loadNextRandom(true); 
      else 
        playlist.oldNext();
    };
    $(playlist.cssSelector.jPlayer).data("jPlayer").onPlay = function () {
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
  });
  function loadNextRandom(playNow) {
    $.get(randomSongUrl, function (data) {
      playlist.add(data, playNow);
    });
  };
  function shouldLoadNextSongFromRandom() {
    return playlist.shuffled && playlist.isLastSongPlaying()
  }
  setInterval(function () {
    const jPlayer = $(playlist.cssSelector.jPlayer).data("jPlayer");
    var isSongNearlyFinished = jPlayer.htmlElement.media.duration - jPlayer.htmlElement.media.currentTime < WAIT_DELAY;
    if (shouldLoadNextSongFromRandom() && isSongNearlyFinished)
      loadNextRandom(false);
  }, (WAIT_DELAY - 5) * 1000);
});
