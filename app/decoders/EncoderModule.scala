package decoders

import net.codingwell.scalaguice.ScalaModule

object EncoderModule extends ScalaModule{
  override def configure(): Unit = {
    bind[Mp3Encoder] toInstance DbPowerampCodec
  }
}
