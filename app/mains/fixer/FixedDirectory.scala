package mains.fixer

import me.tongfei.progressbar.ProgressBar
import rx.lang.scala.Observer

import scala.util.Using

import common.rich.path.{Directory, ObservableRichFileUtils}
import common.rich.path.ObservableRichFileUtils.MoveFileProgress
import common.rx.report.ReportObserver

private class FixedDirectory(val dir: Directory, val name: String) {
  def move(to: Directory): Directory = Using.resource(new ProgressBar("Moving directory", 0)) {
    pb =>
      ReportObserver.asReturnValue[MoveFileProgress, Directory](
        ObservableRichFileUtils.move(dir, to, name, _),
      )(Observer(onNext = p => {
        pb.maxHint(p.total.toLong)
        pb.stepTo(p.processed.toLong)
      }))
  }
}
