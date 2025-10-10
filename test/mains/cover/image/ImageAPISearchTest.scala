package mains.cover.image

import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps
import common.test.AsyncAuxSpecs
import io.lemonlabs.uri.Url
import mains.cover.UrlSource
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify}
import org.scalatest.{AsyncFreeSpec, Succeeded}
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.Future

class ImageAPISearchTest extends AsyncFreeSpec with AsyncAuxSpecs with MockitoSugar {
  private val imageApiFetcher = mock[ImageAPIFetcher]
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
        UrlSource(mock[Url], width = width, height = height)
      })
    }
  private val $ = new ImageAPISearch(executionContext, imageApiFetcher)

  "Does not fetch more than is needed" in {
    val seq = $("whatever", 1000).filter(i => i.isSquare && i.width >= MinSide).take(10).toSeq
    seq >| verify(imageApiFetcher, times(6)).apply(any, any) >| Succeeded
  }
}
