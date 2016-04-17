package sjs

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global

import scala.scalajs.js.JSApp
import org.scalajs.jquery.jQuery

object PlayDemo extends JSApp {
  val player = AuroraPlayer
  val $ = jQuery
  @scala.scalajs.js.annotation.JSExport
  def addButton(text: String)  = {
    val id = "b-" + text
    $("body").append(s"<button id='$id'>$text</button>")
    $("#" + id)
  }
  override def main() {
    def start() {player.loadUrl("/flac/raw")}
    def stop() {player.stop()}
    def play() {player.play()}
    def pause() {player.pause()}
    addButton("start").on("click", start _)
    addButton("play").on("click", play _)
    addButton("pause").on("click", pause _)
    addButton("stop").on("click", stop _)
//    $("body").append("<button id='bPlay'>play</button>").on("click", playClick _)
//    $("body").append("<button id='bPause'>pause</button>").on("click", pauseClick _)
//    $("body").append("<button id='bStop'>stop</button>").on("click", stopClick _)
//    $("body").append("<button id='bStart'>start</button>").on("click", start _)
//
  }
}
