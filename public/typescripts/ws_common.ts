interface WebSocket {
  reconnect(): void
}

function custom_openConnection(
  path: string,
  onMessage: (s: MessageEvent, connection: WebSocket) => void,
  autoReconnectOnClose: boolean = false,
  autoConnectionInterval: number = 10000,
): WebSocket {
  const result = new WebSocket("ws://" + window.location.host + "/ws/" + path)
  result.onopen = function () {
    // console.log(path + " connection opened");
  }
  result.onmessage = e => onMessage(e, result)
  result.onclose = function (event) {
    switch (event.code) {
      case 1000:
        // console.log(path + " explicitly closed connection")
        break
      default:
        // console.log(path + " connection closed for some reason")
        // console.log(event)
        if (autoReconnectOnClose)
          result.reconnect()
    }
  }
  result.reconnect = function () {
    setTimeout(function () {
      // console.log("Retrying connection...")
      const new_connection = new WebSocket(result.url)
      new_connection.onopen = result.onopen
      new_connection.onmessage = result.onmessage
      new_connection.onclose = result.onclose
      new_connection.reconnect = result.reconnect
      new_connection.onerror = result.onerror
      new_connection.send = result.send
    }, autoConnectionInterval)
  }
  return result
}
