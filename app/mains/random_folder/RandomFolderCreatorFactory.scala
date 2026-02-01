package mains.random_folder

import songs.selector.MultiStageSongSelector

import common.path.ref.io.IOSystem

private trait RandomFolderCreatorFactory {
  def create(ss: MultiStageSongSelector[IOSystem]): RandomFolderCreator
}
