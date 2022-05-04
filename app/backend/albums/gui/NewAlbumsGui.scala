package backend.albums.gui

import backend.albums.AlbumsModel
import javax.inject.Inject

import scala.concurrent.ExecutionContext

import javafx.scene.{control => jfx}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.{Button, ScrollPane, TitledPane}
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.input.{Clipboard, ClipboardContent}
import scalafx.scene.layout.{HBox, Pane, VBox}
import scalafx.scene.text.{Text, TextFlow}

import common.rich.collections.RichSeq._
import common.rich.RichFuture.richFuture
import common.rich.RichT.richT
import common.scalafx.Builders
import common.scalafx.RichNode.richNode
import common.Orderings

// REMAINING:
// * Select orderings from the GUI.
// * More buttons (remove/ignore album, remove/ignore artist, torrent).
// ** For artists, this is not trivial, since the TitledPane of the artist needs to be marked so the buttons
//    could be added to it.
// ** Remove row/artist on ignore/remove
// * Genre per row doesn't make sense. Add it to the artist pane if genre isn't grouped on
private class NewAlbumsGui @Inject()(
    albumsModel: AlbumsModel,
    ec: ExecutionContext,
    stateFactory: GuiState.Factory,
) {
  val root: Pane = new Pane

  private implicit val iec: ExecutionContext = ec
  private val state = stateFactory(
    data = albumsModel.albumsFiltered.run.get,
    rowOrdering = Orderings.fromSeqCheck(Vector(
      DataExtractor.Year,
      DataExtractor.Score,
      DataExtractor.Genre,
      DataExtractor.Artist,
      DataExtractor.Album,
    )),
    orders = Vector(Order.ByMissingAlbums, Order.ByScore),
  )
  private def update(): Unit = root.children = Function.tupled(createPane _)(state.prepare)
  update()

  private def createPane(data: GroupedData, extractors: Seq[DataExtractor]): Node = {
    def toRow(e: AlbumEntry): Pane = {
      val text = new TextFlow {
        children = extractors.view.map(NewAlbumsGui.toText(e)).intersperse(new Text(" - "))
        margin = Builders.insets(right = 5, left = 5)
      }
      val searchableTerm = s"${e.artist.name} ${e.albumTitle}"
      val copyToClipboard = new Button("Copy to clipboard") {
        onAction = _ => Clipboard.systemClipboard.content =
          new ClipboardContent().<|(_.putString(searchableTerm))
      }
      val ignore = new Button("Ignore album") {
        onAction = _ => albumsModel.ignoreAlbum(e.artist, e.albumTitle)
      }
      val torrent = new Button("Torrent")
      new HBox {
        spacing = 1
        margin = Insets(0)
        padding = Insets(0)
        alignment = Pos.BaselineLeft
        children = Vector(
          text,
          copyToClipboard,
          torrent,
          ignore,
        )
      }
    }
    def nestTitledPanes(data: Grouped, fontSize: Int): VBox = new VBox(0) {
      padding = Insets(1)
      margin = Builders.insets(left = 10)
      val nextSize: Int = fontSize - 2
      children = data.groups.map {
        case (label, e) =>
          new TitledPane {
            text = label
            content = e match {
              case e: Grouped => nestTitledPanes(e, nextSize)
              case Rows(entries) => new VBox {
                margin = Insets(0)
                padding = Insets(0)
                children = entries.map(toRow)
              }.<|(_.setFontSize(nextSize))
            }
            animated = false
            collapsible = true
            expanded = true
          }
      }
    }.<|(_.setFontSize(fontSize))
    new ScrollPane {
      content =
        nestTitledPanes(data.asInstanceOf[Grouped], fontSize = 18)
            .<|(_.children.foreach(_.asInstanceOf[jfx.TitledPane]
                // The top level panes are collapsible and unexpanded by default.
                .<|(_.setCollapsible(true))
                .<|(_.setExpanded(false))))
      fitToHeight = true
      prefHeight = 1400
      vbarPolicy = ScrollBarPolicy.Always
    }
  }
}

private object NewAlbumsGui {
  private def toText(e: AlbumEntry)(ex: DataExtractor): Text = new Text(ex match {
    case DataExtractor.Score => e.score.orDefaultString
    case DataExtractor.Year => e.date.getYear.toString
    case DataExtractor.Genre => e.genre.fold("N/A")(_.name)
    case DataExtractor.Artist => e.artist.name
    case DataExtractor.Album => s"${
      e.albumTitle
    } (${
      e.albumType
    })"
  })
}
