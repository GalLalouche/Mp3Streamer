package common.io

import java.io.{ByteArrayOutputStream, InputStream}


object RichStream {
  implicit class RichStream(val is: InputStream) {
    require(is != null)
    
    def readAll: String = scala.io.Source.fromInputStream(is).getLines().mkString("\n")
    
    def getBytes: Array[Byte] = {
      val buffer = new ByteArrayOutputStream()
      
      var nRead = 0
      val data = new Array[Byte](16384)
      
      var b = true
      while (b) {
        nRead = is.read(data, 0, data.length)
        if (nRead == -1)
          b = false
        else
          buffer.write(data, 0, nRead)
      }
      
      buffer.flush()
      
      buffer.toByteArray
    }
  }
}
