package controllers

import java.io.File
import java.net.URLDecoder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import common.path.Directory
import decoders.DbPowerampCodec
import decoders.Mp3Decoder
import models.Decoder
import play.api.mvc.Action
import play.api.mvc.Controller

object Streamer extends Controller with Decoder with DbPowerampCodec {
	val codecPath = "c:/program files (x86)/Illustrate/dBpoweramp/CoreConverter.exe"
	val outputDir = Directory("D:/media/streamer/musicOutput")
	
	def download(s: String) = Action {
		val futureFile = Future { decodeFileIfNeeded(new File(URLDecoder.decode(s, "UTF-8"))) }  // this ensures this call isn't blocking
		Async {
			futureFile.map { file =>
				loggers.CompositeLogger.trace("Sending file " + file.getAbsolutePath)
				Status(200).sendFile(file).withHeaders(("Content-length", file.length.toString), ("Accept-Ranges", "bytes"),
					("X-Pad", "avoid browser bug"), ("Content-Transfer-Encoding", "binary"), ("Cache-Control", "no-cache"),
					("Content-Disposition", "attachment; filename=" + file.getName().replaceAll(",", "%2c")),
					("Content-Range", "byte %d/%d".format(file.length, file.length)), ("Content", "audio/mp3"))

			}
		}
	}

	def playSong(s: String) = Action {
		Ok(views.html.playSong("/music/songs/" + s))
	}
}