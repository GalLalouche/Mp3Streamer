function openConnection(path, onMessage) {
  const connection = new WebSocket("ws://" + window.location.host + "/ws/" + path);
  connection.onopen = function() {
    console.log(path + " connection opened");
  };
  connection.onmessage = onMessage;
  connection.onclose = function(event) {
    switch (event.code) {
      case 1000:
        console.log(path + " explicitly closed connection")
        break
      default:
        console.log(path + " connection closed for some reason")
        console.log(event)
    }
  }
  return connection;
}
