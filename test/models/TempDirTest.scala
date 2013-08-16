package models

import java.io.File

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import common.TempDirectory

trait TempDirTest extends Specification {

	class TempDir extends Scope {

		val tempDir: TempDirectory = TempDirectory(locally {
			val f = File.createTempFile(getClass.getSimpleName, "temp")
			f.delete
			f.mkdir
			f
		})
	}
}