package decoders

import net.codingwell.scalaguice.ScalaModule

object EncoderModule extends ScalaModule{
  override def configure(): Unit = {
    bind[Encoder] toInstance DbPowerampCodec
  }
}
