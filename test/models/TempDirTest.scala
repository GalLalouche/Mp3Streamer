//package models
//
//import java.io.File
//import org.specs2.mutable.SpecificationLike
//import org.specs2.specification.Scope
//import common.Debug
//import common.path.
//import org.specs2.mock.Mockito
//
//trait TempDirTest extends SpecificationLike with Debug with Mockito {
//
//	class TempDir extends Scope {
//		val tempDir: TempDirectory = TempDirectory(locally {
//			val f = File.createTempFile("spec" + getClass.getSimpleName, "temp")
//			f.delete
//			f.mkdir
//			f
//		})
//	}
//}