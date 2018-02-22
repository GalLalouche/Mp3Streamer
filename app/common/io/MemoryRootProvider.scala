package common.io

trait MemoryRootProvider extends RootDirectoryProvider {
  override def rootDirectory: MemoryRoot
}
