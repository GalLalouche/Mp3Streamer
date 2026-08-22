package songs.selector.filter.sublinear

import net.codingwell.scalaguice.ScalaModule

private[filter] object SublinearScalingModule extends ScalaModule {
  override def configure(): Unit = {
    bind[ArtistQuantifier].to[NumberOfAlbumsCounter]
    bind[Double].annotatedWithName(SublinearScalingPercentage.ScalingFactor).toInstance(0.5)
    bind[Double].annotatedWithName(SublinearScalingPercentage.DampingFactor).toInstance(3)
  }
}
