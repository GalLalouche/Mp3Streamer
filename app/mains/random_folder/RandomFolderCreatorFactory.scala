package mains.random_folder

import songs.selector.MultiStageSongSelector

import common.io.IOSystem

private trait RandomFolderCreatorFactory {
  def create(ss: MultiStageSongSelector[IOSystem]): RandomFolderCreator
}
