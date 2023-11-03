package backend.lyrics.retrievers

import com.google.inject.BindingAnnotation

import scala.annotation.meta.param
import scala.annotation.Annotation

@param
@BindingAnnotation
private[lyrics] class CompositeAlbumParser extends Annotation
