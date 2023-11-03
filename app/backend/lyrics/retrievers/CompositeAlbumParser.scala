package backend.lyrics.retrievers

import scala.annotation.meta.param
import scala.annotation.Annotation

import com.google.inject.BindingAnnotation

@param
@BindingAnnotation
private[lyrics] class CompositeAlbumParser extends Annotation
