package backend.external.extensions

import backend.external.BaseLink
import backend.recon.Reconcilable

/**
* Extenders (not to be confused with Ex<b>p</b>anders) provide additional links to a given links, e.g., an artists discography.
* The key difference from expanders is that extenders do not scrap HTML; rather, the extensions are hard-coded.
* For example, the discography link will always be in $link/discography. Due to that, this extensions neither need to be saved,
* nor do they involve Futures.
*/
private trait LinkExtender[-R <: Reconcilable] {
  // When the name of the extended link is identical to the URL suffix.
  // For example, create a discography link with the URL www.foo.com/some_artist/discography.
  protected def appendSameSuffix[T <: R](e: BaseLink[T], suffixes: String*): Seq[LinkExtension[T]] =
    append(e, suffixes.map(e => e -> e): _*)
  protected def append[T <: R](e: BaseLink[T], suffixes: (String, String)*): Seq[LinkExtension[T]] =
    suffixes.map(x => x._1 -> (e.link +/ "/" +/ x._2)).map((LinkExtension.apply[T] _).tupled)
  def apply[T <: R](t: T, e: BaseLink[T]): Seq[LinkExtension[T]]
}
