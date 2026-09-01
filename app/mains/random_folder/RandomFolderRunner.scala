package mains.random_folder

import jakarta.inject.Inject
import musicfinder.IOSongFileFinder
import songs.selector.ConfigurableSongSelector

import scala.language.postfixOps

import common.path.PathUtils
import common.rich.primitives.RichInt.Rich

private class RandomFolderRunner @Inject() (
    rfcf: RandomFolderCreatorFactory,
    scoreSummarizer: ScoreSummarizer,
    songFileFinder: IOSongFileFinder,
) {
  def go(
      numberOfFolders: Int,
      songsPerFolder: Int,
  )(
      playlistName: String,
      ss: ConfigurableSongSelector,
  ): Unit = {
    scribe.info(s"Scanning for files for $playlistName folder")
    val creator = rfcf.create(ss)
    scribe.info("Choosing songs")
    val songs = creator.dumpFiltered(numberOfFolders * songsPerFolder)
    val outputDir = creator.moveFilteredSongs(playlistName)
    scoreSummarizer.summary(songs)
    for {
      (group, i) <- outputDir.files.grouped(songsPerFolder).zipWithIndex
      outputDirForGroup = outputDir.addSubDir(s"$playlistName ${i.padLeftZeros(2)}")
      song <- group
    } PathUtils.move(song, outputDirForGroup)
    // The last folder will contain the m3u file, seed, etc.
    val lastDir = outputDir.addSubDir(s"$playlistName $numberOfFolders")
    val songsInLastDir = songFileFinder.getSongFilesInDir(lastDir).toVector
    assert(songsInLastDir.isEmpty, s"Found songs in last dir: $songsInLastDir")
    PathUtils.rename(lastDir, "misc.")
  }
}
