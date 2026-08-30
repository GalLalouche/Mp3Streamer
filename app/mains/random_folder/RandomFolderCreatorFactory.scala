package mains.random_folder

import songs.selector.ConfigurableSongSelector

private trait RandomFolderCreatorFactory {
  def create(ss: ConfigurableSongSelector): RandomFolderCreator
}
