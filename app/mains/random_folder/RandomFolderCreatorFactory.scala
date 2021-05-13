package mains.random_folder

private trait RandomFolderCreatorFactory {
  def apply(seed: Long): RandomFolderCreator
}
