package backend.external.extensions

import backend.external.{MarkedLink, MarkedLinks}
import backend.logging.LoggerProvider
import backend.recon.Reconcilable
import common.rich.func.{MoreTraverseInstances, ToMoreFoldableOps}

/** Extenders whose result is not dependent on other links. */
private abstract class StaticExtender[R <: Reconcilable](implicit lp: LoggerProvider) extends LinkExtender[R]
    with ToMoreFoldableOps with MoreTraverseInstances {
  protected def apply(t: R, e: MarkedLink[R]): Seq[LinkExtension[R]]
  override def apply(r: R, e: MarkedLinks[R]): Seq[LinkExtension[R]] = {
    val linksForHost = e.filter(_.host == this.host)
    if (linksForHost.size > 1)
      lp.logger.warn(s"Expected a single host for <$r>, but found: <$linksForHost>.")
    linksForHost.mapHeadOrElse(apply(r, _), Nil)
  }
}
