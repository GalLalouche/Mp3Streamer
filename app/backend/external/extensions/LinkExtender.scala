package backend.external.extensions

import backend.external.{Host, MarkedLink, MarkedLinks}
import backend.recon.Reconcilable

/**
* Extenders (not to be confused with Ex<b>p</b>anders) provide additional links to a given links,
* e.g., an artists discography. The key difference from expanders is that extenders do not scrap
* HTML; rather, the extensions are hard-coded, e.g., the discography link will always be in
* $link/discography. Therefore, these extensions neither need to be saved, nor do they involve Futures.
*/
private trait LinkExtender[R <: Reconcilable] {
  def host: Host
  // When the name of the extended link is identical to the URL suffix.
  // For example, create a discography link with the URL www.foo.com/some_artist/discography.
  protected def appendSameSuffix(e: MarkedLink[R], suffixes: String*): Seq[LinkExtension[R]] =
    append(e, suffixes.map(e => e -> e): _*)
  protected def append(e: MarkedLink[R], suffixes: (String, String)*): Seq[LinkExtension[R]] =
    suffixes.map(x => x._1 -> (e.link +/ "/" +/ x._2)).map((LinkExtension.apply[R] _).tupled)
  def apply(t: R, e: MarkedLinks[R]): Seq[LinkExtension[R]]
}
