package sjs

import scala.scalajs.js

//var p = AV.Player.fromURL("/flac/raw")
//p.preload()
//p.on('ready', function() {
//console.log('ready');
//
//// Aurora doesn't create the audio context until you start playing it.
//p.play();
//window.setTimeout(function() { p.stop() }, 5000);
//})
@js.native
object AV extends js.Object {
  def Player: AVPlayer = js.native
}
@js.native
trait AVPlayer extends js.Object {
  def fromURL(s: String): Player = js.native
}
@js.native
trait Player extends js.Object {
  def preload(): Unit = js.native
  def play(): Unit = js.native
  def pause(): Unit = js.native
  def stop(): Unit = js.native
  def on(s: String, f: js.Function2[Player, js.Array[js.Dynamic], Unit]): Unit = js.native

}
