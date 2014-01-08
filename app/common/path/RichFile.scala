package common.path

import java.io.File
import java.io.PrintStream
import java.util.Scanner
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.awt.Desktop
import scala.io.Codec
import java.nio.charset.CodingErrorAction
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.io.FileWriter

class RichFile(val f: File) extends Path(f) {

	lazy val extension = {
		val i = p.getName.lastIndexOf('.')
		if (i == -1) "" else p.getName.substring(i + 1).toLowerCase
	}

	import resource._
	def appendLine(s: String) {
		for (fw <- managed(new FileWriter(f, true)))
			fw.append(s + "\n")
	}

	def clear() {
		f.delete
		f.createNewFile
	}

	def write(baos: ByteArrayOutputStream) {
		for (os <- managed(new FileOutputStream(f)))
			baos.writeTo(os)
	}

	def write(bytes: Array[Byte]) {
		for (os <- managed(new FileOutputStream(f)))
			os.write(bytes)
	}

	def readAll: String = {
		managed(new Scanner(f).useDelimiter("\\Z")).acquireAndGet { s =>
			if (s.hasNext) s.next else ""
		}
	}

	def lines: TraversableOnce[String] = {
		new Traversable[String] {
			override def foreach[U](f: String => U): Unit = {
				managed(new Scanner(RichFile.this.f).useDelimiter(System.getProperty("line.separator"))).acquireAndGet { scanner =>
					while (scanner.hasNext)
						f(scanner.next)
				}
			}
		}
	}

	def openWithDefaultApplication {
		Desktop.getDesktop.open(f)
	}

	def readBytes(): Seq[Byte] = {
		IOUtils.toByteArray(new FileInputStream(f))
	}

	def hasSameContentAs(f: File) = {
		readBytes == new RichFile(f).readBytes
	}
}

object RichFile {
	implicit def poorFile(f: RichFile): File = f.f
	implicit def richFile(f: File): RichFile = new RichFile(f)

	def apply(f: File) = new RichFile(f)
	def apply(s: String): RichFile = new RichFile(new File(s))
}

