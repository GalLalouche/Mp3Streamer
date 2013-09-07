package common.path

import java.io.File
import java.io.PrintStream
import java.util.Scanner

class RichFile(val f: File) extends Path(f) {

	lazy val extension = {
		val i = p.getName.lastIndexOf('.')
		if (i == -1) "" else p.getName.substring(i + 1).toLowerCase
	}

	import resource._
	def write(s: String) {
		for (ps <- managed(new PrintStream(f)))
			ps.println(s)
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
}

object RichFile {
	implicit def poorFile(f: RichFile): File = f.f
	implicit def richFile(f: File): RichFile = new RichFile(f)

	def apply(f: File) = new RichFile(f)
}

