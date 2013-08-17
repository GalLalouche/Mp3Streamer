$(function() {
	function openConnection() {
		var treeConnection = new WebSocket("ws://" + window.location.host + "/ws/newFolder")
		treeConnection.onopen = function() {
			console.log("New Folder connection opened");
		};
		treeConnection.onmessage = function(msg) {
			console.log("new album found " + msg.data)
			$.get("/music/albums/" + msg.path, function(data) {
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
