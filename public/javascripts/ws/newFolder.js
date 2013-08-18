$(function() {
	openConnection("newFolder", function(msg) {
		if (window.confirm("Found new folder(" + msg.data + "), would you like to add it to the playlist?"))
			$.get("/music/albums/" + msg.data, function(data) {
				playlist.add(data, false);
			});
	});
});
