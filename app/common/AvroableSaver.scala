package common

import java.io.File
import java.util.regex.Pattern

import com.google.inject.Inject
import org.apache.avro.file.{DataFileReader, DataFileWriter, SeekableByteArrayInput}
import org.apache.avro.generic.{GenericDatumReader, GenericDatumWriter, GenericRecord}

import scala.util.{Try, Using}

import common.rich.func.kats.ToMoreFoldableOps.EitherFoldableOps

import common.io.{DirectoryRef, RootDirectory}
import common.io.avro.{Avroable, AvroReadable}
import common.rich.primitives.RichString._

class AvroableSaver @Inject() (@RootDirectory rootDirectory: DirectoryRef) {
  private def workingDir = rootDirectory.addSubDir("data").addSubDir("avro")
  private def avroFileName[T: Manifest]: String =
    s"${manifest.runtimeClass.getSimpleName.removeAll(AvroableSaver.TrailingSlashes)}s.avro"
  val path = new File("D:\\temp\\songs.avro")
  def save[A: Avroable: Manifest](values: IterableOnce[A]): Unit =
    saveExplicit(values, avroFileName[A])
  def saveExplicit[A: Avroable: Manifest](values: IterableOnce[A], fileName: String): Unit = {
    val avro = Avroable[A]
    val schema = avro.schema
    Using.resource(
      new DataFileWriter[GenericRecord](new GenericDatumWriter[GenericRecord](schema)),
    ) { writer =>
      writer.create(schema, workingDir.addFile(fileName).outputStream)
      values.iterator.foreach(writer append avro.toRecord(_))
    }
  }
  def exists[T: Manifest]: Boolean = workingDir.getFile(avroFileName[T]).isDefined
  def load[A: AvroReadable: Manifest]: Seq[A] = load[A](avroFileName[A])
  def load[A: AvroReadable: Manifest](path: String): Vector[A] = {
    val file = workingDir.getFile(path) match {
      case Some(value) => value
      case None => return Vector.empty
    }
    val avro = AvroReadable[A]
    Using.resource(
      new DataFileReader[GenericRecord](
        new SeekableByteArrayInput(file.bytes),
        new GenericDatumReader[GenericRecord](avro.schema),
      ),
    ) { reader =>
      val builder = Vector.newBuilder[A]
      reader.forEach(r => builder += avro.fromRecord(r))
      builder.result()
    }
  }
  def loadArrayHandleErrors[T: AvroReadable: Manifest]: (Seq[T], Seq[Throwable]) = {
    val res = load[Try[T]](avroFileName[T]).map(_.toEither.swap)
    res.partitionEithers(leftSizeHint = 0, rightSizeHint = res.size)
  }
}
private object AvroableSaver {
  private val TrailingSlashes = Pattern.compile("""\$""")
}
