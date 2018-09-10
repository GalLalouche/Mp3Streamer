package backend.external.extensions

import backend.external.{Host, MarkedLink, MarkedLinks}
import backend.recon.Reconcilable
import common.rich.func.{MoreSeqInstances, TuplePLenses}
import monocle.std.Tuple2Optics
import monocle.syntax.{ApplySyntax, FieldsSyntax}

import scalaz.syntax.ToFunctorOps

/**
* Extenders (not to be confused with Ex<b>p</b>anders) provide additional links to a given links,
* e.g., an artists discography. The key difference from expanders is that extenders do not scrap
* HTML; rather, the extensions are hard-coded, e.g., the discography link will always be in
* $link/discography. Therefore, these extensions neither need to be saved, nor do they involve Futures.
*/
private trait LinkExtender[R <: Reconcilable]
    extends ToFunctorOps with MoreSeqInstances
        with Tuple2Optics with ApplySyntax with FieldsSyntax {
  def host: Host
  // When the name of the extended link is identical to the URL suffix.
  // For example, create a discography link with the URL www.foo.com/some_artist/discography.
  protected def appendSameSuffix(e: MarkedLink[R], suffixes: String*): Seq[LinkExtension[R]] =
    append(e, suffixes.fpair: _*)
  protected def append(e: MarkedLink[R], suffixes: (String, String)*): Seq[LinkExtension[R]] =
    suffixes.map(TuplePLenses.tuple2Second.modify(e.link.+/)).map((LinkExtension.apply[R] _).tupled)
  // For point free style.
  def extend: (R, MarkedLinks[R]) => Seq[LinkExtension[R]]
}
