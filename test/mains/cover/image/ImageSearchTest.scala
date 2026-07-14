package mains.cover.image

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify}
import org.scalatest.Succeeded
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json

import scala.concurrent.Future

import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.test.AsyncAuxSpecs

class ImageSearchTest extends AsyncFreeSpec with AsyncAuxSpecs with MockitoSugar {
  private val imageApiFetcher = mock[ImageAPI]
  private val MinSide = 500
  Mockito
    .when(imageApiFetcher(any, any))
    .thenAnswer { args =>
      val index = args.getArgument[Int](1)
      Future((index * 10).until((index + 1) * 10).map { i =>
        val isSquare = i % 3 == 0
        val isLargeEnough = i % 2 == 0
        val width = if (isLargeEnough) MinSide else MinSide / 2
        val height = if (isSquare) width else MinSide / 3
        Json.obj(
          "original" -> s"https://example.com/image$i.jpg",
          "original_width" -> width,
          "original_height" -> height,
        )
      })
    }
  private val $ = new ImageSearch(executionContext, imageApiFetcher)

  "Does not fetch more than is needed" in {
    val seq = $("whatever", 1000).filter(i => i.isSquare && i.width >= MinSide).take(10).toSeq
    seq >| verify(imageApiFetcher, times(6)).apply(any, any) >| Succeeded
  }
}
