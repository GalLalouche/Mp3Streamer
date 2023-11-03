package mains.random_folder

import common.io.IOSystem
import songs.selector.MultiStageSongSelector

private trait RandomFolderCreatorFactory {
  def create(ss: MultiStageSongSelector[IOSystem]): RandomFolderCreator
}
