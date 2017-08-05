package backend.external.extensions

import backend.configs.StandaloneConfig
import backend.external.{MarkedLink, MarkedLinks}
import backend.recon.Reconcilable
import common.rich.RichT._

/** Extenders whose result is not dependent on other links. */
private trait StaticExtender[R <: Reconcilable] extends LinkExtender[R] {
  protected def apply(t: R, e: MarkedLink[R]): Seq[LinkExtension[R]]
  override def apply(t: R, e: MarkedLinks[R]): Seq[LinkExtension[R]] = {
    val linksForHost = e.flatMap(_.opt.filter(_.host == this.host))
    if (linksForHost.size > 1)
    // TODO pass config?
      StandaloneConfig.logger.warn(s"Expected a single host for <$t>, but found: <$linksForHost>.")
    linksForHost.take(1).flatMap(apply(t, _)).toList
  }
}
