$(function() {
  var randomSongUrl = "data/randomSong";
  // get a random start to start off with
  $.get(randomSongUrl, function(data) {
    // setup playlist type
    playlist = new jPlayerPlaylist({
      jPlayer : "#jquery_jplayer_1",
      cssSelectorAncestor : "#jp_container_1"
    }, [ data ], {
      swfPath : "../js",
      supplied : "webmv, ogv, m4a, oga, mp3, flac"
    });
    $("#jp_poster_0").addClass("poster");
    $("#jquery_jplayer_1").addClass("poster");
    // $("#jp_poster_0").width("270px");
    // $("#jp_poster_0").css("margin-left", "105px");
    // $("body").css("background-color", "#EEEEEE");
    if (mute) // mute for debugging
      $(".jp-mute").click()
    else
      $(".jp-volume-max").click();
    $(".jp-shuffle").click();
    // save a reference to the old next method before changing it
    playlist.oldNext = playlist.next;
    // same as the old, but loads a new random song if in shuffle mode and
    // at the end
    playlist.next = function() {
      if (this.shuffled && this.isLastSongPlaying()) {
        loadNextRandom(true);
      } else {
        playlist.oldNext();
      }
    };
    var jPlayer = $(playlist.cssSelector.jPlayer).data("jPlayer");
    jPlayer.onPlay = function() {
      var currentlyPlayingSong = playlist.currentPlayingSong();
      var songInfo = currentlyPlayingSong.artist + " - " + currentlyPlayingSong.title;
      $(".jp-currently-playing").html(songInfo);
      document.title = songInfo;
      $('#favicon').remove();
      $('head').append('<link href="' + $("img.poster")[0].src+ '" id="favicon" rel="shortcut icon">');
    };
  });

  String.prototype.format = String.prototype.f = function() {
    var s = this, i = arguments.length;

    while (i--)
      s = s.replace(new RegExp('\\{' + i + '\\}', 'gm'), arguments[i]);
    return s;
  };
  var lastClick = 0;

  $(".jp-currently-playing").css({
    width : '300px',
    'font-size' : '0.65em',
    color : $('.jp-duration').css('color'),
    'float' : 'left'
  });

  var loadNextRandom = function(playNow) {
    $.get(randomSongUrl, function(data) {
      playlist.add(data, playNow);
    });
  };

  setInterval(function() {
    var jPlayer = $(playlist.cssSelector.jPlayer).data("jPlayer");
    if (playlist.shuffled
        && playlist.playlist.length - 1 == playlist.current) {
      var duration = jPlayer.htmlElement.media.duration;
      var currentTime = jPlayer.htmlElement.media.currentTime;
      if (duration - currentTime < WAIT_DELAY) {
        loadNextRandom(false);
      }
    }
  }, (WAIT_DELAY - 5) * 1000);
});
