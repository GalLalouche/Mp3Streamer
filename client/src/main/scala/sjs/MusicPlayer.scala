package sjs

import scala.scalajs.js

trait MusicPlayer {
  def loadUrl(url: String)
  def play()
  def pause()
  def stop()
}

object AuroraPlayer extends MusicPlayer {
  var currentPlayer: Player = null
  def loadUrl(url: String) {
    currentPlayer = AV.Player.fromURL(url)
    def callback(x: Player, y: Any) {
      currentPlayer.play()
    }
    currentPlayer.preload()
    currentPlayer.on("ready", callback _)
  }
  override def play() {currentPlayer.play()}
  override def stop() {currentPlayer.stop()}
  override def pause() {currentPlayer.pause()}
}
