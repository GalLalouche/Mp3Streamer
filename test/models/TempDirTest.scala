package models

import java.io.File
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import common.TempDirectory
import org.specs2.mutable.SpecificationLike
import common.Debug

trait TempDirTest extends SpecificationLike with Debug {

	class TempDir extends Scope {

		val tempDir: TempDirectory = TempDirectory(locally {
			val f = File.createTempFile(getClass.getSimpleName, "temp")
			f.delete
			f.mkdir
			f
		})
	}
}