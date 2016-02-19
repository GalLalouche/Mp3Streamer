package mains

import common.rich.path.RichFile

// won't work for directories... FOR NOW! muhaha
object RegeditMaker {
	def apply(text: String, filetype: String, jars: TraversableOnce[String], main: String, outputName: String) {
		val regeditPath = s"HKEY_CURRENT_USER\\Software\\Classes\\$filetype\\shell\\${text.replaceAll("\\s+", "")}"
		List("Windows Registry Editor Version 5.00\n",
			s"[-$regeditPath]\n",
			s"[$regeditPath]",
			s"""@="$text"\n""",
			s"[$regeditPath\\command]",
			s"""@="cmd.exe /d /c java.exe -cp ${jars mkString ";"} $main \\\"%1\\\"""""
		).foreach(RichFile.create(outputName, "reg").clear.appendLine)
	}

	def main(args: Array[String]) {
		val jars = Set(
			"C:/dev/web/play-2.1.2/repository/local/commons-logging/commons-logging/1.1.1/jars/commons-logging.jar",
			"C:/dev/lang/scala/lib/scala-library.jar",
			"C:/dev/web/play-2.1.2/repository/local/org.apache.httpcomponents/httpclient/4.0.1/jars/httpclient.jar",
			"C:/dev/web/play-2.1.2/repository/cache/org.jsoup/jsoup/jars/jsoup-1.8.1.jar",
			"C:/dev/web/play-2.1.2/repository/local/org.apache.httpcomponents/httpcore/4.0.1/jars/httpcore.jar",
			"C:/dev/web/play-2.1.2/repository/local/play/play_2.10/2.1.2/jars/play_2.10.jar",
			"C:/dev/web/play-2.1.2/Mp3Streamer/lib/jaudiotagger-2.0.4-20130131.171158-16.jar",
			"C:/dev/web/play-2.1.2/repository/local/joda-time/joda-time/2.1/jars/joda-time.jar",
			"C:/dev/web/play-2.1.2/repository/local/org.codehaus.jackson/jackson-core-asl/1.9.10/jars/jackson-core-asl.jar",
			"C:/dev/web/play-2.1.2/repository/local/org.codehaus.jackson/jackson-mapper-asl/1.9.10/jars/jackson-mapper-asl.jar",
			"C:/dev/web/play-2.1.2/repository/local/commons-io/commons-io/2.0.1/jars/commons-io.jar",
			"D:/FolderFixer.jar",
			"C:/dev/git/scala-common/ScalaCommon/ScalaCommon.jar")
		this.apply(text = "Extract flac files",
			filetype = "cue_auto_file",
			jars = jars,
			outputName = "d:/Unflacer",
			main = "mains.UnflacerJava")
	}
}