function openConnection(path, onMessage, autoReconnectOnClose, autoConnectionInterval) {
  const connection = new WebSocket("ws://" + window.location.host + "/ws/" + path);
  connection.onopen = function() {
    // console.log(path + " connection opened");
  };
  connection.onmessage = e => onMessage(e, connection);
  connection.onclose = function(event) {
    switch (event.code) {
      case 1000:
        // console.log(path + " explicitly closed connection")
        break
      default:
        // console.log(path + " connection closed for some reason")
        // console.log(event)
        if (autoReconnectOnClose)
          connection.reconnect()
    }
  }
  connection.reconnect = function() {
    setTimeout(function() {
      // console.log("Retrying connection...")
      const new_connection = new WebSocket(connection.url)
      new_connection.onopen = connection.onopen
      new_connection.onmessage = connection.onmessage
      new_connection.onclose = connection.onclose
      new_connection.reconnect = connection.reconnect
      new_connection.onerror = connection.onerror
      new_connection.send = connection.send
    }, autoConnectionInterval || 10000)
  }
  return connection;
}
