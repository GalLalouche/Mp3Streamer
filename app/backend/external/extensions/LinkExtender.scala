package backend.external.extensions

import backend.external.ExternalLink
import backend.recon.Reconcilable

/**
* Extenders (not to be confused with Ex<b>p</b>anders) provide additional links to a given links, e.g., an artists discography.
* The key difference from expanders is that extenders do not scrap HTML; rather, the extensions are hard-coded.
* For example, the discography link will always be in $link/discography. Due to that, this extensions neither need to be saved,
* nor do they involve Futures.
*/
private trait LinkExtender[-R <: Reconcilable] {
  protected implicit def stringExtension(s: String): (String, String) = s -> s
  protected def append[T <: R](e: ExternalLink[T], suffixes: (String, String)*): Seq[LinkExtension[T]] =
    suffixes.map(x => x._1 -> (e.link +/ "/" +/ x._2)).map(e => LinkExtension[T](e._1, e._2))
  def apply[T <: R](t: T, e: ExternalLink[T]): Seq[LinkExtension[T]]
}
