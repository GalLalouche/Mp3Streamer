package models

import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

import common.io.RootDirectory

class IOMusicFinderModule(mf: => IOMusicFinder) extends ScalaModule {
  @Provides private def musicFinder(mf: IOMusicFinder): MusicFinder = mf
  @Provides private def musicFinder: IOMusicFinder = mf
}
object IOMusicFinderModule extends IOMusicFinderModule(IOMusicFinder)

