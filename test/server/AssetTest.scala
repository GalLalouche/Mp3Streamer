package server

import java.io.File

import com.google.inject.Module
import sttp.client3.UriContext
import sttp.model.StatusCode

import common.rich.RichFile.richFile

private class AssetTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
  "ts route returns typescript file" in {
    getBytes(uri"/ts/common.ts") shouldEventuallyReturn new File("public/typescripts/common.ts").bytes
  }

  "ts route returns 404 for nonexistent file" in {
    getRaw(uri"/ts/nonexistent_file_abc123.ts") codeShouldEventuallyReturn StatusCode.NotFound
  }

  // On Linux, AssetHttpRoutes.asset builds "public\" + path using a backslash, which
  // java.io.File treats as a literal character (not a path separator). The file won't
  // be found, so sendFileOrNotFound returns 404.
  "assets route returns 404 on Linux due to backslash path separator" in {
    getRaw(uri"/assets/html/main.html") codeShouldEventuallyReturn StatusCode.NotFound
  }

  // Same backslash issue as the assets route. The path becomes
  // "public\javascripts\jquery.js" which doesn't exist on Linux.
  "js route returns 404 on Linux due to backslash path separator" in {
    getRaw(uri"/js/jquery.js") codeShouldEventuallyReturn StatusCode.NotFound
  }

  // Files not ending in "ts" resolve to target/web/public/main/typescripts/...,
  // which is a build output directory and won't exist in test.
  "ts route returns 404 for js file when target directory missing" in {
    getRaw(uri"/ts/common.js") codeShouldEventuallyReturn StatusCode.NotFound
  }
}
