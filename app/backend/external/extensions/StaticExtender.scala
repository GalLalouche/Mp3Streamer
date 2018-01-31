package backend.external.extensions

import backend.configs.StandaloneConfig
import backend.external.{MarkedLink, MarkedLinks}
import backend.recon.Reconcilable
import common.rich.func.{MoreTraverseInstances, ToMoreFoldableOps}

/** Extenders whose result is not dependent on other links. */
private trait StaticExtender[R <: Reconcilable] extends LinkExtender[R]
    with ToMoreFoldableOps with MoreTraverseInstances {
  protected def apply(t: R, e: MarkedLink[R]): Seq[LinkExtension[R]]
  override def apply(r: R, e: MarkedLinks[R]): Seq[LinkExtension[R]] = {
    val linksForHost = e.filter(_.host == this.host)
    if (linksForHost.size > 1)
    // TODO pass config?
      StandaloneConfig.logger.warn(s"Expected a single host for <$r>, but found: <$linksForHost>.")
    linksForHost.mapHeadOrElse(apply(r, _), Nil)
  }
}
