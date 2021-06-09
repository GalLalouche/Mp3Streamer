package mains.cover

import com.google.inject.assistedinject.Assisted

import common.concurrency.FutureIterant

private trait AsyncFolderImagePanelFactory {
  def apply(
      images: FutureIterant[FolderImage],
      @Assisted("rows") rows: Int,
      @Assisted("cols") cols: Int,
  ): AsyncFolderImagePanel
}
