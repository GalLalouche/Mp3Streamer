package controllers
import java.io.File

import common.Debug
import play.api.mvc.{Action, Controller}

class Flac extends Controller with Debug {
  def raw() = Action {
//    Ok.sendFile(new File("""D:\Media\Music\Rock\Neo-Prog\Beardfish\2012 The Void\09 - Note.flac"""))
    Ok.sendFile(new File("""D:\Media\Music\Rock\Neo-Prog\Transatlantic\2000 SMTPe\03 - Mystery Train.mp3"""))
  }
  def foobar = Action {
    Ok(views.html.flac())
  }
}
