package server

import java.io.File

import com.google.inject.Module
import sttp.client3.UriContext
import sttp.model.StatusCode

import common.rich.RichFile.richFile

private class AssetTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
  "ts route returns typescript file" in {
    getBytes(uri"/ts/common.ts") shouldEventuallyReturn new File(
      "public/typescripts/common.ts",
    ).bytes
  }

  "ts route returns 404 for nonexistent file" in {
    getRaw(uri"/ts/nonexistent_file_abc123.ts") codeShouldEventuallyReturn StatusCode.NotFound
  }
}
