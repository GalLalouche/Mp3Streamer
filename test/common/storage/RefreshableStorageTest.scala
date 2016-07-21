//package common.storage
//
//import common.AuxSpecs
//import org.scalatest.mock.MockitoSugar
//import org.scalatest.{FreeSpec, FunSuite}
//import common.rich.RichT._
//import common.concurrency.SingleThreadedExecutionContext._
//import org.joda.time.Duration
//
//import scala.concurrent.Future
//
//class RefreshableStorageTest extends FreeSpec with MockitoSugar with AuxSpecs {
//  val mockStorage = mock[FreshnessStorage[String, String]]
//  "store" - {
//    "stores" in {
//      val $ = new RefreshableStorage[String, String](mockStorage, _.reverse |> Future.successful, Duration.millis(5))
//    }
//  }
//}
