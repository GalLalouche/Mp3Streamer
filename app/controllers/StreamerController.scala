package controllers

import javax.inject.Inject

import play.api.mvc.InjectedController

import common.rich.RichT._

class StreamerController @Inject() (
    $ : StreamerFormatter,
    converter: PlayActionConverter,
) extends InjectedController {
  def download(path: String) = converter.parse(
    _.toTuple(_.headers.get("Range"), PlayControllerUtils.shouldEncodeMp3),
  ) { case (range, shouldEncode) => $(PlayUrlDecoder(path), range, shouldEncode) }
}
