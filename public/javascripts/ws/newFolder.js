$(function() {
	function openConnection() {
		var treeConnection = new WebSocket("ws://" + window.location.host + "/ws/newFolder")
		treeConnection.onopen = function() {
			console.log("New Folder connection opened");
		};
		treeConnection.onmessage = function(msg) {
			if (window
					.confirm("Found new folder, would you like to add it to the playlist?"))
				$.get("/music/albums/" + msg.data, function(data) {
					playlist.add(data, false);
				});
		};
		treeConnection.onclose = function() {
			console.log("New Folder connection closed, attempting reopen...");
			openConnection(); // reopen connection if server has reloaded
		};
	}
	openConnection();
});
