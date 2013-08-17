$(function() {
	function openConnection() {
		var treeConnection = new WebSocket("ws://" + window.location.host + "/ws/console")
		treeConnection.onopen = function() {
			console.log("Console connection opened");
		};
		treeConnection.onmessage = function(msg) {
			console.log("(Server)" + msg.data)
		};
		treeConnection.onclose = function() {
			console.log("Console connection closed, attempting reopen...");
			openConnection(); // reopen connection if server has reloaded
		};
	}
	openConnection();
});
