$(function() {
	openConnection("newFolder", function(msg) {
		// in case pop blocker is disabled, fuck that and just add the folder
		if (!window.confirm || window.confirm("Found new folder(" + msg.data + "), would you like to add it to the playlist?"))
			$.get("/music/albums/" + msg.data, function(data) {
				playlist.add(data, false);
			});
	});
});
