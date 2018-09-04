package backend.external.extensions

import backend.external.MarkedLinks
import backend.logging.Logger
import backend.recon.Reconcilable
import common.rich.func.{MoreTraversableInstances, ToMoreFoldableOps}
import javax.inject.Inject

/** For extenders whose result is not dependent on other links. */
private final class StaticExtenderHelper @Inject()(logger: Logger)
    extends ToMoreFoldableOps with MoreTraversableInstances {
  def apply[R <: Reconcilable](extender: StaticExtender[R])(r: R, e: MarkedLinks[R]): Seq[LinkExtension[R]] = {
    val linksForHost = e.filter(_.host == extender.host)
    if (linksForHost.size > 1)
      logger.warn(s"Expected a single host for <$r>, but found: <$linksForHost>.")
    linksForHost.mapHeadOrElse(extender.extend(r, _), Nil)
  }
}
