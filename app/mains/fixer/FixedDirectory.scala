package mains.fixer

import me.tongfei.progressbar.ProgressBar
import rx.lang.scala.Observer

import scala.util.Using

import common.path.ObservablePathUtils
import common.path.ObservablePathUtils.MoveFileProgress
import common.path.ref.io.IODirectory
import common.rx.report.ReportObserver

private class FixedDirectory(val dir: IODirectory, val name: String) {
  def move(to: IODirectory): IODirectory = Using.resource(new ProgressBar("Moving directory", 0)) {
    pb =>
      ReportObserver.asReturnValue[MoveFileProgress, IODirectory](
        ObservablePathUtils.move(dir, to, name, _),
      )(Observer(onNext = p => {
        pb.maxHint(p.total.toLong)
        pb.stepTo(p.processed.toLong)
      }))
  }
}
