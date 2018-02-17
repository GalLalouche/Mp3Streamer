package models

trait IOMusicFinderProvider extends MusicFinderProvider {
  override def mf: IOMusicFinder
}
