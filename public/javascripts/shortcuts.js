$(function() {
	jPlayer.togglePlay = function() {
		var jPlayer = $('#jquery_jplayer_1').data().jPlayer;
		if (jPlayer.status.paused) 
			click("play");
		else 
			click("pause");
	}
	$(document).keyup(function(e) {
		var jPlayer = $('#jquery_jplayer_1').data().jPlayer;
		var code = e.keyCode || e.which;
		var letter = String.fromCharCode(e.which);
		switch (letter) {
		case 'Z':
			if (playlist.current) {
				click("previous");				
			} else {
				click("stop");
				click("play");
			}
			break;
		case 'X':
			if (jPlayer.status.paused) {
				click("play");
			} else {
				click("stop");
				click("play");
			}
			break;
		case 'C':
			if (jPlayer.status.paused) {
				click("play");
			} else {
				click("pause");
			}
			break;
		case 'V':
			click("stop");
			break;
		case 'B':
			click("next");
			break;
		}
	});
	
	function click(what) {
		$(".jp-" + what).click();
	}
});
