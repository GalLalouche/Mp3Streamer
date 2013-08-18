function openConnection(path, onMessage) {
	var connection = new WebSocket("ws://" + window.location.host + "/ws/" + path);
	connection.onopen = function() {
		console.log(path + " connection opened");
	};
	connection.onmessage = onMessage; 
	connection.onclose = function() {
		console.log(path + " connection closed, attempting reopen...");
		openConnection(path, onMessage);
	};
	return connection;
}
